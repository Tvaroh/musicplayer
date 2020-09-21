package musicplayer.player

import java.nio.file.Path

trait MusicPlayer[F[_]] {

  def playTrack(path: Path): F[Unit]

  def release: F[Unit]

}
