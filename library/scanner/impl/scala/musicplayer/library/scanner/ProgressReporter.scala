package musicplayer.library.scanner

import java.nio.file.Path

import musicplayer.library.model.metadata.TrackMetadata

trait ProgressReporter[F[_]] {

  def nextTrack(path: Path, trackMetadata: Option[TrackMetadata]): F[Unit]

}
