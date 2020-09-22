package musicplayer.player

import java.nio.file.Path

import cats.effect.{Concurrent, Resource, Sync}
import cats.implicits._
import fs2._
import fs2.concurrent.Queue
import musicplayer.player.model.PlayerEvent
import tofu.lift.UnsafeExecFuture
import uk.co.caprica.vlcj.player.base.{MediaApi, MediaPlayer}
import uk.co.caprica.vlcj.player.component.AudioPlayerComponent

private class MusicPlayerImpl[F[_]](mediaApi: MediaApi,
                                    override val events: Stream[F, PlayerEvent])
                                   (implicit F: Sync[F])
  extends MusicPlayer[F] {

  override def playTrack(path: Path): F[Unit] =
    F.delay(mediaApi.play(path.toString)).void

}

object MusicPlayerImpl {

  def apply[F[_]](eventsBufferSize: Int = 100)
                 (implicit F: Concurrent[F],
                           unsafeExecFuture: UnsafeExecFuture[F]): F[Resource[F, MusicPlayer[F]]] =
    for {
      queue <- Queue.boundedNoneTerminated[F, PlayerEvent](eventsBufferSize)
      runToFuture <- unsafeExecFuture.unlift
    } yield {
      Resource.make(
        F.delay {
          new AudioPlayerComponent() {

            override def timeChanged(mediaPlayer: MediaPlayer, newTime: Long): Unit = {
              sendEvent(PlayerEvent.TimeChanged((newTime / 1000).toInt))
            }

            private def sendEvent(event: PlayerEvent): Unit = {
              runToFuture(queue.offer1(Some(event)))
              ()
            }

          }
        }
      )(audioPlayerComponent => queue.offer1(None) >> F.delay(audioPlayerComponent.release()))
        .map { audioPlayerComponent =>
          new MusicPlayerImpl(audioPlayerComponent.mediaPlayer().media(), queue.dequeue)
        }
    }

}
