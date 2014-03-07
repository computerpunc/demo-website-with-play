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

import play.api.mvc._
import play.api.Play.current
import java.io.File
import java.net.URLEncoder
import play.api.libs.concurrent.Akka
import scala.concurrent.duration._
import com.typesafe.plugin._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.Some


object Application extends Controller {
  def ActionOverHttps(f: Request[AnyContent] => Result): Action[AnyContent] = Action { request =>
    request.headers.get("x-forwarded-proto") match {
      case Some(header)=> if ("https"==header) {
        f(request) match {
          case res:PlainResult=> res.withHeaders(("Strict-Transport-Security", "max-age=31536000"))
          case res:Result=> res
        }
      } else Redirect(current.configuration.getString("controllers.application.appServer").get+request.uri)
      case None=> f(request)
    }
  }

  def index=ActionOverHttps { requst=>
    Redirect("/dashboard")
  }

  def login(redirect:Option[String])=ActionOverHttps { request=>
    Ok(views.html.login(redirect)).discardingCookies("userId", "accessToken")
  }

  def signup=ActionOverHttps { request=>
    Ok(views.html.signup())
  }

  def completeSignup(userId:Long, email:String, resetPasswordToken:String)=ActionOverHttps { request=>
    Akka.system.scheduler.scheduleOnce(0.1 seconds) {
      val mail=use[MailerPlugin].email
      mail.setFrom(current.configuration.getString("controllers.core.sendMailFrom").get)
      mail.setSubject("Page /singup/completed reached by:"+email)
      mail.setRecipient(current.configuration.getString("controllers.core.sendSignupsTo").get)
      mail.send("/singup/completed")
    }
    Ok(views.html.completeSignup(userId, email, resetPasswordToken)).discardingCookies("userId", "accessToken")
  }

  def request=ActionOverHttps { request=>
    Ok(views.html.request())
  }


  def forgotPassword=ActionOverHttps { request=>
    Ok(views.html.forgotPassword())
  }


  def resetPassword(userId:Long, email:String, resetPasswordToken:String)=ActionOverHttps { request=>
    Ok(views.html.resetPassword(userId, email, resetPasswordToken)).discardingCookies("userId", "accessToken")
  }

  def dashboard=ActionOverHttps { request=>
    Ok.sendFile(content=new File("public/apps/dashboard/app/index.html"), inline=true).as("text/html")
//      request.cookies.get("developer") match {
//        case None=>Redirect(routes.Application.login)
//        case Some(cookie)=>Ok(views.html.index("Your new application is ready."))
  }

  def sdk=ActionOverHttps { request=>
    Redirect(current.configuration.getString("controllers.application.sdkUrl").get)
  }

  def docs=ActionOverHttps { request=>
    Redirect(current.configuration.getString("controllers.application.docsUrl").get)
  }

  def redirectOrLogin(redirect:String)=ActionOverHttps { request=>
    val ard=Redirect("/login?redirect="+URLEncoder.encode("/"+redirect, "UTF8"))
    (request.cookies.get("userId"), request.cookies.get("accessToken")) match {
      case (Some(userId), Some(accessToken))=> if (1>userId.value.length||1>accessToken.value.length) ard else Redirect("/"+redirect)
      case _=> ard
    }
  }

  def redirectRandomly(base:String, ext:Option[String], r:String, ref:Option[String])=Action { request =>
    val extSt=ext match {
      case Some(value)=> "."+value
      case None=>""
    }
    val refSt=ref match {
      case Some(value)=> "?ref="+value
      case None=> ""
    }
    val rands=r.split("-")
    val rand=rands((math.random*rands.size).toInt)

    Redirect(current.configuration.getString("controllers.wwwServer").get+"/"+base+rand+extSt+refSt+base+rand)//.withCookies()
  }

  def angularJsConfig=Action { request=>
    Ok(views.js.angularjsConfig())
  }


}