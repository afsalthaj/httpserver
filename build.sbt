import sbt._
import sbt.Keys._

name := "StringCompression"

version := "0.1"

scalaVersion := "2.12.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1"

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

// Auto run scalastyle on compile
compileScalastyle := org.scalastyle.sbt.ScalastylePlugin.autoImport.scalastyle.in(Compile).toTask(" q").value // " q" is the quiet flag

(compile in Compile) := ((compile in Compile) dependsOn compileScalastyle ).value

org.scalastyle.sbt.ScalastylePlugin.autoImport.scalastyleConfig := file("project/scalastyle-config.xml") // Needed as intelij looks here
