package musicplayer.player.model

import cats.Eq

sealed trait PlayerEvent

object PlayerEvent {

  case class PlayingStarted() extends PlayerEvent
  case class PlayingPaused() extends PlayerEvent
  case class PlayingStopped() extends PlayerEvent
  case class TimeChanged(seconds: Int) extends PlayerEvent

  implicit val eq: Eq[PlayerEvent] = Eq.fromUniversalEquals

}
