import sbt._
import Keys._
import sbtrelease._

object BuildSettings {
  val buildOrganization = "com.github.gilbertw1"
  val buildVersion      = "0.1.4"
  val buildScalaVersion = "2.11.8"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion,
    publishMavenStyle := true)
}

object Resolvers {
  val typesafeRepo = "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
}

object Dependencies {
  val akkaVersion = "2.4.4"
  val sprayVersion = "1.3.3"

  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion

  val scalaAsync = "org.scala-lang.modules" %% "scala-async" % "0.9.5"
  val dispatch = "net.databinder.dispatch" %% "dispatch-core" % "0.11.3"
  val playJson = "com.typesafe.play" %% "play-json" % "2.5.3"

  val akkaDependencies = Seq(akkaActor, akkaSlf4j)
  val miscDependencies = Seq(playJson, scalaAsync, dispatch)

  val allDependencies = akkaDependencies ++ miscDependencies
}

object Twipoli extends Build {
  import Resolvers._
  import BuildSettings._
  import Defaults._

  lazy val twipoli =
    Project ("twipoli", file("."))
      .settings ( buildSettings : _* )
      .settings ( resolvers ++= Seq(typesafeRepo) )
      .settings ( libraryDependencies ++= Dependencies.allDependencies )
      .settings ( scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xlint", "-Xfatal-warnings", "-feature") )

}
