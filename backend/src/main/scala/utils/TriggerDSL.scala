package utils

import model.entities.customers._

/** Object containing the Domain-Specific Language (DSL) for defining dynamic
  * triggers.
  *
  * This DSL allows for the creation of clear, readable, and composable
  * conditions used within the simulation's decision-making logic, such as in
  * the DecisionManager.
  */
object TriggerDSL:

  /** Represents a generic trigger condition.
    *
    * A Trigger takes an entity context and evaluates to a boolean, indicating
    * whether the condition is met.
    *
    * @tparam A
    *   The type of the entity (context) on which the trigger evaluates.
    */
  trait Trigger[A]:
    /** Evaluates the trigger condition against the given entity context.
      * @param c
      *   The entity context.
      * @return
      *   True if the condition is met, false otherwise.
      */
    def eval(c: A): Boolean

  /** Creates a trigger that evaluates to true if the entity's loss streak is
    * greater than or equal to 'n'.
    *
    * This trigger is specific to entities using Martingale or OscarGrind
    * betting strategies.
    *
    * @param n
    *   The minimum number of losses in a streak to trigger.
    * @tparam A
    *   The entity type, which must have Bankroll, CustomerState, and
    *   HasBetStrategy capabilities.
    * @return
    *   A Trigger instance.
    */
  def Losses[A <: Bankroll[A] & CustomerState[A] & HasBetStrategy[A]](
      n: Int
  ): Trigger[A] = new Trigger[A]:
    override def eval(c: A): Boolean =
      c.betStrategy match
        case m: MartingaleStrat[A] => m.lossStreak >= n
        case o: OscarGrindStrat[A] => o.lossStreak >= n
        case _ => false // Returns false for strategies without a lossStreak

  /** Creates a trigger that evaluates to true if the entity's frustration level
    * is greater than or equal to 'p'.
    * @param p
    *   The frustration percentage threshold (0.0 - 100.0).
    * @tparam A
    *   The entity type, which must have BoredomFrustration capabilities.
    * @return
    *   A Trigger instance.
    */
  def FrustAbove[A <: BoredomFrustration[A]](p: Double): Trigger[A] =
    new Trigger[A]:
      override def eval(c: A): Boolean = c.frustration >= p

  /** Creates a trigger that evaluates to true if the entity's boredom level is
    * greater than or equal to 'p'.
    * @param p
    *   The boredom percentage threshold (0.0 - 100.0).
    * @tparam A
    *   The entity type, which must have BoredomFrustration capabilities.
    * @return
    *   A Trigger instance.
    */
  def BoredomAbove[A <: BoredomFrustration[A]](p: Double): Trigger[A] =
    new Trigger[A]:
      override def eval(c: A): Boolean = c.boredom >= p

  /** Creates a trigger that evaluates to true if the entity's bankroll ratio
    * (current bankroll / starting bankroll) is greater than or equal to 'r'.
    * This is typically used for "take-profit" conditions.
    * @param r
    *   The bankroll ratio threshold.
    * @tparam A
    *   The entity type, which must have Bankroll capabilities.
    * @return
    *   A Trigger instance.
    */
  def BrRatioAbove[A <: Bankroll[A]](r: Double): Trigger[A] =
    new Trigger[A]:
      override def eval(c: A): Boolean = c.bankrollRatio >= r

  /** Creates a trigger that evaluates to true if the entity's bankroll ratio
    * (current bankroll / starting bankroll) is less than or equal to 'r'. This
    * is typically used for "stop-loss" conditions.
    * @param r
    *   The bankroll ratio threshold.
    * @tparam A
    *   The entity type, which must have Bankroll capabilities.
    * @return
    *   A Trigger instance.
    */
  def BrRatioBelow[A <: Bankroll[A]](r: Double): Trigger[A] =
    new Trigger[A]:
      override def eval(c: A): Boolean = c.bankrollRatio <= r

  /** Creates a trigger that always evaluates to true. Useful for default
    * actions or conditions that are always met.
    * @tparam A
    *   The entity type (can be any type as evaluation is constant).
    * @return
    *   A Trigger instance that always returns true.
    */
  def Always[A]: Trigger[A] = new Trigger[A]:
    override def eval(c: A): Boolean = true

  /** Provides extension methods for composing Trigger instances using logical
    * operators. These methods enable a fluent and intuitive syntax for building
    * complex trigger conditions.
    */
  extension [A](a: Trigger[A])
    /** Composes two triggers with a logical AND operation. Both triggers must
      * evaluate to true for the combined trigger to be true.
      * @param b
      *   The right-hand side Trigger.
      * @return
      *   A new Trigger representing the logical AND.
      */
    infix def &&(b: Trigger[A]): Trigger[A] = new Trigger[A]:
      override def eval(c: A): Boolean = a.eval(c) && b.eval(c)

    /** Composes two triggers with a logical OR operation. At least one trigger
      * must evaluate to true for the combined trigger to be true.
      * @param b
      *   The right-hand side Trigger.
      * @return
      *   A new Trigger representing the logical OR.
      */
    infix def ||(b: Trigger[A]): Trigger[A] = new Trigger[A]:
      override def eval(c: A): Boolean = a.eval(c) || b.eval(c)

    /** Negates the result of a trigger (logical NOT operation).
      * @return
      *   A new Trigger representing the logical NOT.
      */
    def unary_! : Trigger[A] = new Trigger[A]:
      override def eval(c: A): Boolean = !a.eval(c)
