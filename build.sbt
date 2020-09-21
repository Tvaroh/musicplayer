import sbt._, Keys._

name := "musicplayer"
organization in ThisBuild := "musicplayer"
version in ThisBuild := "0.1-SNAPSHOT"
sourcesInBase := false
Settings.sharedSettings

lazy val utilEffect =
  project.in(file("util/effect"))
    .settings(Settings.sharedSettings)
    .settings(
      name := "util-effect",
      libraryDependencies ++= Seq(
        Deps.Cats.Effect
      )
    )

val libraryModel =
  project.in(file("library/model"))
    .settings(Settings.sharedSettings)
    .settings(
      name := "library-model",
      libraryDependencies ++= Seq(
        Deps.TaggedTypes,
        Deps.Cats.Core
      )
    )
val libraryScannerApi =
  project.in(file("library/scanner/api"))
    .settings(Settings.sharedSettings)
    .settings(
      name := "library-scanner-api"
    )
    .dependsOn(libraryModel)
val libraryScannerImpl =
  project.in(file("library/scanner/impl"))
    .settings(Settings.sharedSettings)
    .settings(
      name := "library-scanner-impl",
      libraryDependencies ++= Seq(
        Deps.Cats.Effect,
        Deps.FS2IO,
        Deps.VlcjInfo
      )
    )
    .dependsOn(libraryScannerApi, utilEffect)
