package musicplayer.player.model

sealed trait PlayerEvent

object PlayerEvent {

  case class TimeChanged(seconds: Int) extends PlayerEvent

}
