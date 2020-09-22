package musicplayer.library

import java.nio.file.Path

import fs2._
import musicplayer.library.model.TrackMetadata

trait LibraryScanner[F[_]] {

  def scan(paths: Set[Path]): Stream[F, (Path, TrackMetadata)]

}
