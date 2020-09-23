import sbt._

object Deps {

  object Scala extends Dep("2.13.3") {

    val Compiler = module("org.scala-lang", "scala-compiler", crossVersion = false)

  }

  val TaggedTypes = "io.treev" %% "tagged-types" % "3.4"

  object Cats extends Dep("2.2.0") {

    val Core = module("org.typelevel", "cats-core")
    val Effect = module("org.typelevel", "cats-effect")

  }

  val TofuCore = "ru.tinkoff" %% "tofu-core" % "0.8.0"

  val FS2IO = "co.fs2" %% "fs2-io" % "2.4.4"

  val H2 = "com.h2database" % "h2" % "1.4.200" % Test

  val VlcjInfo = "uk.co.caprica" % "vlcj-info" % "2.0.0"
  val Vlcj = "uk.co.caprica" % "vlcj" % "4.6.0"

  val MonixEval = "io.monix" %% "monix-eval" % "3.2.2"

  abstract class Dep(val version: String) {

    protected def module(organization: String,
                         artifact: String,
                         version: String = version,
                         crossVersion: Boolean = true): ModuleID =
      (if (crossVersion) organization %% artifact else organization % artifact) % version

  }

}
