package utils

import model.entities.customers._
import model.entities.games.GameType
import model.entities.Player

object TriggerDSL:

  /** Interfaccia base per tutti i Trigger */
  trait Trigger[A]:
    def eval(c: A): Boolean

  /** === Trigger DSL primitives === */
  def Losses[A](n: Int): Trigger[A] = new Trigger[A]:
    def eval(c: A): Boolean =
      c.betStrategy match
        case m: MartingaleStrat[A] => m.lossStreak >= n
        case o: OscarGrindStrat[A] => o.lossStreak >= n
        case _                     => false

  def FrustrationAbove[A](p: Double): Trigger[A] = new Trigger[A]:
    def eval(c: A): Boolean = c.frustration > p

  def BoredomAbove[A](p: Double): Trigger[A] = new Trigger[A]:
    def eval(c: A): Boolean = c.boredom > p

  def BankrollRatioAbove[A <: Bankroll[A]](r: Double): Trigger[A] =
    new Trigger[A]:
      def eval(c: A): Boolean = c.bankroll / c.startingBankroll > r

  def Always[A]: Trigger[A] = new Trigger[A]:
    def eval(c: A): Boolean = true

  /** === Operatori booleani per combinare trigger === */
  extension [A](a: Trigger[A])
    infix def &&(b: Trigger[A]): Trigger[A] = new Trigger[A]:
      def eval(c: A): Boolean = a.eval(c) && b.eval(c)

    infix def ||(b: Trigger[A]): Trigger[A] = new Trigger[A]:
      def eval(c: A): Boolean = a.eval(c) || b.eval(c)

    def unary_! : Trigger[A] = new Trigger[A]:
      def eval(c: A): Boolean = !a.eval(c)
