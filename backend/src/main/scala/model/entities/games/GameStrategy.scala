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
  def use(history: GameHistory = GameHistory(List.empty)): BetResult

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
  override def use(history: GameHistory): BetResult =
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
  private trait BetType:
    def payout: Int

  private object BetType:
    case object StraightUp extends BetType:
      val payout = 35
    case object Split extends BetType:
      val payout = 17
    case object Street extends BetType:
      val payout = 11
    case object Corner extends BetType:
      val payout = 8
    case object SixLine extends BetType:
      val payout = 5
    case object Column extends BetType:
      val payout = 2
    case object Dozen extends BetType:
      val payout = 2
    case object RedBlack extends BetType:
      val payout = 1
    case object OddEven extends BetType:
      val payout = 1
    case object HighLow extends BetType:
      val payout = 1

  private val redNumbers =
    Set(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36)

  private def determineBetType(targetsSize: Int): BetType = targetsSize match
    case 1  => BetType.StraightUp
    case 2  => BetType.Split
    case 3  => BetType.Street
    case 4  => BetType.Corner
    case 6  => BetType.SixLine
    case 12 => BetType.Column // or Dozen
    case 18 => BetType.RedBlack // or OddEven or HighLow
    case _  => BetType.StraightUp // Default fallback

  private def calculateHouseEdge(
      consecutiveWins: Int,
      noTurns: Int,
      betAmount: Double,
      betType: BetType,
      maxHouseEdge: Double = 0.15
  ): Double =
    val validatedTurns = Math.max(1, Math.min(20, noTurns))
    val validatedMaxEdge = Math.max(0.01, Math.min(1.0, maxHouseEdge))

    val baseEdge = 0.027 // Standard 2.7% European roulette edge

    // Progressive edge based on consecutive wins (scaled by houseEdgeTurns)
    val progressiveEdge =
      consecutiveWins * (0.008 * validatedTurns / 10.0)

    // Volume-based edge (more bets = worse odds for player)
    val volumeEdge = Math.min(
      noTurns * (0.0002 * validatedTurns / 10.0),
      0.03 * validatedTurns / 10.0
    )

    // Bet size penalty (discourages large bets)
    val betSizeEdge =
      if betAmount > 100 then
        Math.min(
          (betAmount - 100) * (0.00005 * validatedTurns / 10.0),
          0.025 * validatedTurns / 10.0
        )
      else 0.0

    val betTypeModifier = betType match
      case BetType.StraightUp =>
        0.005 * validatedTurns / 10.0 // Extra edge on single numbers
      case BetType.RedBlack | BetType.OddEven | BetType.HighLow =>
        0.003 * validatedTurns / 10.0 // Small extra on even money
      case _ => 0.0

    // Calculate total edge
    val totalEdge =
      baseEdge + progressiveEdge + volumeEdge + betSizeEdge + betTypeModifier

    // Cap at maximum allowed edge
    Math.min(totalEdge, validatedMaxEdge)

  override def use(history: GameHistory): BetResult =
    if !condition() then Result.Failure(betAmount)
    else
      val winningNumber = Random.nextInt(37)
      val betType = determineBetType(targets.size)
      val houseEdgeMultiplier = 1.0 - calculateHouseEdge(
        history.gains.map(_.getMoneyGain).reverse.takeWhile(_ >= 0).length,
        history.gains.length,
        betAmount,
        betType
      )
      if targets.contains(winningNumber) then
        betType match
          case BetType.StraightUp =>
            Result.Success(betAmount * betType.payout)

          case BetType.RedBlack | BetType.OddEven | BetType.HighLow =>
            // Even money bets: 1:1 but lose on 0 (green)
            if (winningNumber == 0) Result.Failure(betAmount)
            else Result.Success(betAmount * betType.payout)

          case BetType.Column | BetType.Dozen =>
            // 2:1 bets but lose on 0
            if (winningNumber == 0) Result.Failure(betAmount)
            else Result.Success(betAmount * betType.payout)

          case _ =>
            val adjustedPayout =
              (betAmount * betType.payout * houseEdgeMultiplier)
            Result.Success(adjustedPayout)
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

  private def dealHand(): Int =
    val card1 = Random.nextInt(10) + 1
    val card2 = Random.nextInt(10) + 1
    var total = card1 + card2

    // Handle Aces (simplified - count as 11 if beneficial, 1 otherwise)
    if (card1 == 1 && total + 10 <= 21) total += 10
    if (card2 == 1 && total + 10 <= 21) total += 10

    total

  private def hitUntilStand(initialValue: Int, standValue: Int): Int =
    @tailrec
    def hit(current: Int): Int =
      if (current >= standValue || current > 21) current
      else {
        val newCard = Random.nextInt(10) + 1
        val newTotal = current + newCard
        hit(newTotal)
      }

    hit(initialValue)

  override def use(history: GameHistory): BetResult =
    // Deal initial hands
    val dealerInitial = dealHand()
    val playerInitial = dealHand()

    // Player plays first (hits until reaching minimumValue or busting)
    val playerFinal = if (playerInitial < minimumValue) {
      hitUntilStand(playerInitial, minimumValue)
    } else playerInitial

    // Player busts - automatic loss
    if (playerFinal > 21) {
      return Result.Failure(betAmount)
    }

    // Dealer plays (must hit on 16, stand on 17)
    val dealerFinal = if (dealerInitial < 17) {
      hitUntilStand(dealerInitial, 17)
    } else dealerInitial

    // Determine winner
    val result: BetResult = (playerFinal, dealerFinal) match
      // Player blackjack (21 with 2 cards) vs dealer blackjack - push
      case (21, 21) if playerInitial == 21 && dealerInitial == 21 =>
        Result.Success(0.0) // Push - return bet

      // Player blackjack wins (pays 3:2)
      case (21, _) if playerInitial == 21 && dealerInitial != 21 =>
        Result.Success(betAmount * 1.5)

      // Dealer busts, player doesn't
      case (p, d) if d > 21 && p <= 21 =>
        Result.Success(betAmount)

      // Both under 21 - higher wins
      case (p, d) if p <= 21 && d <= 21 =>
        if (p > d) Result.Success(betAmount)
        else if (p == d) Result.Success(0) // Push
        else Result.Failure(betAmount)

      // Player busts (already handled above, but for completeness)
      case _ => Result.Failure(betAmount)

    // Apply the condition check
    if (condition()) result else Result.Failure(betAmount)

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
