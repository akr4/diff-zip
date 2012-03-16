import sbt._
import sbt.Keys._

object MyBuild extends Build {

  val groupName = "myproject"

  def id(name: String) = "%s-%s" format(groupName, name)

  override val settings = super.settings :+ 
    (shellPrompt := { s => Project.extract(s).currentProject.id + "> " })

  val defaultSettings = Defaults.defaultSettings ++ Seq(
    version := "0.1",
    organization := "net.physalis",
    crossScalaVersions := Seq("2.9.0", "2.9.0-1", "2.9.1"),
    scalaVersion := "2.9.1",
    scalacOptions ++= Seq("-unchecked", "-deprecation")
  )

  object Dependency {

    val basic = {
      Seq(
        "org.scala-tools.time" %% "time" % "0.5",
        "org.clapper" %% "argot" % "0.3.8"
      )
    }

    val io = {
      val version = "0.3.0"
      Seq(
        "com.github.scala-incubator.io" %% "scala-io-core" % version,
        "com.github.scala-incubator.io" %% "scala-io-file" % version
      )
    }

    val logging = Seq(
      "ch.qos.logback" % "logback-classic" % "0.9.25",
      "org.codehaus.groovy" % "groovy" % "1.8.0",
      "org.slf4j" % "slf4j-api" % "1.6.2",
      "org.clapper" %% "grizzled-slf4j" % "0.6.6"
    )

    val test = Seq(
      "org.scalatest" %% "scalatest" % "1.6.1",
      "org.scalamock" %% "scalamock-scalatest-support" % "latest.integration"
    ).map { _ % "test" }

    val default = basic ++ io ++ logging ++ test
  }

  lazy val main = Project(groupName, file("."),
    settings = defaultSettings ++ Seq(
      libraryDependencies := Dependency.default,
      initialCommands := """
          |import scalax.io._
          |import scalax.file._
          |import org.scala_tools.time.Imports._
        """.stripMargin
    )
  )
}

