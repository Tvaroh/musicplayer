package musicplayer.library

import java.nio.file.Path

import musicplayer.library.model.TrackMetadata

trait MetadataReader[F[_]] {

  def readMetadata(filePath: Path): F[Option[TrackMetadata]]

}
