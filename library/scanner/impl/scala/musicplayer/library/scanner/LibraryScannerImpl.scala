package musicplayer.library.scanner

import java.nio.file.{Files, LinkOption, Path}

import cats.effect.{Blocker, Concurrent, ContextShift}
import cats.implicits._
import fs2._
import musicplayer.library.model.MediaFormat
import musicplayer.library.model.metadata.TrackMetadata
import musicplayer.library.scanner.config.LibraryScannerConfig

class LibraryScannerImpl[F[_]](config: LibraryScannerConfig)
                              (metadataReader: MetadataReader[F])
                              (implicit F: Concurrent[F],
                                        blocker: Blocker,
                                        cs: ContextShift[F])
  extends LibraryScanner[F] {

  override def scan(paths: Set[Path]): Stream[F, (Path, TrackMetadata)] =
    paths.map(scanPath).reduce(_ ++ _)

  private def scanPath(path: Path): Stream[F, (Path, TrackMetadata)] =
    fs2.io.file.walk(blocker, path)
      .filterNot(_.getFileName.toString.startsWith("."))
      .filter { path =>
        if (config.followSymLinks) Files.isRegularFile(path) else Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
      }
      .mapAsync(1) { filePath =>
        MediaFormat.fromFilename(filePath.getFileName.toString)
          .filter(config.supportedFormats.contains)
          .traverse(_ => metadataReader.readMetadata(filePath).tupleLeft(filePath))
      }
      .flattenOption
      .collect { case (path, Some(metadata)) => (path, metadata) }

}
