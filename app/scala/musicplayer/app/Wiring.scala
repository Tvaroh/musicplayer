package musicplayer.app

import java.nio.file.Paths

import cats.effect.{Blocker, Concurrent, ContextShift}
import cats.implicits._
import musicplayer.library.config.LibraryScannerConfig
import musicplayer.library.model.MediaFormat
import musicplayer.library.{LibraryScannerImpl, MetadataReaderImpl, ProgressReporter}
import musicplayer.player.MusicPlayerImpl

import scala.util.Random

class Wiring[F[_]](implicit F: Concurrent[F],
                            blocker: Blocker,
                            cs: ContextShift[F]) {

  val app: F[Unit] = {
    val libraryScanner =
      new LibraryScannerImpl[F](LibraryScannerConfig(MediaFormat.All, followSymLinks = false))(
        new MetadataReaderImpl, ProgressReporter.empty
      )

    for {
      library <- libraryScanner.scan(Set(Paths.get("/Users/tvaroh/Temp")))
      player <- MusicPlayerImpl[F]

      allTracks = library.tracks
      randomTrack =
        if (allTracks.nonEmpty) Some(allTracks.values.toIndexedSeq(Random.nextInt(allTracks.size))) else None

      _ <-
        randomTrack
          .map(_.path)
          .traverse(player.playTrack(_) >> F.delay(Thread.currentThread().join()))
    } yield ()
  }

}
