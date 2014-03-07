import play.Project._
import sbt._
import Keys._

object ApplicationBuild extends Build {

    val appName         = "demo-website-with-play"
    val appVersion      = "0.1.0"

  val appDependencies = Seq(
    jdbc,
    anorm,
    "org.postgresql" % "postgresql" % "9.3-1100-jdbc4",
    "com.typesafe" %% "play-plugins-mailer" % "2.2.0",
    "com.lambdaworks" % "scrypt" % "1.4.0",
    "commons-io" % "commons-io" % "2.3",
    "commons-codec" % "commons-codec" % "1.6",
    "org.mockito" % "mockito-all" % "1.9.0"
  )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      
    )

}
