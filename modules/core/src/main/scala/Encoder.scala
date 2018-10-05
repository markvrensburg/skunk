package skunk


import cats._

trait Encoder[A] { outer =>

  lazy val empty: List[Option[String]] =
    oids.map(_ => None)

  def encode(a: A): List[Option[String]]

  def oids: List[Type]

  def contramap[B](f: B => A): Encoder[B] =
    new Encoder[B] {
      def encode(b: B) = outer.encode(f(b))
      val oids = outer.oids
    }

  def product[B](fb: Encoder[B]): Encoder[(A, B)] =
    new Encoder[(A, B)] {
      def encode(ab: (A, B)) = outer.encode(ab._1) ++ fb.encode(ab._2)
      val oids = outer.oids ++ fb.oids
    }

  def ~[B](fb: Encoder[B]): Encoder[A ~ B] =
    product(fb)

  override def toString =
    s"Encoder(${oids.toList.mkString(", ")})"

  // todo: implicit evidence that it's not already an option .. can be circumvented but prevents
  // dumb errors
  def opt: Encoder[Option[A]] =
    new Encoder[Option[A]] {
      def encode(a: Option[A]) = a.fold(empty)(outer.encode)
      val oids = outer.oids
    }

}

object Encoder {

  implicit val ContravariantSemigroupalEncoder: ContravariantSemigroupal[Encoder] =
    new ContravariantSemigroupal[Encoder] {
      def contramap[A, B](fa: Encoder[A])(f: B => A) = fa contramap f
      def product[A, B](fa: Encoder[A],fb: Encoder[B]) = fa product fb
    }

  val void: Encoder[Void] =
    new Encoder[Void] {
      def encode(a: Void) = sys.error("impossible: Void is uninhabited")
      val oids = Nil
    }

}