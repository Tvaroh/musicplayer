package musicplayer.app

import java.nio.file.Paths

import cats.effect.{Blocker, Concurrent, ContextShift}
import cats.implicits._
import musicplayer.library.model.MediaFormat
import musicplayer.library.scanner.config.LibraryScannerConfig
import musicplayer.library.scanner.{LibraryScannerImpl, MetadataReaderImpl, ProgressReporter}
import musicplayer.player.MusicPlayerImpl
import tofu.lift.UnsafeExecFuture

import scala.util.Random

class Wiring[F[_]](implicit F: Concurrent[F],
                            blocker: Blocker,
                            cs: ContextShift[F],
                            unsafeExecFuture: UnsafeExecFuture[F]) {

  val app: F[Unit] = {
    val libraryScanner =
      new LibraryScannerImpl[F](LibraryScannerConfig(MediaFormat.All, followSymLinks = false))(
        new MetadataReaderImpl, ProgressReporter.empty
      )

    for {
      library <-
        Option(System.getProperty("LIBRARY_PATH"))
          .map(_.split(';').toSet.map(Paths.get(_: String)))
          .traverse(libraryScanner.scan)

      _ <- MusicPlayerImpl[F]().use { player =>
        val randomTrack = for {
          library <- library
          track <-
            if (library.tracks.nonEmpty)
              Some(library.tracks.values.toIndexedSeq(Random.nextInt(library.tracks.size)))
            else
              None
        } yield track

        randomTrack.traverse {
          player.play(_) >>
            player.events.map(println).compile.drain >>
            F.delay(Thread.currentThread().join())
        }
      }
    } yield ()
  }

}
