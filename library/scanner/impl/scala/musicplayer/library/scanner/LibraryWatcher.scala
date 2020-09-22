package musicplayer.library.scanner

import java.nio.file._

import cats.effect.{Blocker, ContextShift, Resource, Sync}
import cats.implicits._
import fs2._
import musicplayer.library.scanner.model.LibraryWatcherEvent

import scala.jdk.CollectionConverters._

trait LibraryWatcher[F[_]] {

  def events: Stream[F, LibraryWatcherEvent]

}

private class LibraryWatcherImpl[F[_]](override val events: Stream[F, LibraryWatcherEvent])
  extends LibraryWatcher[F]

object LibraryWatcherImpl {

  def apply[F[_]](followSymLinks: Boolean,
                  paths: Set[Path])
                 (implicit F: Sync[F],
                           blocker: Blocker,
                           cs: ContextShift[F]): Resource[F, LibraryWatcher[F]] =
    Resource.make {
      for {
        watchService <- F.delay(FileSystems.getDefault.newWatchService())
        directories <- findDirectoriesToWatch(followSymLinks, paths)
        _ <- directories.toList.traverse(registerDirectory(_, watchService))
      } yield {
        val libraryWatcher =
          new LibraryWatcherImpl(
            Stream.eval(blocker.delay(watchService.take()))
              .repeat
              .evalMap(handleWatchEvents(_)(registerDirectory(_, watchService)))
              .flatMap(events => Stream(events: _*))
          )

        (watchService, libraryWatcher)
      }
    } { case (watchService, _) => F.delay(watchService.close()) }
      .map(_._2)

  private def registerDirectory[F[_]](directory: Path, watchService: WatchService)
                                     (implicit F: Sync[F],
                                               blocker: Blocker,
                                               cs: ContextShift[F]): F[Unit] =
    blocker
      .delay {
        directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE)
      }
      .void

  private def findDirectoriesToWatch[F[_]](followSymLinks: Boolean,
                                           paths: Set[Path])
                                          (implicit F: Sync[F],
                                                    blocker: Blocker,
                                                    cs: ContextShift[F]): F[Set[Path]] =
    paths.toList
      .traverse { path =>
        fs2.io.file.walk(blocker, path)
          .filter { path =>
            if (followSymLinks) Files.isDirectory(path) else Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)
          }
          .compile
          .toVector
      }
      .map(_.view.flatten.toSet)

  private def handleWatchEvents[F[_]](watchKey: WatchKey)
                                     (registerDirectory: Path => F[Unit])
                                     (implicit F: Sync[F]): F[List[LibraryWatcherEvent]] =
    (watchKey.watchable() match {
      case directory: Path =>
        watchKey.pollEvents().asScala.toList
          .traverse { event =>
            event.context() match {
              case path: Path =>
                val absolutePath = directory.resolve(path)

                event.kind match {
                  case StandardWatchEventKinds.ENTRY_CREATE =>
                    registerDirectory(absolutePath).whenA(Files.isDirectory(absolutePath))
                      .as((LibraryWatcherEvent.Created(absolutePath): LibraryWatcherEvent).some)
                  case StandardWatchEventKinds.ENTRY_DELETE =>
                    F.pure((LibraryWatcherEvent.Deleted(absolutePath): LibraryWatcherEvent).some)
                  case _ =>
                    F.pure(none[LibraryWatcherEvent])
                }
              case _ =>
                F.pure(none[LibraryWatcherEvent])
            }
          }
          .map(_.flatten)
      case _ =>
        F.pure(List.empty[LibraryWatcherEvent])
    }) <* F.delay(watchKey.reset())

}
