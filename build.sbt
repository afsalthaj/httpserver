import sbt._
import sbt.Keys._

name := "server"

version := "0.1"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1",
  "org.specs2"    %% "specs2-scalacheck" % "4.0.2",
  "org.specs2"    %% "specs2-core" % "4.0.2"
)

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

// Auto run scalastyle on compile
compileScalastyle := org.scalastyle.sbt.ScalastylePlugin.autoImport.scalastyle.in(Compile).toTask(" q").value // " q" is the quiet flag

(compile in Compile) := ((compile in Compile) dependsOn compileScalastyle ).value

org.scalastyle.sbt.ScalastylePlugin.autoImport.scalastyleConfig := file("project/scalastyle-config.xml") // Needed as intelij looks here
