package musicplayer.library.scanner

import java.nio.file.Path

import musicplayer.library.model.Library

trait LibraryScanner[F[_]] {

  def scan(paths: Set[Path]): F[Library]

}
