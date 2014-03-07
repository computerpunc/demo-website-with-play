//  The MIT License (MIT)
//  Copyright (c) 2012 Ram Hardy & Elad Hemar
//
//    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
//    documentation files (the "Software"), to deal in the Software without restriction, including without limitation
//    the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
//    and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
//
//    The above copyright notice and this permission notice shall be included in all copies or substantial portions
//    of the Software.
//
//    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
//    TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
//    THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
//    CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
//    IN THE SOFTWARE.

package controllers

/**
 * Created with IntelliJ IDEA.
 * User: ram
 * Date: 6/9/12
 * Time: 8:34 AM
 * To change this template use File | Settings | File Templates.
 */

//import _root_.models.{App, User}

import models._
import play.api._
import libs.concurrent.Akka
import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.libs.ws.WS
import play.api.mvc._
import com.typesafe.plugin._
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import scala.Some
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.FileUtils
import scala.concurrent.duration._
import scala.concurrent.Future

object Core extends Controller {

  val forbidden=Future{ Forbidden("{\"error\":\"credentials missmatch\"}").as("application/json") }

  def ActionOverHttps[T](parser:BodyParser[T])(f: Request[T] => Future[SimpleResult]):Action[T]= Action.async[T](parser) { request =>
    request.headers.get("x-forwarded-proto") match {
      case Some(header)=> if ("https"==header) f(request) else Future { Redirect(current.configuration.getString("controllers.application.appServer").get+request.uri) }
      case None=> f(request)
    }
  }

  def doWithUserNoAuth[T](parser:BodyParser[T])(userId:Long,  doIt:(Request[T], User)=> Future[SimpleResult])=ActionOverHttps[T](parser) { request=>
    User.findById(userId) match {
      case None=> forbidden
      case Some(user)=> doIt(request, user)
    }
  }

  def doWithUser[T](parser:BodyParser[T])(userId:Long, accessToken:String, doIt:(Request[T], User)=> Future[SimpleResult])=doWithUserNoAuth[T](parser)(userId, { (request, user)=>
    if (accessToken==user.accessToken) doIt(request, user)
    else forbidden
  })

  def makeUser(user:User):JsValue=toJson(Map("id"->toJson(user.id.get), "accessToken"->toJson(user.accessToken), "name"->toJson(user.name), "email"->toJson(user.email)))

  def registerUser=ActionOverHttps[JsValue](parse.json) { request=>
    import play.api.libs.concurrent._
    (request.body\"email").asOpt[String] match {
      case Some(email)=> {
        val resetPasswordToken=java.util.UUID.randomUUID().toString.replace("-","")
        val user=User.findByEmail(email).getOrElse {
            var password=resetPasswordToken
            val accessToken=java.util.UUID.randomUUID().toString.replace("-","")
            val userId=User.create(email, (request.body\"name").asOpt[String].getOrElse("[Name Not Set]"), password, accessToken, resetPasswordToken)
            User.findById(userId.get).get
        }
        if (user.checkPassword(user.resetPasswordToken.getOrElse(""))) {  // password == reset-password-token then this user has not signed up yet
          val userWithToken=if (Some(resetPasswordToken)!=user.resetPasswordToken) {
            User.updatePassword(user.id.get, resetPasswordToken)
            User.updateResetPasswordToken(user.id.get, Some(resetPasswordToken))
            User.findById(user.id.get).get
          } else user
          Akka.system.scheduler.scheduleOnce(0.1 seconds) {
            val mail1=use[MailerPlugin].email
            mail1.setFrom(current.configuration.getString("controllers.core.sendMailFrom").get)
            mail1.setSubject("New Signup Request:"+email)
            mail1.setRecipient(current.configuration.getString("controllers.core.sendSignupsTo").get)
            mail1.send(views.txt.completeSignupToUs(userWithToken, request.body.toString()).body)

            val mail2=use[MailerPlugin].email
            mail2.setFrom(current.configuration.getString("controllers.core.sendMailFrom").get)
            mail2.setSubject("Welcome to example.com!")
            mail2.setRecipient(email)
            mail2.send(views.txt.completeSignup().body)
          }
        } else {
          User.updateResetPasswordToken(user.id.get, Some(resetPasswordToken))
          val userWithToken=User.findById(user.id.get).get
          Akka.system.scheduler.scheduleOnce(10 seconds) {
            val mail=use[MailerPlugin].email
            mail.setFrom(current.configuration.getString("controllers.core.sendMailFrom").get)
            mail.setSubject("Reset Password Instructions")
            mail.setRecipient(email)
            //mail.addBcc(current.configuration.getString("controllers.core.sendSignupsTo").get)
            mail.send(views.txt.resetPassword(userWithToken).body)
          }
        }
        Future { Ok("{}").as("application/json") }
      }
      case _=> Future { BadRequest("{\"error\":\"email is missing\"}").as("application/json") }
    }
  }

