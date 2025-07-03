package model.entities.games

import scala.util.Random

import utils.Result

trait GameStrategy[B <: Bet]:
  def use(bet: B): Result[Double, Double]

case class SlotStrategy(rollQuantity: Int, rollMaxValue: Int, multiplier: Int)
    extends GameStrategy[FixedBet]:
  override def use(bet: FixedBet): Result[Double, Double] =
    val values =
      for _ <- 1 to rollQuantity yield Random.nextInt(rollMaxValue) + 1
    if (values.distinct.size == 1)
      Result.Success(bet.amount * multiplier)
    else
      Result.Failure(bet.amount)

case class BlackjackStrategy() extends GameStrategy[FixedBet]:
  override def use(bet: FixedBet): Result[Double, Double] = ???

case class RouletteStrategy() extends GameStrategy[RouletteBet]:
  override def use(bet: RouletteBet): Result[Double, Double] = ???
