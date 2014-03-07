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

package models

import play.api.db.DB
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import com.lambdaworks.crypto.SCryptUtil


/**
 * Created with IntelliJ IDEA.
 * User: Ram Hardy
 * Date: 6/8/12
 * Time: 4:02 PM
 */

case class User(id:Pk[Long], name:String, email:String, password:String, accessToken:String, resetPasswordToken:Option[String]) {
  def checkPassword(password:String):Boolean={
    try {
      SCryptUtil.check(password, this.password)
    } catch {
      case _=> false
    }
  }
}

object User {

  val user={
    get[Pk[Long]]("id")~get[String]("name")~get[String]("email")~get[String]("password")~get[String]("access_token")~get[Option[String]]("reset_password_token") map {
      case id~name~email~password~access_token~reset_password_token=> User(id, name, email, password, access_token, reset_password_token)
    }
  }

  def create(email:String, name:String, password:String, accessToken:String, resetPasswordToken:String):Option[Long]=DB.withConnection { implicit c=>
    val hash=SCryptUtil.scrypt(password, 16384, 8, 1)
    SQL("INSERT INTO users (name, email, password, access_token, reset_password_token, time_created) VALUES ({name}, {email}, {password}, {access_token}, {reset_password_token}, NOW())").on('email->email.toLowerCase, 'name->name, 'password->hash, 'access_token->accessToken, 'reset_password_token->resetPasswordToken).executeInsert()
  }

  def findById(userId:Long):Option[User]=DB.withConnection { implicit c=>
      SQL("SELECT * FROM users WHERE id={id}").on('id->userId).as(user*).headOption;
  }

  def findByEmail(email:String):Option[User]=DB.withConnection { implicit c=>
    SQL("SELECT * FROM users WHERE email={email}").on('email->email.toLowerCase).as(user*).headOption;
  }

  def updateEmail(userId:Long, email:String)=DB.withConnection { implicit c=>
    SQL("UPDATE users SET email={email} WHERE id={id}").on('email->email.toLowerCase, 'id->userId).executeUpdate()
  }

  def updatePassword(userId:Long, password:String)=DB.withConnection { implicit c=>
    val hash=SCryptUtil.scrypt(password, 16384, 8, 1)
    SQL("UPDATE users SET password={password} WHERE id={id}").on('password->hash, 'id->userId).executeUpdate()
  }

  def updateName(userId:Long, name:String)=DB.withConnection { implicit c=>
    SQL("UPDATE users SET name={name} WHERE id={id}").on('name->name, 'id->userId).executeUpdate()
  }

  def updateResetPasswordToken(userId:Long, tokenOption:Option[String])=DB.withConnection { implicit c=>
    tokenOption match {
      case Some(token)=> SQL("UPDATE users SET reset_password_token={token} WHERE id={id}").on('token->token, 'id->userId).executeUpdate()
      case None=> SQL("UPDATE users SET reset_password_token=NULL WHERE id={id}").on('id->userId).executeUpdate()
    }
  }
}
