package musicplayer.library.scanner

import java.nio.file.{Files, LinkOption, Path}

import cats.effect.{Blocker, Concurrent, ContextShift}
import cats.implicits._
import musicplayer.library.scanner.config.LibraryScannerConfig
import musicplayer.library.model.metadata.TrackMetadata
import musicplayer.library.model.{Library, MediaFormat}

class LibraryScannerImpl[F[_]](config: LibraryScannerConfig)
                              (metadataReader: MetadataReader[F],
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
      .filterNot(_.getFileName.toString.startsWith("."))
      .filter { path =>
        if (config.followSymLinks) Files.isRegularFile(path) else Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
      }
      .mapAsync(1) { filePath =>
        MediaFormat.fromFilename(filePath.getFileName.toString)
          .filter(config.supportedFormats.contains)
          .traverse(_ => metadataReader.readMetadata(filePath).tupleLeft(filePath))
          .flatTap(_.traverse((progressReporter.nextTrack _).tupled))
      }
      .flattenOption
      .compile
      .toVector
      .map(_.collect { case (path, Some(metadata)) => (path, metadata) })

}
