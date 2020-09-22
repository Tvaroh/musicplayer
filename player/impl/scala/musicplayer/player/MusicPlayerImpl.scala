package musicplayer.player

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, Resource, Sync}
import cats.implicits._
import cats.~>
import fs2._
import fs2.concurrent.{NoneTerminatedQueue, Queue}
import musicplayer.library.model.Track
import musicplayer.player.model.PlayerEvent
import tofu.lift.UnsafeExecFuture
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.component.AudioPlayerComponent

import scala.concurrent.Future

private class MusicPlayerImpl[F[_]](mediaPlayer: MediaPlayer,
                                    state: Ref[F, MusicPlayerImpl.State],
                                    override val events: Stream[F, PlayerEvent])
                                   (implicit F: Sync[F])
  extends MusicPlayer[F] {

  override def play(track: Track): F[Unit] =
    for {
      success <- F.delay(mediaPlayer.media().play(track.path.toString))
      _ <- state.update(_.play(track)).whenA(success)
    } yield ()

  override def pause: F[Unit] =
    for {
      wasPaused <- state.getAndUpdate(_.pause).map(_.isPaused)
      _ <- F.delay(mediaPlayer.controls().pause()).whenA(!wasPaused)
    } yield ()

  override def resume: F[Unit] =
    for {
      wasPaused <- state.getAndUpdate(_.resume).map(_.isPaused)
      _ <- F.delay(mediaPlayer.controls().pause()).whenA(wasPaused)
    } yield ()

  override def stop: F[Unit] =
    for {
      _ <- F.delay(mediaPlayer.controls().stop())
      _ <- state.update(_.stop)
    } yield ()

}

object MusicPlayerImpl {

  def apply[F[_]](eventsBufferSize: Int = 100)
                 (implicit F: Concurrent[F],
                           unsafeExecFuture: UnsafeExecFuture[F]): Resource[F, MusicPlayer[F]] =
    Resource.make {
      for {
        state <- Ref.of(State())
        queue <- Queue.boundedNoneTerminated[F, PlayerEvent](eventsBufferSize)
        runToFuture <- unsafeExecFuture.unlift
      } yield (new AudioPlayerComponentImpl(queue, runToFuture), state, queue)
    } { case (audioPlayerComponent, _, queue) =>
      queue.offer1(None) >> F.delay(audioPlayerComponent.release())
    } map { case (audioPlayerComponent, state, queue) =>
      new MusicPlayerImpl(
        audioPlayerComponent.mediaPlayer(),
        state,
        queue.dequeue.filterWithPrevious(_ =!= _)
      )
    }

  private class AudioPlayerComponentImpl[F[_]](queue: NoneTerminatedQueue[F, PlayerEvent],
                                               runToFuture: F ~> Future)
    extends AudioPlayerComponent {

    override def playing(mediaPlayer: MediaPlayer): Unit = {
      sendEvent(PlayerEvent.PlayingStarted())
    }

    override def paused(mediaPlayer: MediaPlayer): Unit = {
      sendEvent(PlayerEvent.PlayingPaused())
    }

    override def stopped(mediaPlayer: MediaPlayer): Unit = {
      sendEvent(PlayerEvent.PlayingStopped())
    }

    override def timeChanged(mediaPlayer: MediaPlayer, newTime: Long): Unit = {
      sendEvent(PlayerEvent.TimeChanged((newTime / 1000).toInt))
    }

    private def sendEvent(event: PlayerEvent): Unit = {
      runToFuture(queue.offer1(Some(event)))
      ()
    }

  }

  private trait State {

    def play(track: Track): State
    def pause: State
    def isPaused: Boolean
    def resume: State
    def stop: State

  }

  private object State {

    def apply(): State =
      Impl(None, paused = false)

    private case class Impl(currentTrack: Option[Track],
                            paused: Boolean)
      extends State {

        override def play(track: Track): State =
          copy(currentTrack = Some(track), paused = false)

        override def pause: State =
          if (currentTrack.isDefined && !paused) copy(paused = true) else this

        override def isPaused: Boolean =
          currentTrack.isDefined && paused

        override def resume: State =
          if (isPaused) copy(paused = false) else this

        override def stop: State =
          if (currentTrack.isDefined) copy(currentTrack = None, paused = false) else this

    }

  }

}
