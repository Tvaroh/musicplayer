package musicplayer.player.model

import cats.Eq
import musicplayer.library.model.Track

sealed trait PlayerEvent

object PlayerEvent {

  case class PlayingStarted(track: Track) extends PlayerEvent
  case class PlayingPaused() extends PlayerEvent
  case class PlayingStopped() extends PlayerEvent
  case class TimeChanged(seconds: Int) extends PlayerEvent

  implicit val eq: Eq[PlayerEvent] = Eq.fromUniversalEquals

}
