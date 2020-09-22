package musicplayer.library.scanner.model

import java.nio.file.Path

sealed trait LibraryWatcherEvent

object LibraryWatcherEvent {

  case class Created(path: Path) extends LibraryWatcherEvent
  case class Deleted(path: Path) extends LibraryWatcherEvent

}
