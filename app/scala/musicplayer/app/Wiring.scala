package musicplayer.app

import java.nio.file.Paths

import cats.effect.{Blocker, Concurrent, ContextShift}
import cats.implicits._
import musicplayer.library.config.LibraryScannerConfig
import musicplayer.library.model.MediaFormat
import musicplayer.library.{LibraryScannerImpl, MetadataReaderImpl, ProgressReporter}
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
      library <- libraryScanner.scan(Set(Paths.get("/Users/tvaroh/Temp")))
      musicPlayerResource <- MusicPlayerImpl[F]()
      _ <- musicPlayerResource.use { player =>
        val randomTrack =
          if (library.tracks.nonEmpty)
            Some(library.tracks.values.toIndexedSeq(Random.nextInt(library.tracks.size)).path)
          else
            None

        randomTrack.traverse {
          player.playTrack(_) >>
            player.events.map(println).compile.drain >>
            F.delay(Thread.currentThread().join())
        }
      }
    } yield ()
  }

}
