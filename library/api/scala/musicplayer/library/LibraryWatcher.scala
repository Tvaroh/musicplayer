package musicplayer.library

import fs2.Stream
import musicplayer.library.model.event.LibraryWatcherEvent

trait LibraryWatcher[F[_]] {

  def events: Stream[F, LibraryWatcherEvent]

}
