import sbt._, Keys._

name := "musicplayer"
organization in ThisBuild := "musicplayer"
version in ThisBuild := "0.1-SNAPSHOT"
sourcesInBase := false
Settings.sharedSettings

val utilModel =
  project.in(file("util/model"))
    .settings(Settings.sharedSettings)
    .settings(
      name := "util-model",
      libraryDependencies ++= Seq(
        Deps.TaggedTypes,
        Deps.Cats.Core
      )
    )
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
    .dependsOn(utilModel)

val libraryScannerModel =
  project.in(file("library/scanner/model"))
    .settings(Settings.sharedSettings)
    .settings(
      name := "library-scanner-model",
    )
    .dependsOn(utilModel)
val libraryScannerApi =
  project.in(file("library/scanner/api"))
    .settings(Settings.sharedSettings)
    .settings(
      name := "library-scanner-api",
      libraryDependencies ++= Seq(
        Deps.FS2IO
      )
    )
    .dependsOn(libraryScannerModel, libraryModel)
val libraryScannerImpl =
  project.in(file("library/scanner/impl"))
    .settings(Settings.sharedSettings)
    .settings(
      name := "library-scanner-impl",
      libraryDependencies ++= Seq(
        Deps.VlcjInfo
      )
    )
    .dependsOn(libraryScannerApi, utilEffect)

val libraryDbApi =
  project.in(file("library/db/api"))
    .settings(Settings.sharedSettings)
    .settings(
      name := "library-db-api"
    )
    .dependsOn(libraryModel)
val libraryDbImpl =
  project.in(file("library/db/impl"))
    .settings(Settings.sharedSettings)
    .settings(
      name := "library-db-impl",
      libraryDependencies ++= Seq(
        Deps.H2
      )
    )
    .dependsOn(libraryDbApi, utilEffect)

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
    .dependsOn(playerApi, libraryScannerApi, utilEffect)

val app =
  project.in(file("app"))
    .settings(Settings.sharedSettings)
    .settings(
      name := "app",
      libraryDependencies ++= Seq(
        Deps.MonixEval
      )
    )
    .dependsOn(libraryScannerImpl, libraryDbImpl, playerImpl)
