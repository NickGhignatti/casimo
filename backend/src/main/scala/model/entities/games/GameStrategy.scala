package model.entities.games

import scala.annotation.tailrec
import scala.util.Random

import utils.Result

trait GameStrategy:
  def use(): BetResult

object SlotStrategy:
  def apply: SlotStrategyBuilder = SlotStrategyBuilder()

object RouletteStrategy:
  def apply: RouletteStrategyBuilder = RouletteStrategyBuilder()

object BlackJackStrategy:
  def apply: BlackJackStrategyBuilder = BlackJackStrategyBuilder()

case class SlotStrategyBuilder(
    betAmount: Option[Double] = None,
    condition: Option[() => Boolean] = None
):
  def bet(amount: Double): SlotStrategyBuilder =
    require(amount > 0.0, "Bet amount must be positive")
    this.copy(betAmount = Some(amount))

  def when(cond: => Boolean): SlotStrategyInstance =
    SlotStrategyInstance(betAmount.getOrElse(0.5), () => cond)

case class SlotStrategyInstance(betAmount: Double, condition: () => Boolean)
    extends GameStrategy:
  override def use(): BetResult =
    val values =
      for _ <- 1 to 5 yield Random.nextInt(5) + 1
    if condition() && values.distinct.size == 1 then
      Result.Success(betAmount * 10)
    else Result.Failure(betAmount)

case class RouletteStrategyBuilder(
    betAmount: Option[Double] = None,
    targets: Option[List[Int]] = None
):
  def bet(amount: Double): RouletteStrategyBuilder =
    require(amount > 0.0, "Bet amount must be positive")
    this.copy(betAmount = Some(amount))

  def on(targets: List[Int]): RouletteStrategyBuilder =
    require(targets.nonEmpty, "Roulette bet must have at least one target")
    this.copy(targets = Some(targets))

  def when(cond: => Boolean): RouletteStrategyInstance =
    RouletteStrategyInstance(
      betAmount.getOrElse(0.5),
      targets.getOrElse(List(0)),
      () => cond
    )

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

case class BlackJackStrategyBuilder(
    betAmount: Option[Double] = None,
    minimumVal: Option[Int] = None,
    condition: Option[() => Boolean] = None
):
  def bet(amount: Double): BlackJackStrategyBuilder =
    require(amount > 0.0, "Bet amount must be positive")
    this.copy(betAmount = Some(amount))

  def accept(minimum: Int): BlackJackStrategyBuilder =
    this.copy(minimumVal = Some(minimum))

  def when(cond: => Boolean): BlackJackStrategyInstance =
    BlackJackStrategyInstance(
      betAmount.getOrElse(0.5),
      minimumVal.getOrElse(17),
      () => cond
    )

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

object dsl:
  def use(strategy: SlotStrategy.type): SlotStrategyBuilder = strategy.apply
  def use(strategy: RouletteStrategy.type): RouletteStrategyBuilder =
    strategy.apply
  def use(strategy: BlackJackStrategy.type): BlackJackStrategyBuilder =
    strategy.apply
