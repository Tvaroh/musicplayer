import sbt._

object Deps {

  object Scala extends Dep("2.13.3") {

    val Compiler = module("org.scala-lang", "scala-compiler", crossVersion = false)

  }

  abstract class Dep(val version: String) {

    protected def module(organization: String,
                         artifact: String,
                         version: String = version,
                         crossVersion: Boolean = true): ModuleID =
      (if (crossVersion) organization %% artifact else organization % artifact) % version

  }

}
