package musicplayer.util.model.syntax

trait Cond[A] {

  def cond(condition: Boolean)(onTrue: A => A): A =
    if (condition) onTrue(value) else value

  def condOpt[B](option: Option[B])(f: (A, B) => A): A =
    option.fold(value)(f(value, _))

  protected def value: A

}

object Cond {

  implicit class CondAny[A](override val value: A)
    extends Cond[A]

}
