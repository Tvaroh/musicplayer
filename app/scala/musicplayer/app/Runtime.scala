package musicplayer.app

import java.nio.file.Path

import cats.effect.Blocker
import cats.~>
import monix.eval.Task
import monix.execution.Scheduler
import musicplayer.util.effect.FileWalk
import tofu.lift.Unlift
import tofu.{BlockExec, Scoped}

import scala.concurrent.Future

object Runtime {

  type Effect[A] = Task[A]

  implicit val scheduler: Scheduler = Scheduler.global
  private val ioScheduler: Scheduler = Scheduler.io()

  private implicit val blocker: Blocker = Blocker.liftExecutionContext(ioScheduler)
  implicit val blockExec: BlockExec[Effect] = Scoped.blockerExecute[Effect]

  implicit val unliftFuture: Unlift[Future, Effect] =
    new Unlift[Future, Effect] {

      override def lift[A](fa: Future[A]): Effect[A] = Task.deferFuture(fa)

      override def unlift: Effect[Effect ~> Future] = Task.now {
        new ~>[Effect, Future] {
          override def apply[A](fa: Effect[A]): Future[A] =
            fa.runToFuture
        }
      }

    }

  implicit val fileWalk: FileWalk[Effect] =
    (path: Path) => fs2.io.file.walk[Effect](blocker, path)

}
