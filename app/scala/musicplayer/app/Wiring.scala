package musicplayer.app

import java.nio.file.Paths

import cats.effect.Concurrent
import cats.implicits._
import musicplayer.library.config.LibraryConfig
import musicplayer.library.model.{LibraryMetadata, MediaFormat}
import musicplayer.library.{LibraryScannerImpl, LibraryWatcherImpl, MetadataReaderImpl}
import musicplayer.player.MusicPlayerImpl
import musicplayer.util.effect.FileWalk
import tofu.Blocks
import tofu.lift.UnsafeExecFuture

import scala.util.Random

class Wiring[F[_]](implicit F: Concurrent[F],
                            blocks: Blocks[F],
                            fileWalk: FileWalk[F],
                            unsafeExecFuture: UnsafeExecFuture[F]) {

  val app: F[Unit] = {
    val config = LibraryConfig(MediaFormat.All, followSymLinks = false)
    val libraryScanner = new LibraryScannerImpl[F](config)(new MetadataReaderImpl)

    val libraryPaths =
      Option(System.getProperty("LIBRARY_PATH"))
        .map(_.split(';').toSet.map(Paths.get(_: String)))
        .getOrElse(Set.empty)

    LibraryWatcherImpl(config.followSymLinks, libraryPaths).use { libraryWatcher =>
      for {
        libraryMetadata <-
          libraryScanner.scan(libraryPaths)
            .evalTap { case (path, _) => F.delay(println(path)) }
            .compile.toVector
            .map(LibraryMetadata(_))

        _ <- MusicPlayerImpl[F]().use { player =>
          val randomTrack =
            if (libraryMetadata.tracks.nonEmpty)
              Some(libraryMetadata.tracks.values.toIndexedSeq(Random.nextInt(libraryMetadata.tracks.size)))
            else
              None

          F.start(libraryWatcher.events.map(println).compile.drain) >>
            randomTrack.traverse {
              player.play(_) >>
                player.events.map(println).compile.drain >>
                F.delay(Thread.currentThread().join())
            }
        }
      } yield ()
    }
  }

}
