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

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import scala.Some
import play.api.test.{FakeHeaders, FakeRequest, FakeApplication}
import play.api.mvc._
import scala.Some
import play.api.libs.json.{JsNumber, JsString, Json}
import play.api.test.FakeHeaders
import play.api.test.FakeApplication
import scala.Some
import play.api.mvc.AsyncResult
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart
import java.io.{FileOutputStream, FileWriter}
import org.apache.commons.codec.binary.Base64

/**
 * Created with IntelliJ IDEA.
 * User: Ram Hardy
 * Date: 6/22/12
 * Time: 11:51 AM
 */

class AppsSpec extends Specification {
  val pushCert_apnsd="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"
  val pushCert_apns="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"

  "Server with apps" should {
    "have an empty test to compile" in {
      running(FakeApplication()) {
        true must beTrue
      }
    }
//    "get apps of user" in {
//      running(FakeApplication()) {
//        val result=route(FakeRequest(GET, "/apps?userId=1&accessToken=at12345")).get
//
//        status(result) must equalTo(OK)
//        contentType(result) must beSome("application/json")
//        //charset(result) must beSome("utf-8")
//        contentAsString(result) must equalTo("[{\"id\":1,\"uuid\":\"unittest\",\"name\":\"Unit Test\",\"downloadUrl\":\"http://itunes.apple.com/us/app/angry-birds/id343200656?mt=8\",\"imageUrl\":\"http://www.foorocket.com/public/images/functional-app.png\"},{\"id\":3,\"uuid\":\"functionalapp\",\"name\":\"Functional App\",\"downloadUrl\":\"http://itunes.apple.com/us/app/angry-birds/id343200656?mt=8\",\"imageUrl\":\"http://www.foorocket.com/public/images/functional-app.png\"}]")
//      }
//    }
//
//    "not get apps when no access token" in {
//      running(FakeApplication()) {
//        val result=route(FakeRequest(GET, "/apps?userId=1")).get
//
//        status(result) must equalTo(BAD_REQUEST)
//      }
//    }
//
//    "not get apps when no valid access token" in {
//      running(FakeApplication()) {
//        val result=route(FakeRequest(GET, "/apps?userId=1&accessToken=at11111")).get
//
//        status(result) must equalTo(FORBIDDEN)
//        contentType(result) must beSome("application/json")
//        //charset(result) must beSome("utf-8")
//        contentAsString(result) must equalTo("{\"error\":\"credentials missmatch\"}")
//      }
//    }
//
//    "get app of user" in {
//      running(FakeApplication()) {
//        val result=route(FakeRequest(GET, "/apps/1?userId=1&accessToken=at12345")).get
//
//        status(result) must equalTo(OK)
//        contentType(result) must beSome("application/json")
//        //charset(result) must beSome("utf-8")
//        contentAsString(result) must equalTo("{\"id\":1,\"uuid\":\"unittest\",\"name\":\"Unit Test\",\"downloadUrl\":\"http://itunes.apple.com/us/app/angry-birds/id343200656?mt=8\",\"imageUrl\":\"http://www.foorocket.com/public/images/functional-app.png\"}")
//      }
//    }
//
//    "not get app of user when not owner" in {
//      running(FakeApplication()) {
//        val result=route(FakeRequest(GET, "/apps/2?userId=1&accessToken=at12345")).get
//
//        status(result) must equalTo(FORBIDDEN)
//        contentType(result) must beSome("application/json")
//        //charset(result) must beSome("utf-8")
//        contentAsString(result) must equalTo("{\"error\":\"credentials missmatch\"}")
//      }
//    }
//
//    "create and update app of user" in {
//      running(FakeApplication()) {
//        val postBody=Json.parse("{\"name\":\"My New App\",\"downloadUrl\":\"http://www.pics.com/mypic.jpg\"}")
//        val result1=route(new FakeRequest(POST, "/apps?userId=2&accessToken=at54321", FakeHeaders(Seq(play.api.http.HeaderNames.CONTENT_TYPE->Seq("application/json"))), postBody)).get
//        status(result1) must equalTo(OK)
//        contentType(result1) must beSome("application/json")
//        //charset(result) must beSome("utf-8")
//
//        val json1=Json.parse(contentAsString(result1))
//        (json1\"name").as[String] must equalTo("My New App")
//        (json1\"downloadUrl").as[String] must equalTo("http://www.pics.com/mypic.jpg")
//        (json1\"id").asOpt[Int] mustNotEqual(None)
//        (json1\"uuid").asOpt[String] mustNotEqual(None)
//        (json1\"imageUrl").asOpt[String] must equalTo(None)
//
//        val appId=(json1\"id").as[Long]
//        val uuid=(json1\"uuid").as[String]
//        val result2=route(FakeRequest(GET, "/apps/"+appId+"?userId=2&accessToken=at54321")).get
//        status(result2) must equalTo(OK)
//        contentType(result2) must beSome("application/json")
//        //charset(result2) must beSome("utf-8")
//
//        val json2=Json.parse(contentAsString(result2))
//        (json2\"name").as[String] must equalTo("My New App")
//        (json2\"downloadUrl").as[String] must equalTo("http://www.pics.com/mypic.jpg")
//        (json2\"id").as[Long] must equalTo(appId)
//        (json2\"uuid").as[String] must equalTo(uuid)
//        (json2\"imageUrl").asOpt[String] must equalTo(None)
//
//        val putBody=Json.parse("{\"id\":3, \"name\":\"Unit Test New\",\"uuid\":\"unittestnew\",\"imageUrl\":\"http://www.foorocket.com/public/images/functional-app123.png\",\"downloadUrl\":\"http://itunes.apple.com/us/app/angry-birds/id343200656?mt=18888\"}") // should ignore id and uuid
//        val result3=route(new FakeRequest(PUT, "/apps/"+appId+"?userId=2&accessToken=at54321", FakeHeaders(Seq(play.api.http.HeaderNames.CONTENT_TYPE->Seq("application/json"))), putBody)).get
//        status(result3) must equalTo(OK)
//        contentType(result3) must beSome("application/json")
//        //charset(result3) must beSome("utf-8")
//
//        val json3=Json.parse(contentAsString(result3))
//        (json3\"name").as[String] must equalTo("Unit Test New")
//        (json3\"downloadUrl").as[String] must equalTo("http://itunes.apple.com/us/app/angry-birds/id343200656?mt=18888")
//        (json3\"id").as[Long] must equalTo(appId)
//        (json3\"uuid").as[String] must equalTo(uuid)
//        (json3\"imageUrl").asOpt[String] must equalTo(None)
//      }
//    }
//
//    "update app of user via form" in {
//      running(FakeApplication()) {
//        val postBody=Json.parse("{\"name\":\"My New App\",\"downloadUrl\":\"http://www.pics.com/mypic.jpg\"}")
//        val result1=route(new FakeRequest(POST, "/apps?userId=2&accessToken=at54321", FakeHeaders(Seq(play.api.http.HeaderNames.CONTENT_TYPE->Seq("application/json"))), postBody)).get
//        status(result1) must equalTo(OK)
//        contentType(result1) must beSome("application/json")
//        //charset(result) must beSome("utf-8")
//
//        val json1=Json.parse(contentAsString(result1))
//        val appId=(json1\"id").as[Long]
//        val uuid=(json1\"uuid").as[String]
//
//        //val putBody=Json.parse("{\"id\":3, \"name\":\"Unit Test New\",\"uuid\":\"unittestnew\",\"imageUrl\":\"http://www.foorocket.com/public/images/functional-app123.png\",\"downloadUrl\":\"http://itunes.apple.com/us/app/angry-birds/id343200656?mt=18888\"}") // should ignore id and uuid
//        val nonFiles=Map("id"->Seq("3"), "name"->Seq("Unit Test New"), "uuid"->Seq("unittestnew"), "imageUrl"->Seq("http://www.foorocket.com/public/images/functional-app123.png"),  "downloadUrl"->Seq("http://itunes.apple.com/us/app/angry-birds/id343200656?mt=18888"))
//        val file_apns=TemporaryFile("cert_apns", "p12")
//        new FileOutputStream(file_apns.file).write(Base64.decodeBase64(pushCert_apns))
//        val file_bad_apnsd=TemporaryFile("cert_apns", "p12")
//        new FileOutputStream(file_bad_apnsd.file).write(Array[Byte]('1', '2', '3', '4'))
//
//
//        val files=Seq(FilePart("pushCert_apns", "cert_apns.p12", Some("application/x-pkcs12"), file_apns), FilePart("pushCert_apnsd", "cert_bad_apnd.p12", Some("application/x-pkcs12"), file_bad_apnsd))
//        val putBody=MultipartFormData[TemporaryFile](nonFiles, files, Seq(), Seq()) // should ignore id and uuid
//
//        val putResult=route(new FakeRequest(PUT, "/apps/"+appId+"/form?userId=2&accessToken=at54321", FakeHeaders(Seq(play.api.http.HeaderNames.CONTENT_TYPE->Seq("multipart/form-data"))), putBody)).get
//        status(putResult) must equalTo(OK)
//        contentType(putResult) must beSome("application/json")
//        //charset(result3) must beSome("utf-8")
//
//        val json2=Json.parse(contentAsString(putResult))
//        (json2\"name").as[String] must equalTo("Unit Test New")
//        (json2\"downloadUrl").as[String] must equalTo("http://itunes.apple.com/us/app/angry-birds/id343200656?mt=18888")
//        (json2\"id").as[Long] must equalTo(appId)
//        (json2\"uuid").as[String] must equalTo(uuid)
//        (json2\"imageUrl").asOpt[String] must equalTo(None)
//        (json2\"pushCert_apns"\"issuer").as[String] must equalTo("CN=Apple Worldwide Developer Relations Certification Authority, OU=Apple Worldwide Developer Relations, O=Apple Inc., C=US")
//        (json2\"pushCert_apns"\"subject").as[String] must equalTo("C=IL, CN=Apple Production IOS Push Services: com.eladhemar.PushTest, UID=com.eladhemar.PushTest")
//        (json2\"pushCert_apns"\"notValidBefore").as[Long] must equalTo(1342132140000L)
//        (json2\"pushCert_apns"\"notValidAfter").as[Long] must equalTo(1373668140000L)
//        (1000>math.abs((json2\"pushCert_apns"\"updatedOn").as[Long]-System.currentTimeMillis())) must equalTo(true)
//        (json2\"pushCert_apnsd"\"error").as[String] must equalTo("Cannot decrypt cert")
//        (1000>math.abs((json2\"pushCert_apnsd"\"updatedOn").as[Long]-System.currentTimeMillis())) must equalTo(true)
//
//      }
//    }
//
//    "update too big cert via form" in {
//      running(FakeApplication()) {
//        val postBody=Json.parse("{\"name\":\"My New App\",\"downloadUrl\":\"http://www.pics.com/mypic.jpg\"}")
//        val result1=route(new FakeRequest(POST, "/apps?userId=2&accessToken=at54321", FakeHeaders(Seq(play.api.http.HeaderNames.CONTENT_TYPE->Seq("application/json"))), postBody)).get
//        status(result1) must equalTo(OK)
//        contentType(result1) must beSome("application/json")
//        //charset(result) must beSome("utf-8")
//
//        val json1=Json.parse(contentAsString(result1))
//        val appId=(json1\"id").as[Long]
//        val uuid=(json1\"uuid").as[String]
//
//        val file_apns=TemporaryFile("cert_apns", "p12")
//        val os=new FileOutputStream(file_apns.file)
//        for (i<-1 to 1024) os.write(Array[Byte]('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'))
//
//        val files=Seq(FilePart("pushCert_apns", "cert_apns.p12", Some("application/x-pkcs12"), file_apns))
//        val putBody=MultipartFormData[TemporaryFile](Map(), files, Seq(), Seq()) // should ignore id and uuid
//
//        val putResult=route(new FakeRequest(PUT, "/apps/"+appId+"/form?userId=2&accessToken=at54321", FakeHeaders(Seq(play.api.http.HeaderNames.CONTENT_TYPE->Seq("multipart/form-data"))), putBody)).get
//        status(putResult) must equalTo(OK)
//        contentType(putResult) must beSome("application/json")
//        //charset(result3) must beSome("utf-8")
//
//        val json2=Json.parse(contentAsString(putResult))
//        (json2\"id").as[Long] must equalTo(appId)
//        (json2\"uuid").as[String] must equalTo(uuid)
//        (json2\"pushCert_apns").asOpt[String] must equalTo(None)
//      }
//    }
//    "get user when presented with a valid email and password" in {
//      running(FakeApplication()) {
//        val Some(result)=routeAndCall(FakeRequest(GET, "/users?email=ram%40hardy.co.il&password=h12345"))
//
//        status(result) must equalTo(OK)
//        contentType(result) must beSome("application/json")
//        //charset(result) must beSome("utf-8")
//        contentAsString(result) must equalTo("[{\"id\":1,\"name\":\"Ram Hardy\",\"email\":\"ram@example.com\",\"accessToken\":\"at12345\"}]")
//      }
//    }
//
//    "shouldn't get acces token when presented with a invalid password" in {
//      running(FakeApplication()) {
//        val Some(result)=routeAndCall(FakeRequest(POST, "/users/1/accessToken?password=h"))
//
//        status(result) must equalTo(FORBIDDEN)
//        contentType(result) must beSome("application/json")
//        //charset(result) must beSome("utf-8")
//        contentAsString(result) must equalTo("{\"error\":\"password missmatch\"}")
//      }
//    }
  }
}
