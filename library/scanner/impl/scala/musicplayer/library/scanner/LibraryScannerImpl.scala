package musicplayer.library.scanner

import java.nio.file.{Files, LinkOption, Path}

import cats.effect.{Blocker, Concurrent, ContextShift}
import cats.implicits._
import musicplayer.library.model.metadata.TrackMetadata
import musicplayer.library.model.{Library, MediaFormat}
import musicplayer.library.scanner.config.LibraryScannerConfig

class LibraryScannerImpl[F[_]](config: LibraryScannerConfig)
                              (metadataReaders: Map[MediaFormat, MetadataReader[F]],
                               progressReporter: ProgressReporter[F])
                              (implicit F: Concurrent[F],
                                        blocker: Blocker,
                                        cs: ContextShift[F])
  extends LibraryScanner[F] {

  override def scan(paths: Set[Path]): F[Library] =
    paths.toVector.traverse(scanPath)
      .map(_.flatten)
      .map(Library(_))

  private def scanPath(path: Path): F[Vector[(Path, TrackMetadata)]] =
    fs2.io.file.walk(blocker, path)
      .filter { file =>
        if (config.followSymLinks) Files.isRegularFile(file) else Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)
      }
      .mapAsync(1) { filePath =>
        MediaFormat.fromFilename(filePath.getFileName.toString)
          .flatMap(metadataReaders.get)
          .traverse(_.readMetadata(filePath).tupleLeft(filePath))
          .flatTap(_.traverse((progressReporter.nextTrack _).tupled))
      }
      .flattenOption
      .compile
      .toVector
      .map(_.collect { case (path, Some(metadata)) => (path, metadata) })

}
