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
        Deps.Cats.Effect,
        Deps.TofuCore
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
val libraryApi =
  project.in(file("library/api"))
    .settings(Settings.sharedSettings)
    .settings(
      name := "library-api"
    )
    .dependsOn(libraryModel)
val libraryImpl =
  project.in(file("library/impl"))
    .settings(Settings.sharedSettings)
    .settings(
      name := "library-impl",
      libraryDependencies ++= Seq(
        Deps.FS2IO,
        Deps.VlcjInfo
      )
    )
    .dependsOn(libraryApi, utilEffect)

val playerModel =
  project.in(file("player/model"))
    .settings(Settings.sharedSettings)
    .settings(
      name := "player-model"
    )
    .dependsOn(libraryModel)
val playerApi =
  project.in(file("player/api"))
    .settings(Settings.sharedSettings)
    .settings(
      name := "player-api",
      libraryDependencies ++= Seq(
        Deps.FS2IO
      )
    )
    .dependsOn(playerModel)
val playerImpl =
  project.in(file("player/impl"))
    .settings(Settings.sharedSettings)
    .settings(
      name := "player-impl",
      libraryDependencies ++= Seq(
        Deps.Vlcj
      )
    )
    .dependsOn(playerApi, libraryApi, utilEffect)

val app =
  project.in(file("app"))
    .settings(Settings.sharedSettings)
    .settings(
      name := "app",
      libraryDependencies ++= Seq(
        Deps.MonixEval
      )
    )
    .dependsOn(libraryImpl, playerImpl)
