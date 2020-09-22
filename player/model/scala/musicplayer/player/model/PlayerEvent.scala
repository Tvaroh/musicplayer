package musicplayer.player.model

sealed trait PlayerEvent

object PlayerEvent {

  case class PlayingStarted() extends PlayerEvent
  case class PlayingPaused() extends PlayerEvent
  case class PlayingStopped() extends PlayerEvent
  case class TimeChanged(seconds: Int) extends PlayerEvent

}
