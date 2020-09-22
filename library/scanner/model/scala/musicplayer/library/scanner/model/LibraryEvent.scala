package musicplayer.library.scanner.model

import java.nio.file.Path

sealed trait LibraryEvent

object LibraryEvent {

  case class Created(path: Path) extends LibraryEvent
  case class Deleted(path: Path) extends LibraryEvent

}
