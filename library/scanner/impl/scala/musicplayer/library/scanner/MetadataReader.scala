package musicplayer.library.scanner

import java.nio.file.Path

import musicplayer.library.model.metadata.TrackMetadata

trait MetadataReader[F[_]] {

  def readMetadata(filePath: Path): F[Option[TrackMetadata]]

}
