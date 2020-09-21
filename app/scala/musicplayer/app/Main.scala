package musicplayer.app

import monix.eval.Task
import musicplayer.app.Runtime._

object Main extends App {

  new Wiring[Task].app.runSyncUnsafe()

}
