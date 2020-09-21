package musicplayer.app

import java.nio.file.Paths

import cats.effect.{Blocker, Concurrent, ContextShift}
import cats.implicits._
import musicplayer.library.{LibraryScannerImpl, MetadataReaderImpl, ProgressReporter}
import musicplayer.library.config.LibraryScannerConfig
import musicplayer.library.model.MediaFormat

class Wiring[F[_]](implicit F: Concurrent[F],
                            blocker: Blocker,
                            cs: ContextShift[F]) {

  val app: F[Unit] = {
    val libraryScanner =
      new LibraryScannerImpl[F](LibraryScannerConfig(MediaFormat.All, followSymLinks = false))(
        new MetadataReaderImpl, ProgressReporter.empty
      )

    libraryScanner.scan(Set(Paths.get("/Users/tvaroh/Temp"))).map(println)
  }

}
