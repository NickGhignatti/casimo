package utils

import model.entities.customers._

object TriggerDSL:

  trait Trigger[A]:
    def eval(c: A): Boolean

  def Losses[A <: Bankroll[A] & CustomerState[A] & HasBetStrategy[A]](
      n: Int
  ): Trigger[A] = new Trigger[A]:
    def eval(c: A): Boolean =
      c.betStrategy match
        case m: MartingaleStrat[A] => m.lossStreak >= n
        case o: OscarGrindStrat[A] => o.lossStreak >= n
        case _                     => false

  def FrustAbove[A <: BoredomFrustration[A]](p: Double): Trigger[A] =
    new Trigger[A]:
      def eval(c: A): Boolean = c.frustration >= p

  def BoredomAbove[A <: BoredomFrustration[A]](p: Double): Trigger[A] =
    new Trigger[A]:
      def eval(c: A): Boolean = c.boredom >= p

  def BrRatioAbove[A <: Bankroll[A]](r: Double): Trigger[A] =
    new Trigger[A]:
      def eval(c: A): Boolean = c.bankrollRatio >= r

  def BrRatioBelow[A <: Bankroll[A]](r: Double): Trigger[A] =
    new Trigger[A]:
      def eval(c: A): Boolean = c.bankrollRatio <= r

  def Always[A]: Trigger[A] = new Trigger[A]:
    def eval(c: A): Boolean = true

  extension [A](a: Trigger[A])
    infix def &&(b: Trigger[A]): Trigger[A] = new Trigger[A]:
      def eval(c: A): Boolean = a.eval(c) && b.eval(c)

    infix def ||(b: Trigger[A]): Trigger[A] = new Trigger[A]:
      def eval(c: A): Boolean = a.eval(c) || b.eval(c)

    def unary_! : Trigger[A] = new Trigger[A]:
      def eval(c: A): Boolean = !a.eval(c)
