package musicplayer.library.scanner

import musicplayer.library.model.Track

trait ProgressReporter[F[_]] {

  def nextTrack(track: Track): F[Unit]

}
