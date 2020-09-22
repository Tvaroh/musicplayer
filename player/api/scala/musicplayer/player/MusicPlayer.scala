package musicplayer.player

import fs2.Stream
import musicplayer.library.model.Track
import musicplayer.player.model.PlayerEvent

trait MusicPlayer[F[_]] {

  def events: Stream[F, PlayerEvent]

  def play(track: Track): F[Unit]
  def pause: F[Unit]
  def resume: F[Unit]
  def stop: F[Unit]

}
