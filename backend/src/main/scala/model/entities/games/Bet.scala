package model.entities.games

import utils.Result

type BetResult = Result[Double, Double]

trait Bet:
  val amount: Double

case class SlotBet(amount: Double) extends Bet:
  require(amount > 0, "Bet amount must be positive")

case class RouletteBet(amount: Double, targets: List[Int]) extends Bet:
  require(amount > 0, "Bet amount must be positive")
  require(targets.nonEmpty, "Roulette bet must have at least one target")
  require(
    targets.forall(t => t >= 0 && t <= 36),
    "Targets must be between 0 and 36"
  )

case class BlackJackBet(amount: Double, minimumValue: Int) extends Bet:
  require(amount > 0, "Bet amount must be positive")
  require(minimumValue > 0, "Cards value must be positive")
