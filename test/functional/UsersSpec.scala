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

package functional

/**
 * Created with IntelliJ IDEA.
 * User: Ram Hardy
 * Date: 6/11/12
 * Time: 5:17 PM
 */

import org.specs2.mutable._
import org.specs2.mock.Mockito

import play.api.test._
import play.api.test.Helpers._
import models.User
import play.api.libs.json.Json
import play.api.db.DB
import play.api.Play.current
import play.api.Application
import anorm._
import play.api.test.FakeHeaders
import play.api.test.FakeApplication
import scala.Some
import play.api.test.FakeHeaders
import play.api.test.FakeApplication
import scala.Some
import play.api.test.FakeHeaders
import play.api.test.FakeApplication
import scala.Some
import play.api.test.FakeHeaders
import play.api.test.FakeApplication
import scala.Some
import play.api.test.FakeHeaders
import play.api.test.FakeApplication
import scala.Some
import play.api.libs.ws.WS

import com.typesafe.plugin._

class MockMailerPlugin(app:Application) extends MailerPlugin with Mockito {
  def email: com.typesafe.plugin.MailerAPI=mock[MailerAPI]
}


class UsersSpec extends Specification with Mockito {
  "Server with user" should {
    "get user info" in {
      running(FakeApplication()) {
        val result=route(FakeRequest(GET, "/users/1?accessToken=at12345")).get

        status(result) must equalTo(OK)
        contentType(result) must beSome("application/json")
        //charset(result) must beSome("utf-8")
        contentAsString(result) must equalTo("{\"id\":1,\"accessToken\":\"at12345\",\"name\":\"Ram Hardy\",\"email\":\"ram@example.com\"}")
      }
    }

    "not get user info when no access token" in {
      running(FakeApplication()) {
        val result=route(FakeRequest(GET, "/users/1")).get

        status(result) must equalTo(BAD_REQUEST)
      }
    }

    "not get user info when no valid access token" in {
      running(FakeApplication()) {
        val result=route(FakeRequest(GET, "/users/1?accessToken=at11111")).get

        status(result) must equalTo(FORBIDDEN)
        contentType(result) must beSome("application/json")
        //charset(result) must beSome("utf-8")
        contentAsString(result) must equalTo("{\"error\":\"credentials missmatch\"}")
      }
    }

    "get user when presented with a valid email and password" in {
      running(FakeApplication()) {
        val result=route(FakeRequest(GET, "/users?email=ram%40example.com&password=h12345")).get

        status(result) must equalTo(OK)
        contentType(result) must beSome("application/json")
        //charset(result) must beSome("utf-8")
        contentAsString(result) must equalTo("[{\"id\":1,\"accessToken\":\"at12345\",\"name\":\"Ram Hardy\",\"email\":\"ram@example.com\"}]")
      }
    }

    "get nothing when presented with a invalid email and password" in {
      running(FakeApplication()) {
        val result=route(FakeRequest(GET, "/users?email=ram%40example.com&password=h123451")).get

        status(result) must equalTo(OK)
        contentType(result) must beSome("application/json")
        //charset(result) must beSome("utf-8")
        contentAsString(result) must equalTo("[]")
      }
    }

    "shouldn't get access token when presented with a invalid password" in {
      running(FakeApplication()) {
        val result=route(FakeRequest(POST, "/users/1/accessToken?password=h")).get

        status(result) must equalTo(FORBIDDEN)
        contentType(result) must beSome("application/json")
        //charset(result) must beSome("utf-8")
        contentAsString(result) must equalTo("{\"error\":\"password missmatch\"}")
      }
    }

    "create & update a new user" in {
      running(FakeApplication(additionalPlugins=Seq("functional.MockMailerPlugin"))) {
        val mailer=use[MailerPlugin].email

        val userId0=DB.withConnection { implicit c =>
          SQL("DELETE FROM users WHERE email='john@example.com'").executeUpdate()
          SQL("SELECT id FROM users ORDER BY id desc limit 1").apply().head[Long]("id")
        }
        val body=Json.parse("{\"email\":\"john@example.com\", \"name\":\"John by Example\"}")
        val result=route(new FakeRequest(POST, "/users", FakeHeaders(Seq("Content-Type"->Seq("application/json"))), body)).get

        status(result) must equalTo(OK)
        contentType(result) must beSome("application/json")
        //charset(result) must beSome("utf-8")
        contentAsString(result) must equalTo("{}")

        val userId1:Long=DB.withConnection { implicit c =>
          SQL("SELECT id FROM users ORDER BY id desc limit 1").apply().head[Long]("id")
        }
        userId0 mustNotEqual(userId1)
//           //eventually()
//        there was one(mailer).setSubject("New Signup Request:john@example.com")
      }
    }

    "can't create a new user without an email" in {
      running(FakeApplication(additionalPlugins=Seq("functional.MockMailerPlugin"))) {
        val userId0=DB.withConnection { implicit c =>
          SQL("DELETE FROM users WHERE email='john@example.com'").executeUpdate()
          SQL("SELECT id FROM users ORDER BY id desc limit 1").apply().head[Long]("id")
        }
        val body=Json.parse("{\"name\":\"John by Example\"}")
        val result=route(new FakeRequest(POST, "/users", FakeHeaders(Seq("Content-Type"->Seq("application/json"))), body)).get

        status(result) must equalTo(BAD_REQUEST)
        contentType(result) must beSome("application/json")
        //charset(result) must beSome("utf-8")
        contentAsString(result) must equalTo("{\"error\":\"email is missing\"}")
      }
    }

    "should create a new password for new user and then change it" in {
      running(FakeApplication(additionalPlugins=Seq("functional.MockMailerPlugin"))) {
        DB.withConnection { implicit c =>
          SQL("DELETE FROM users WHERE email='john@example.com'").executeUpdate()
        }
        val createBody=Json.parse("{\"email\":\"john@example.com\", \"name\":\"John by Example\"}")
        val createResult=route(new FakeRequest(POST, "/users", FakeHeaders(Seq("Content-Type"->Seq("application/json"))), createBody)).get
        status(createResult) must equalTo(OK)

        val userId:Long=DB.withConnection { implicit c =>
          SQL("SELECT id FROM users ORDER BY id desc limit 1").apply().head[Long]("id")
        }

        val checkPasswordResult=route(FakeRequest(GET, "/users?email=john%40example.com&password=p1234567")).get
        status(checkPasswordResult) must equalTo(OK)
        contentAsString(checkPasswordResult) must equalTo("[]")

        val resetPasswordToken:String=DB.withConnection { implicit c =>
          SQL("SELECT reset_password_token FROM users ORDER BY id desc limit 1").apply().head[String]("reset_password_token")
        }


        // can't create new password w/o valid resetPasswordToken
        val updateBody="p1234567"
        val updateBadResult=route(new FakeRequest(PUT, "/users/"+userId+"/password?resetPasswordToken=12345678", FakeHeaders(Seq("Content-Type"->Seq("application/json"))), updateBody)).get
        status(updateBadResult) must equalTo(FORBIDDEN)

        // create new password
        val updateResult=route(new FakeRequest(PUT, "/users/"+userId+"/password?resetPasswordToken="+resetPasswordToken, FakeHeaders(Seq("Content-Type"->Seq("application/json"))), updateBody)).get
        status(updateResult) must equalTo(OK)
        contentType(updateResult) must beSome("application/json")
        //charset(result) must beSome("utf-8")
        contentAsString(updateResult) must equalTo("{}")
        DB.withConnection { implicit c =>
          SQL("SELECT reset_password_token FROM users ORDER BY id desc limit 1").apply().head[Option[String]]("reset_password_token")
        } must equalTo(None)

        val checkPasswordResult2=route(FakeRequest(GET, "/users?email=john%40example.com&password=p1234567")).get
        status(checkPasswordResult2) must equalTo(OK)
        contentAsString(checkPasswordResult2) must startWith("[{\"id\":"+userId)

        // can't change password w/o valid current password
        val changeBody="p7654321"
        val changeBadResult=route(new FakeRequest(PUT, "/users/"+userId+"/password?currentPassword=111", FakeHeaders(Seq("Content-Type"->Seq("application/json"))), changeBody)).get
        status(changeBadResult) must equalTo(FORBIDDEN)

        // change password
        val changeResult=route(new FakeRequest(PUT, "/users/"+userId+"/password?currentPassword=p1234567", FakeHeaders(Seq("Content-Type"->Seq("application/json"))), changeBody)).get
        status(changeResult) must equalTo(OK)
        contentType(changeResult) must beSome("application/json")
        //charset(result) must beSome("utf-8")
        contentAsString(changeResult) must equalTo("{}")
        DB.withConnection { implicit c =>
          SQL("SELECT reset_password_token FROM users ORDER BY id desc limit 1").apply().head[Option[String]]("reset_password_token")
        } must equalTo(None)

        val checkPasswordResult3=route(FakeRequest(GET, "/users?email=john%40example.com&password=p7654321")).get
        status(checkPasswordResult3) must equalTo(OK)
        contentAsString(checkPasswordResult3) must startWith("[{\"id\":"+userId)
      }
    }

//    "should create a new password \" for new user and then change it to \"" in {
//      running(TestServer(3333)) {
//        DB.withConnection { implicit c =>
//          SQL("DELETE FROM users WHERE email='john@example.com'").executeUpdate()
//        }
//
//        val createBody=Json.parse("{\"email\":\"john@example.com\", \"name\":\"John by Example\"}")
//        val result=await(WS.url("http://localhost:3333/users").post(createBody))
//        result.status must equalTo(OK)
//
//        val userId:Long=DB.withConnection { implicit c =>
//          SQL("SELECT id FROM users ORDER BY id desc limit 1").apply().head[Long]("id")
//        }
//        val resetPasswordToken:String=DB.withConnection { implicit c =>
//          SQL("SELECT reset_password_token FROM users ORDER BY id desc limit 1").apply().head[String]("reset_password_token")
//        }
//
//
//        // create new " password
//        val updateResult=await(WS.url("http://localhost:3333/users/"+userId+"/password?resetPasswordToken="+resetPasswordToken).withHeaders(("Content-Type", "application/json")).put("\"\"\"")) //
//        updateResult.status must equalTo(OK)
//        updateResult.body must equalTo("{}")
//        DB.withConnection { implicit c =>
//          SQL("SELECT reset_password_token FROM users ORDER BY id desc limit 1").apply().head[Option[String]]("reset_password_token")
//        } must equalTo(None)
//
////        val Some(checkPasswordResult2)=routeAndCall(FakeRequest(GET, "/users?email=john%40example.com&password=p1234567"))
////        status(checkPasswordResult2) must equalTo(OK)
////        contentAsString(checkPasswordResult2) must startWith("[{\"id\":"+userId)
////
////        // can't change password w/o valid current password
////        val changeBody=Json.parse("\"p7654321\"")
////        val Some(changeBadResult)=routeAndCall(new FakeRequest(PUT, "/users/"+userId+"/password?currentPassword=111", FakeHeaders(Map("Content-Type"->Seq("application/json"))), changeBody))
////        status(changeBadResult) must equalTo(FORBIDDEN)
////
////        // change password
////        val Some(changeResult)=routeAndCall(new FakeRequest(PUT, "/users/"+userId+"/password?currentPassword=p1234567", FakeHeaders(Map("Content-Type"->Seq("application/json"))), changeBody))
////        status(changeResult) must equalTo(OK)
////        contentType(changeResult) must beSome("application/json")
////        //charset(result) must beSome("utf-8")
////        contentAsString(changeResult) must equalTo("{}")
////        DB.withConnection { implicit c =>
////          SQL("SELECT reset_password_token FROM users ORDER BY id desc limit 1").apply().head[Option[String]]("reset_password_token")
////        } must equalTo(None)
////
////        val Some(checkPasswordResult3)=routeAndCall(FakeRequest(GET, "/users?email=john%40example.com&password=p7654321"))
////        status(checkPasswordResult3) must equalTo(OK)
////        contentAsString(checkPasswordResult3) must startWith("[{\"id\":"+userId)
//      }
//    }
  }
}
