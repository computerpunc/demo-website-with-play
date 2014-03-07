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
import play.api.test.{FakeRequest, FakeApplication}
import play.api.test.FakeApplication
import scala.Some
import play.api.mvc._

/**
 * Created with IntelliJ IDEA.
 * User: Ram Hardy
 * Date: 7/7/12
 * Time: 8:57 PM
 */

class MiscSpec extends Specification {
  "Server" should {
    "get redirected to https for Application" in {
      running(FakeApplication()) {
        val result=route(FakeRequest(GET, "/dashboard")).get
        status(result) must equalTo(OK)

        val resultRedirect=route(FakeRequest(GET, "/dashboard").withHeaders(("x-forwarded-proto", "http"))).get
        status(resultRedirect) must equalTo(SEE_OTHER)
        headers(resultRedirect).get("Location").get must equalTo("http://localhost:9001/dashboard")

        val resultWithNoRedirect=route(FakeRequest(GET, "/dashboard").withHeaders(("x-forwarded-proto", "https"))).get
        status(resultWithNoRedirect) must equalTo(OK)
        header("Strict-Transport-Security", resultWithNoRedirect).get must equalTo("max-age=31536000")
//        val h2=header("Content-Length", resultRedirect)
//        h("Strict-Transport-Security") must equalTo("max-age=31536000")
      }
    }

    "get redirected to https for Core" in {
      running(FakeApplication()) {
        val result=route(FakeRequest(GET, "/users/1?accessToken=at12345")).get
        status(result) must equalTo(OK)

        val resultRedirect=route(FakeRequest(GET, "/users/1?accessToken=at12345").withHeaders(("x-forwarded-proto", "http"))).get
        status(resultRedirect) must equalTo(SEE_OTHER)
        headers(resultRedirect).get("Location").get must equalTo("http://localhost:9001/users/1?accessToken=at12345")

        val resultWithNoRedirect=route(FakeRequest(GET, "/users/1?accessToken=at12345").withHeaders(("x-forwarded-proto", "https"))).get
        status(resultWithNoRedirect) must equalTo(OK)
      }
    }

    "get login page when not login in ard" in {
      running(FakeApplication()) {
        val redirect=route(FakeRequest(GET, "/ard/dashboard#/apps")).get
        status(redirect) must equalTo(SEE_OTHER)
        headers(redirect).get("Location").get must equalTo("/login?redirect=%2Fdashboard%23%2Fapps")

        val resultRedirect=route(FakeRequest(GET, "/ard/dashboard#/apps").withCookies(Cookie("userId", "1"), Cookie("accessToken","12345"))).get
        status(resultRedirect) must equalTo(SEE_OTHER)
        headers(resultRedirect).get("Location").get must equalTo("/dashboard#/apps")

//        val Some(resultWithNoRedirect)=routeAndCall(FakeRequest(GET, "/dashboard").withHeaders(("x-forwarded-proto", "https")))
//        status(resultWithNoRedirect) must equalTo(OK)
      }
    }

    "get randomly redirected" in {
      running(FakeApplication()) {
        val redirect=route(FakeRequest(GET, "/rrd?ref=53&base=index&ext=html&r=1-5-2-4-3")).get

        status(redirect) must equalTo(SEE_OTHER)
        headers(redirect).get("Location").get must beMatching("""http://localhost:9002/index[1-5]\.html\?ref=53index[1-5]""".r)
      }
    }
  }
}
