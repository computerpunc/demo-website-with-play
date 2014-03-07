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

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import anorm.~
import play.api.Play.current

/**
 * Created with IntelliJ IDEA.
 * User: Ram Hardy
 * Date: 6/19/12
 * Time: 3:45 PM
 */

case class App(id:Pk[Long], userId:Long, accessToken:String)

object App {

  val app={
    get[Pk[Long]]("id")~get[Long]("user_id")~get[String]("access_token") map {
      case id~user_id~access_token=>App(id, user_id, access_token)
    }
  }

  def findById(appId:Long):Option[App]=DB.withConnection { implicit c =>
    SQL("SELECT * FROM apps WHERE id={id}").on('id->appId).as(app*).headOption
  }

  def findByUserId(userId:Long):List[App]=DB.withConnection { implicit c =>
    SQL("SELECT * FROM apps WHERE user_id={user_id}").on('user_id->userId).as(app*)
  }

  def register(appId:Long, userId:Long, accessToken:String)={
    DB.withConnection { implicit c =>
      SQL("INSERT INTO apps (id, user_id, access_token, time_created) VALUES ({id}, {user_id}, {access_token}, NOW())").on('id->appId, 'user_id->userId, 'access_token->accessToken).executeInsert()
    }
    App.findById(appId)
  }
}