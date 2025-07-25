package model.entities.games

import scala.annotation.tailrec
import scala.util.Random

import utils.Result

/** Base trait for all game strategy implementations.
  *
  * Defines the common interface that all game strategies must implement to
  * execute betting logic and return results.
  */
trait GameStrategy:
  /** Executes the strategy and returns the betting result.
    *
    * @return
    *   BetResult indicating win (Success) or loss (Failure) with monetary
    *   amounts
    */
  def use(): BetResult

/** Executes the strategy and returns the betting result.
  *
  * @return
  *   BetResult indicating win (Success) or loss (Failure) with monetary amounts
  */
object SlotStrategy:
  /** Creates a new slot strategy builder.
    *
    * @return
    *   a new SlotStrategyBuilder instance for configuration
    */
  def apply: SlotStrategyBuilder = SlotStrategyBuilder()

/** Factory object for creating roulette strategy builders.
  *
  * Provides a convenient entry point for constructing roulette betting
  * strategies using the builder pattern.
  */
object RouletteStrategy:
  /** Creates a new roulette strategy builder.
    *
    * @return
    *   a new RouletteStrategyBuilder instance for configuration
    */
  def apply: RouletteStrategyBuilder = RouletteStrategyBuilder()

/** Factory object for creating blackjack strategy builders.
  *
  * Provides a convenient entry point for constructing blackjack betting
  * strategies using the builder pattern.
  */
object BlackJackStrategy:
  /** Creates a new blackjack strategy builder.
    *
    * @return
    *   a new BlackJackStrategyBuilder instance for configuration
    */
  def apply: BlackJackStrategyBuilder = BlackJackStrategyBuilder()

/** Builder for constructing slot machine betting strategies.
  *
  * Uses the builder pattern to configure bet amounts and conditions before
  * creating the final strategy instance.
  *
  * @param betAmount
  *   optional bet amount (defaults to 0.5 if not specified)
  * @param condition
  *   optional execution condition function
  */
case class SlotStrategyBuilder(
    betAmount: Option[Double] = None,
    condition: Option[() => Boolean] = None
):
  /** Sets the betting amount for the strategy.
    *
    * @param amount
    *   the monetary amount to bet (must be positive)
    * @return
    *   updated builder with the specified bet amount
    * @throws IllegalArgumentException
    *   if amount is not positive
    */
  def bet(amount: Double): SlotStrategyBuilder =
    require(amount > 0.0, "Bet amount must be positive")
    this.copy(betAmount = Some(amount))

  /** Sets the execution condition and creates the final strategy instance.
    *
    * @param cond
    *   the condition under which the strategy should execute
    * @return
    *   configured SlotStrategyInstance ready for execution
    */
  def when(cond: => Boolean): SlotStrategyInstance =
    SlotStrategyInstance(betAmount.getOrElse(0.5), () => cond)

/** Executable slot machine strategy instance.
  *
  * Implements slot machine logic where winning requires all 5 generated numbers
  * to be identical. Winning pays 10x the bet amount.
  *
  * @param betAmount
  *   the amount being wagered
  * @param condition
  *   function determining if the strategy should execute
  */
case class SlotStrategyInstance(betAmount: Double, condition: () => Boolean)
    extends GameStrategy:
  override def use(): BetResult =
    val values =
      for _ <- 1 to 5 yield Random.nextInt(5) + 1
    if condition() && values.distinct.size == 1 then
      Result.Success(betAmount * 10)
    else Result.Failure(betAmount)

/** Builder for constructing roulette betting strategies.
  *
  * Uses the builder pattern to configure bet amounts, target numbers, and
  * conditions before creating the final strategy instance.
  *
  * @param betAmount
  *   optional bet amount (defaults to 0.5 if not specified)
  * @param targets
  *   optional list of target numbers (defaults to List(0) if not specified)
  */
case class RouletteStrategyBuilder(
    betAmount: Option[Double] = None,
    targets: Option[List[Int]] = None
):
  /** Sets the betting amount for the strategy.
    *
    * @param amount
    *   the monetary amount to bet (must be positive)
    * @return
    *   updated builder with the specified bet amount
    * @throws IllegalArgumentException
    *   if amount is not positive
    */
  def bet(amount: Double): RouletteStrategyBuilder =
    require(amount > 0.0, "Bet amount must be positive")
    this.copy(betAmount = Some(amount))

  /** Sets the target numbers to bet on.
    *
    * @param targets
    *   list of roulette numbers to bet on (must be non-empty)
    * @return
    *   updated builder with the specified targets
    * @throws IllegalArgumentException
    *   if targets list is empty
    */
  def on(targets: List[Int]): RouletteStrategyBuilder =
    require(targets.nonEmpty, "Roulette bet must have at least one target")
    this.copy(targets = Some(targets))

  /** Sets the execution condition and creates the final strategy instance.
    *
    * @param cond
    *   the condition under which the strategy should execute
    * @return
    *   configured RouletteStrategyInstance ready for execution
    */
  def when(cond: => Boolean): RouletteStrategyInstance =
    RouletteStrategyInstance(
      betAmount.getOrElse(0.5),
      targets.getOrElse(List(0)),
      () => cond
    )

