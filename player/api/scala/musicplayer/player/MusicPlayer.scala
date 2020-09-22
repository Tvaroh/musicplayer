package musicplayer.player

import java.nio.file.Path

import fs2.Stream
import musicplayer.player.model.PlayerEvent

trait MusicPlayer[F[_]] {

  def events: Stream[F, PlayerEvent]

  def playTrack(path: Path): F[Unit]

  def release: F[Unit]

}
