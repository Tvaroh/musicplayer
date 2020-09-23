package musicplayer.util.effect

import java.nio.file.Path

import fs2.Stream

trait FileWalk[F[_]] {

  def walk(path: Path): Stream[F, Path]

}
