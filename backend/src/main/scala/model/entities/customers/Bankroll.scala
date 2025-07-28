package model.entities.customers

/** Defines the contract for an entity that possesses a bankroll (money).
  *
  * This trait tracks the entity's current financial balance and its initial
  * starting balance, providing methods to update the bankroll and calculate its
  * ratio relative to the start.
  *
  * @tparam T
  *   The concrete type of the entity that extends this trait, enabling
  *   F-bounded polymorphism for immutable updates.
  */
trait Bankroll[T <: Bankroll[T]]:
  /** The current amount of money held by the entity.
    */
  val bankroll: Double

  /** The initial bankroll amount when the entity started. Used for ratio
    * calculations.
    */
  val startingBankroll: Double

  // Precondition to ensure bankroll is non-negative
  require(
    bankroll >= 0,
    s"Bankroll amount must be positive, instead is $bankroll"
  )

  /** Updates the entity's bankroll by adding a `netValue`. The `netValue` can
    * be positive (gain) or negative (loss). A requirement ensures the bankroll
    * does not drop below zero.
    *
    * @param netValue
    *   The amount to add to the current bankroll.
    * @return
    *   A new instance of the entity with the updated bankroll.
    * @throws IllegalArgumentException
    *   if the new bankroll would be negative.
    */
  def updateBankroll(netValue: Double): T =
    val newBankroll = bankroll + netValue
    require(
      newBankroll >= 0,
      s"Bankroll amount must be positive, instead is $newBankroll"
    )
    withBankroll(newBankroll, true) // Pass true to indicate an update

  /** Calculates the ratio of the current bankroll to the starting bankroll.
    * This is useful for tracking profit/loss relative to the initial
    * investment.
    *
    * @return
    *   The bankroll ratio (current bankroll / starting bankroll).
    */
  def bankrollRatio: Double = bankroll / startingBankroll

  /** Returns a new instance of the entity with an updated bankroll value. This
    * method must be implemented by concrete classes to ensure immutability.
    *
    * @param newBankroll
    *   The new bankroll value.
    * @param update
    *   A boolean flag, often used to differentiate initial setting from an
    *   update, though its specific use depends on the implementing class.
    * @return
    *   A new instance of the entity with the new bankroll.
    */
  def withBankroll(newBankroll: Double, update: Boolean = false): T
