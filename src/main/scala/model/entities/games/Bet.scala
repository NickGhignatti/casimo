package model.entities.games

trait Bet {
  val amount: Double
}

case class FixedBet(amount: Double) extends Bet {
  require(amount > 0, "Bet amount must be positive")
}

type SlotBet = FixedBet
type BlackjackBet = FixedBet

case class RouletteBet(amount: Double, targets: List[Int]) extends Bet {
  require(amount > 0, "Bet amount must be positive")
  require(targets.nonEmpty, "Roulette bet must have at least one target")
  require(
    targets.forall(t => t >= 0 && t <= 36),
    "Targets must be between 0 and 36"
  )
}
