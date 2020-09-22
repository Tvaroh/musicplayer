package musicplayer.library.scanner

import java.nio.file.Path

import cats.Applicative
import musicplayer.library.model.metadata.TrackMetadata

trait ProgressReporter[F[_]] {

  def nextTrack(path: Path, trackMetadata: Option[TrackMetadata]): F[Unit]

}

object ProgressReporter {

  def empty[F[_]](implicit F: Applicative[F]): ProgressReporter[F] =
    new Empty

  private class Empty[F[_]](implicit F: Applicative[F])
    extends ProgressReporter[F] {

    override def nextTrack(path: Path, trackMetadata: Option[TrackMetadata]): F[Unit] =
      F.unit

  }

}
