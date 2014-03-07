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
 * Date: 6/11/12
 * Time: 3:58 PM
 */

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import models.User
import play.api.db.DB
import anorm._
import play.api.test.FakeApplication
import scala.Some
import play.api.Play.current

class UserSpec extends Specification {
  "User model" should {
    "be retrieved by id" in {
      running(FakeApplication()) {
        val Some(ram)=User.findById(1L)
        ram.name must equalTo("Ram Hardy")
        ram.email must equalTo("ram@example.com")
        ram.password must equalTo("$s0$e0801$UTcluMO95WiIDOWWvvdgnw==$gnxcEj/kFk1LrfT8rm3OtD8zv1TaisZksvsbsh6R6kk=")
        ram.accessToken must equalTo("at12345")
        ram.resetPasswordToken must equalTo(None)

        val Some(elad)=User.findById(2L)
        elad.name must equalTo("Elad Hemar")
        elad.email must equalTo("elad@example.com")
        elad.password must equalTo("h54321")
        elad.accessToken must equalTo("at54321")
        elad.resetPasswordToken must equalTo(None)
      }
    }
    "should not be retrieved by non existant id" in {
      running(FakeApplication()) {
        User.findById(0L) must equalTo(None)
      }
    }
    "be retrieved by email" in {
      running(FakeApplication()) {
        val Some(ram)=User.findByEmail("ram@example.com")
        ram.id.get must equalTo(1L)
        ram.name must equalTo("Ram Hardy")
        ram.email must equalTo("ram@example.com")
        ram.password must equalTo("$s0$e0801$UTcluMO95WiIDOWWvvdgnw==$gnxcEj/kFk1LrfT8rm3OtD8zv1TaisZksvsbsh6R6kk=")
        ram.accessToken must equalTo("at12345")

        val Some(elad)=User.findByEmail("elad@example.com")
        elad.id.get must equalTo(2L)
        elad.name must equalTo("Elad Hemar")
        elad.email must equalTo("elad@example.com")
        elad.password must equalTo("h54321")
        elad.accessToken must equalTo("at54321")
      }
    }
    "be retrieved by email case insensitivly" in {
      running(FakeApplication()) {
        val Some(ram)=User.findByEmail("Ram@Example.com")
        ram.id.get must equalTo(1L)
        ram.email must equalTo("ram@example.com")

        val Some(elad)=User.findByEmail("Elad@Example.com")
        elad.id.get must equalTo(2L)
        elad.email must equalTo("elad@example.com")
      }
    }
    "should not be retrieved by non existant id" in {
      running(FakeApplication()) {
        User.findByEmail("idjx8374mcjdh@jfiejcie8.839.837") must equalTo(None)
      }
    }
    "should be created and updated" in {
      running(FakeApplication()) {
        DB.withConnection { implicit c =>
          SQL("DELETE FROM users WHERE email='john@example.com'").executeUpdate()
          SQL("DELETE FROM users WHERE email='john1@example.com'").executeUpdate()
        }
        val Some(userId)=User.create("john@example.com", "John by Example", "p12345", "johnAT12345", "johnRPT12345")

        val Some(user1)=User.findById(userId)
        user1.id.get must  equalTo(userId)
        user1.name must equalTo("John by Example")
        user1.email must equalTo("john@example.com")
        user1.checkPassword("p12345") must equalTo(true)
        user1.checkPassword("p1234") must equalTo(false)
        user1.accessToken must equalTo("johnAT12345")
        user1.resetPasswordToken must equalTo(Some("johnRPT12345"))

        User.updateEmail(userId, "john1@example.com")
        User.updatePassword(userId, "pp12345")
        User.updateName(userId, "John Example")
        User.updateResetPasswordToken(userId, Some("rt12345"))

        val Some(user2)=User.findById(userId)
        user2.id.get must equalTo(userId)
        user2.name must equalTo("John Example")
        user2.email must equalTo("john1@example.com")
        user2.checkPassword("pp12345") must equalTo(true)
        user2.checkPassword("p12345") must equalTo(false)
        user2.accessToken must equalTo("johnAT12345")
        user2.resetPasswordToken must equalTo(Some("rt12345"))
      }
    }
    "should be created and updated with case insensitive in email" in {
      running(FakeApplication()) {
        DB.withConnection { implicit c =>
          SQL("DELETE FROM users WHERE email='john@example.com'").executeUpdate()
          SQL("DELETE FROM users WHERE email='john1@example.com'").executeUpdate()
        }
        val Some(userId)=User.create("John@Example.com", "John by Example", "p12345", "johnAT12345", "johnRPT12345")

        val Some(user1)=User.findById(userId)
        user1.id.get must  equalTo(userId)
        user1.email must equalTo("john@example.com")

        User.updateEmail(userId, "John1@Example.com")

        val Some(user2)=User.findById(userId)
        user2.id.get must equalTo(userId)
        user2.email must equalTo("john1@example.com")
      }
    }
  }
}