  def userByIdAndAccessToken(userId:Long, accessToken:String)=doWithUser[Option[Any]](parse.empty)(userId, accessToken, { (request, user)=>
    val json=makeUser(user)
    Future { Ok(json).as("application/json") }
  })

  def usersByEmailAndPassword(email:String, password:String)=ActionOverHttps[AnyContent](parse.anyContent) { request=>
    val ok=Future { Ok("[]").as("application/json") }
    User.findByEmail(email) match {
      case None=> ok
      case Some(user)=>{
        if (user.checkPassword(password)) {
          val json=JsArray(Seq(makeUser(user)))
          Future { Ok(json).as("application/json") }
        } else ok
      }
    }
  }

  def accessToken(id:Long, password:String)=doWithUserNoAuth[Option[Any]](parse.empty)(id, { (request, user)=>
    if (password==user.password) Future { Ok("{\"accessToken\":\""+user.accessToken+"\"}").as("application/json") }
    else Future { Forbidden("{\"error\":\"password missmatch\"}").as("application/json") }
  })

  def updateUser(userId:Long, accessToken:String)=doWithUser[JsValue](parse.json)(userId, accessToken, { (request, user)=>
    request.body.as[JsObject].fields foreach {
      case ("name", name)=> User.updateName(user.id.get, name.as[String])
      //case ("password", password)=> User.updatePassword(user.id.get, password.as[String])
    }
    val Some(updatedUser)=User.findById(user.id.get)
    Future { Ok(makeUser(updatedUser)).as("application/json") }
  })

  def updateUserPassword(userId:Long, currentPassword:Option[String], resetPasswordToken:Option[String])=doWithUserNoAuth[String](parse.tolerantText(128))(userId, { (request, user)=>
    if (1>request.body.length) Future { BadRequest("{\"error\":\"password is to short\"}").as("application/json") }
    else {
      def changePassword()={
        User.updatePassword(user.id.get, request.body)
        User.updateResetPasswordToken(user.id.get, None)

        Future { Ok("{}").as("application/json") }
      }

      Akka.system.scheduler.scheduleOnce(new DurationDouble(0.1) seconds) {
        val mail=use[MailerPlugin].email
        mail.setFrom(current.configuration.getString("controllers.core.sendMailFrom").get)
        mail.setSubject("New Password set by:"+user.email)
        mail.setRecipient(current.configuration.getString("controllers.core.sendSignupsTo").get)
        mail.send(views.txt.resetPasswordToUs(user).body)
      }

      (currentPassword, resetPasswordToken) match {
        case (Some(current), _)=>{
          if (user.checkPassword(current)) changePassword()
          else forbidden
        }
        case (None, tokenOption)=>{
          if (tokenOption==user.resetPasswordToken) changePassword()
          else forbidden
        }
        case _=> Future { Forbidden("{\"error\":\"missing password/token\"}").as("application/json") }
      }
    }
  })


  // apps

  def makeApp(json:JsValue):JsObject={
    val app=json.as[JsObject].fields map {
      _ match {
        case ("id", value)=> ("id", value)
        case ("uuid", value)=> ("uuid", value)
        case ("name", value)=> ("name", value)
        case ("downloadUrl", value)=> ("downloadUrl", value)
        case ("imageUrl", value)=> ("imageUrl", value)
        case ("pushCert_apns", value)=> ("pushCert_apns", value)
        case ("pushCert_apnsd", value)=> ("pushCert_apnsd", value)

        case _=> null
      }
    } filter(null!=_)
    JsObject(app)
  }

  def apps(userId:Long, accessToken:String)=doWithUser[Option[Any]](parse.empty)(userId, accessToken, { (request, user)=>
    val qs=App.findByUserId(userId) map { app=> "id="+app.id.get+"&accessToken="+app.accessToken }
    WS.url(current.configuration.getString("controllers.core.coreServer").get+"/apps"+qs.mkString("?", "&", "")).get().map { response =>
      response.status match {
        case 200=>{
          val apps=response.json match {
            case JsArray(es)=> es map { e=>makeApp(e) }
            case _ => throw new RuntimeException("Elements MUST be a list")
          }
          Ok(toJson(apps)).as("application/json")
        }
        case _=> ServiceUnavailable("{\"error\":\"Cannot get a response from core\"}")
      }
    }
  })

