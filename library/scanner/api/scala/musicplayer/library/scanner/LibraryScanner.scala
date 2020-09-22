package musicplayer.library.scanner

import java.nio.file.Path

import fs2._
import musicplayer.library.model.metadata.TrackMetadata

trait LibraryScanner[F[_]] {

  def scan(paths: Set[Path]): Stream[F, (Path, TrackMetadata)]

}
