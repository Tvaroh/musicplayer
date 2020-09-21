package musicplayer.player
import java.nio.file.Path

import cats.effect.Sync
import cats.implicits._
import uk.co.caprica.vlcj.player.base.MediaApi
import uk.co.caprica.vlcj.player.component.AudioPlayerComponent

private class MusicPlayerImpl[F[_]](mediaApi: MediaApi,
                                    override val release: F[Unit])
                                   (implicit F: Sync[F])
  extends MusicPlayer[F] {

  override def playTrack(path: Path): F[Unit] =
    F.delay(mediaApi.play(path.toString)).void

}

object MusicPlayerImpl {

  def apply[F[_]](implicit F: Sync[F]): F[MusicPlayer[F]] =
    for {
      audioPlayerComponent <- F.delay(new AudioPlayerComponent())
    } yield {
      val mediaPlayer = audioPlayerComponent.mediaPlayer()

      new MusicPlayerImpl(mediaPlayer.media(), F.delay(audioPlayerComponent.release()))
    }

}