  def doWithApp[T](parser:BodyParser[T])(appId:Long, userId:Long, accessToken:String, doIt:(Request[T], App)=>Future[SimpleResult])=doWithUser[T](parser)(userId, accessToken, { (request, user)=>
    App.findById(appId) match  {
      case None=>forbidden
      case Some(app)=> {
        if (app.userId!=userId) forbidden
        else doIt(request, app)
      }
    }
  })

  def app(appId:Long, userId:Long, accessToken:String)=doWithApp[Option[Any]](parse.empty)(appId, userId, accessToken, { (request, app)=>
    WS.url(current.configuration.getString("controllers.core.coreServer").get+"/apps/"+app.id.get+"?accessToken="+app.accessToken).get().map { response =>
      response.status match {
        case 200=> Ok(toJson(makeApp(response.json))).as("application/json")
        case _=> ServiceUnavailable("{\"error\":\"Cannot get a response from core\"}")
      }
    }
  })

  def prepareUpdate(request:Request[JsValue]):Map[String, Seq[String]]={
    val update=request.body.as[JsObject].fields map {
      _ match {
        case ("name", value)=>("name", Seq(value.as[String]))
        case ("downloadUrl", value)=>("downloadUrl", Seq(value.as[String]))
        case _=>null
      }
    } filter(null!=_)
    update.toMap
  }

  def updateWithApp(appId:Long, userId:Long, accessToken:String, updateIt:(Request[JsValue], App, Map[String, Seq[String]])=>Future[SimpleResult])=doWithApp[JsValue](parse.json)(appId, userId, accessToken, { (request, app)=>
    val update=prepareUpdate(request)
    updateIt(request, app, update)
  })

  def registerApp(userId:Long, accessToken:String)=doWithUser[JsValue](parse.json)(userId, accessToken,  { (request, user)=>
    val update=prepareUpdate(request)
    WS.url(current.configuration.getString("controllers.core.coreServer").get+"/apps?userId="+user.id+"&accessToken="+user.accessToken).post(update).map { response =>
      response.status match {
        case 200=> {
          App.register((response.json\"id").as[Long], userId, (response.json\"accessToken").as[String])
          Ok(toJson(makeApp(response.json))).as("application/json")
        }
        case _=> ServiceUnavailable("{\"error\":\"Cannot get a response from core\"}")
      }
    }
  })

  def updateApp(appId:Long, userId:Long, accessToken:String)=updateWithApp(appId, userId, accessToken,  { (request,app, update)=>
    WS.url(current.configuration.getString("controllers.core.coreServer").get+"/apps/"+app.id.get+"?accessToken="+app.accessToken).put(update).map { response =>
      response.status match {
        case 200=> Ok(toJson(makeApp(response.json))).as("application/json")
        case _=> ServiceUnavailable("{\"error\":\"Cannot get a response from core\"}")
      }
    }
  })

  def updateAppUsingForm(appId:Long, userId:Long, accessToken:String)=doWithApp[play.api.mvc.MultipartFormData[play.api.libs.Files.TemporaryFile]](parse.multipartFormData)(appId, userId, accessToken,  { (request, app)=>
    val nonFiles=request.body.asFormUrlEncoded filter { nonFile=>val (key, values)=nonFile; "name"==key||"downloadUrl"==key }
    val files=request.body.files filter { 8192>_.ref.file.length } map { file=>
      file.key match {
        case "pushCert_apns"=> (file.key,  Seq(Base64.encodeBase64String(FileUtils.readFileToByteArray(file.ref.file))))
        case "pushCert_apnsd"=> (file.key,  Seq(Base64.encodeBase64String(FileUtils.readFileToByteArray(file.ref.file))))
        case _=>null
      }
    } filter (null!=_)

    WS.url(current.configuration.getString("controllers.core.coreServer").get+"/apps/"+app.id.get+"?accessToken="+app.accessToken).put(nonFiles++files.toMap).map { response =>
      response.status match {
        case 200=> Ok(toJson(makeApp(response.json))).as("application/json")
        case _=> ServiceUnavailable("{\"error\":\"Cannot get a response from core\"}")
      }
    }
  })
}
