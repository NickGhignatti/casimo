package model.entities.games

import utils.Result

/** Type alias representing the result of a bet operation.
  *
  * Uses the Result type to handle both successful wins (with Double payout) and
  * losses (with Double loss amount).
  */
type BetResult = Result[Double, Double]

/** Base trait for all betting operations in casino games.
  *
  * Defines the common interface that all bet types must implement, ensuring
  * every bet has an associated monetary amount.
  */
trait Bet:
  val amount: Double

/** Represents a bet placed on a slot machine game.
  *
  * Slot bets are simple wagers with only an amount, as slot machines typically
  * don't require additional betting parameters.
  *
  * @param amount
  *   the monetary amount being wagered
  * @throws IllegalArgumentException
  *   if amount is not positive
  */
case class SlotBet(amount: Double) extends Bet:
  require(amount > 0, "Bet amount must be positive")

/** Represents a bet placed on a roulette game with specific number targets.
  *
  * Roulette bets allow players to wager on one or more numbers on the wheel.
  * All target numbers must be valid roulette numbers (0-36).
  *
  * @param amount
  *   the monetary amount being wagered
  * @param targets
  *   the list of roulette numbers (0-36) being bet on
  * @throws IllegalArgumentException
  *   if amount is not positive
  * @throws IllegalArgumentException
  *   if no targets are specified
  * @throws IllegalArgumentException
  *   if any target is outside the valid range (0-36)
  */
case class RouletteBet(amount: Double, targets: List[Int]) extends Bet:
  require(amount > 0, "Bet amount must be positive")
  require(targets.nonEmpty, "Roulette bet must have at least one target")
  require(
    targets.forall(t => t >= 0 && t <= 36),
    "Targets must be between 0 and 36"
  )

/** Represents a bet placed on a blackjack game with a minimum hand value
  * strategy.
  *
  * BlackJack bets include a minimum value parameter that likely represents the
  * minimum hand value the player is aiming to achieve or stand on.
  *
  * @param amount
  *   the monetary amount being wagered
  * @param minimumValue
  *   the minimum card value threshold for the betting strategy
  * @throws IllegalArgumentException
  *   if amount is not positive
  * @throws IllegalArgumentException
  *   if minimumValue is not positive
  */
case class BlackJackBet(amount: Double, minimumValue: Int) extends Bet:
  require(amount > 0, "Bet amount must be positive")
  require(minimumValue > 0, "Cards value must be positive")
