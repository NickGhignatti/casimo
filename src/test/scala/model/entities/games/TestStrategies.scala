package model.entities.games

import org.scalatest.funsuite.AnyFunSuite
import utils.Result.{Failure, Success}

class TestStrategies extends AnyFunSuite {

  test(
    "SlotStrategy.use should return either correct winnings or refund loss"
  ) {
    val betValue = 10
    val multiplier = 100
    val slotMachine = SlotStrategy(5, 6, multiplier)
    val bet = FixedBet(betValue)

    slotMachine.use(bet) match {
      case Success(winnings) =>
        assert(winnings === betValue * multiplier)
      case Failure(loss) =>
        assert(loss === betValue)
    }
  }
}
