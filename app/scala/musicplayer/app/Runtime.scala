package musicplayer.app

import cats.effect.Blocker
import monix.execution.Scheduler

object Runtime {

  implicit val scheduler: Scheduler = Scheduler.global
  private val ioScheduler: Scheduler = Scheduler.io()

  implicit val blocker: Blocker = Blocker.liftExecutionContext(ioScheduler)

}
