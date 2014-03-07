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

package unit

/**
 * Created with IntelliJ IDEA.
 * User: Ram Hardy
 * Date: 6/19/12
 * Time: 4:01 PM
 */


import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.Play.current
import models.App
import play.api.db.DB
import anorm._
import play.api.test.FakeApplication
import scala.Some

class AppSpec extends Specification {
  "App model" should {
    "be retrieved by id" in {
      running(FakeApplication()) {
        val Some(app1)=App.findById(1L)

        app1.id.get must equalTo(1L)
        app1.userId must equalTo(1L)
        app1.accessToken must equalTo("2d295da7d88d4ef7b1d2e9be1bf80c37")

        val Some(app2)=App.findById(4L)

        app2.id.get must equalTo(4L)
        app2.userId must equalTo(2L)
        app2.accessToken must equalTo("2d295da7d88d4ef7b1d2e9be1bf80c37")
      }
    }
    "should not be retrieved by non existant id" in {
      running(FakeApplication()) {
        App.findById(0L) must equalTo(None)
      }
    }
    "be retrieved by user-id" in {
      running(FakeApplication()) {
        val apps=App.findByUserId(1L)
        apps.length must equalTo(2)
        val i=apps.iterator

        val app1=i.next
        app1.id.get must equalTo(1L)
        app1.userId must equalTo(1L)

        val app2=i.next
        app2.id.get must equalTo(3L)
        app2.userId must equalTo(1L)
      }
    }
    "should not be retrieved by non existant user-id" in {
      running(FakeApplication()) {
        App.findByUserId(0L).headOption must equalTo(None)
      }
    }

    "be registered" in {
      running(FakeApplication()) {
        DB.withConnection { implicit c =>
          SQL("DELETE FROM apps WHERE id=7").executeUpdate()
        }
        val Some(app)=App.register(7L,2L, "2d295da7d88d4ef7b1d2e9be1bf80c37")
        app.id.get must equalTo(7L)
        app.userId must equalTo(2L)
        app.accessToken must equalTo("2d295da7d88d4ef7b1d2e9be1bf80c37")
      }
    }
  }
}
