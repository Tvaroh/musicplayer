package musicplayer.app

import cats.effect.Blocker
import cats.~>
import monix.eval.Task
import monix.execution.Scheduler
import tofu.lift.Unlift

import scala.concurrent.Future

object Runtime {

  implicit val scheduler: Scheduler = Scheduler.global
  private val ioScheduler: Scheduler = Scheduler.io()

  implicit val blocker: Blocker = Blocker.liftExecutionContext(ioScheduler)

  implicit val unliftFuture: Unlift[Future, Task] =
    new Unlift[Future, Task] {

      override def lift[A](fa: Future[A]): Task[A] = Task.deferFuture(fa)

      override def unlift: Task[Task ~> Future] = Task.now {
        new ~>[Task, Future] {
          override def apply[A](fa: Task[A]): Future[A] =
            fa.runToFuture
        }
      }

    }

}