/** Executable roulette strategy instance.
  *
  * Implements roulette logic with a 37-number wheel (0-36). Winning pays based
  * on the number of targets bet on: payout = betAmount * 37 / targets.size.
  *
  * @param betAmount
  *   the amount being wagered
  * @param targets
  *   the list of numbers being bet on
  * @param condition
  *   function determining if the strategy should execute
  */
case class RouletteStrategyInstance(
    betAmount: Double,
    targets: List[Int],
    condition: () => Boolean
) extends GameStrategy:
  override def use(): BetResult =
    if condition() then
      val winningNumber = Random.nextInt(37)
      if targets.contains(winningNumber) then
        Result.Success(betAmount * 37 / targets.size)
      else Result.Failure(betAmount)
    else Result.Failure(betAmount)

/** Builder for constructing blackjack betting strategies.
  *
  * Uses the builder pattern to configure bet amounts, minimum hand values, and
  * conditions before creating the final strategy instance.
  *
  * @param betAmount
  *   optional bet amount (defaults to 0.5 if not specified)
  * @param minimumVal
  *   optional minimum hand value threshold (defaults to 17 if not specified)
  * @param condition
  *   optional execution condition function
  */
case class BlackJackStrategyBuilder(
    betAmount: Option[Double] = None,
    minimumVal: Option[Int] = None,
    condition: Option[() => Boolean] = None
):
  /** Sets the betting amount for the strategy.
    *
    * @param amount
    *   the monetary amount to bet (must be positive)
    * @return
    *   updated builder with the specified bet amount
    * @throws IllegalArgumentException
    *   if amount is not positive
    */
  def bet(amount: Double): BlackJackStrategyBuilder =
    require(amount > 0.0, "Bet amount must be positive")
    this.copy(betAmount = Some(amount))

  /** Sets the minimum hand value threshold for the player strategy.
    *
    * @param minimum
    *   the minimum hand value the player will accept before standing
    * @return
    *   updated builder with the specified minimum value
    */
  def accept(minimum: Int): BlackJackStrategyBuilder =
    this.copy(minimumVal = Some(minimum))

  /** Sets the execution condition and creates the final strategy instance.
    *
    * @param cond
    *   the condition under which the strategy should execute
    * @return
    *   configured BlackJackStrategyInstance ready for execution
    */
  def when(cond: => Boolean): BlackJackStrategyInstance =
    BlackJackStrategyInstance(
      betAmount.getOrElse(0.5),
      minimumVal.getOrElse(17),
      () => cond
    )

/** Executable blackjack strategy instance.
  *
  * Implements simplified blackjack logic where both dealer and player draw
  * cards until reaching their respective thresholds. Player wins if dealer
  * busts (>21) or if player has higher value ≤21. Blackjack (21) pays 3x, other
  * wins pay 2x.
  *
  * @param betAmount
  *   the amount being wagered
  * @param minimumValue
  *   the minimum hand value the player will accept
  * @param condition
  *   function determining if the strategy should execute
  */
case class BlackJackStrategyInstance(
    betAmount: Double,
    minimumValue: Int,
    condition: () => Boolean
) extends GameStrategy:
  @tailrec
  private def dealCard(cardsValue: Int, stopValue: Int): Int =
    val currentValue = cardsValue + Random.nextInt(10) + 1
    if currentValue > stopValue then currentValue
    else dealCard(currentValue, stopValue)

  override def use(): BetResult =
    val dealerValue = dealCard(0, 17)
    val playerValue = dealCard(0, minimumValue)
    if condition() && (dealerValue > 21 || (playerValue > dealerValue && playerValue <= 21))
    then
      Result.Success(betAmount * playerValue match
        case 21 => 3
        case _  => 2
      )
    else Result.Failure(betAmount)

/** Domain Specific Language (DSL) for game strategy creation.
  *
  * Provides a fluent, readable interface for constructing game strategies using
  * a natural language-like syntax. The DSL abstracts the direct factory method
  * calls and enables more expressive code when building betting strategies.
  *
  * Example usage:
  * {{{
  * val strategy = use(SlotStrategy).bet(10.0).when(true)
  * val rouletteStrategy = use(RouletteStrategy).bet(5.0).on(List(1, 2, 3)).when(true)
  * val blackjackStrategy = use(BlackJackStrategy).bet(20.0).accept(18).when(true)
  * }}}
  */
object dsl:
  def use(strategy: SlotStrategy.type): SlotStrategyBuilder = strategy.apply
  def use(strategy: RouletteStrategy.type): RouletteStrategyBuilder =
    strategy.apply
  def use(strategy: BlackJackStrategy.type): BlackJackStrategyBuilder =
    strategy.apply
