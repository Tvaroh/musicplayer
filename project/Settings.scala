import sbt.Keys._
import sbt._

object Settings {

  val sharedSettings: Seq[Setting[_]] = Seq(
    scalaVersion := Deps.Scala.version,
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-explaintypes",
      "-feature",
      "-unchecked",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-Wdead-code",
      "-Werror",
      "-Wnumeric-widen",
      "-Wunused:imports",
      "-Wunused:linted",
      "-Wunused:locals",
      "-Wunused:params",
      "-Wunused:privates",
      "-Wvalue-discard",
      "-Xcheckinit",
      "-Xlint",
      "-Xsource:2.13"
    ),

    scalaSource in Compile := baseDirectory.value / "scala",
    resourceDirectory in Compile := baseDirectory.value / "resources",

    scalaSource in Test := baseDirectory.value / "test",
    resourceDirectory in Test := baseDirectory.value / "test-resources",

    sources in (Compile,doc) := Seq.empty,
    publishArtifact in (Compile, packageDoc) := false,

    libraryDependencies ++= Seq(
      compilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)
    ),

    resolvers += Resolver.sonatypeRepo("releases")
  )

}
