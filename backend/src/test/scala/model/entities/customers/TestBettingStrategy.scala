package model.entities.customers

import model.entities.customers.CustState.{Idle, Playing}
import model.entities.games.{BlackJackBet, GameBuilder, RouletteBet, SlotBet}
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D

case class MockCustomer(
                         customerState: CustState = Idle,
                         bankroll: Double,
                         betStrategy: BettingStrategy[MockCustomer],
                       ) extends CustomerState[MockCustomer],
  Bankroll[MockCustomer],
  HasBetStrategy[MockCustomer]:

  protected def changedState(newState: CustState): MockCustomer =
    this.copy(customerState = newState)

  protected def updatedBankroll(newBankroll: Double): MockCustomer =
    this.copy(bankroll = newBankroll)

  protected def changedBetStrategy(newStrat: BettingStrategy[MockCustomer]): MockCustomer =
    this.copy(betStrategy = newStrat)

val mockGame = GameBuilder.slot(Vector2D.zero)

class TestBettingStrategy extends AnyFunSuite:

  test("creating a customer with a Bet strategy should store the type of strategy"):
    val mock = MockCustomer(
      customerState = Playing(mockGame),
      bankroll = 40.0,
      betStrategy = FlatBetting[MockCustomer](10)
    )
    assert(mock.betStrategy.isInstanceOf[FlatBetting[MockCustomer]])

  test("creating a bet strategy with a negative bet amount should throw IllegalArgumentException"):
    val negativeBet = -50.0
    val ex = intercept[IllegalArgumentException] {
      MockCustomer(
        customerState = Playing(mockGame),
        bankroll = 40.0,
        betStrategy = FlatBetting[MockCustomer](negativeBet)
      )
    }

    assert(ex.getMessage === s"requirement failed: Bet amount must be positive, instead is $negativeBet")

  test("Modifying the bet amount to a new positive amount should update the amount"):
    val mock = MockCustomer(
      customerState = Playing(mockGame),
      bankroll = 40.0,
      betStrategy = FlatBetting[MockCustomer](10)
    )
    val updatedMock = mock.changeBetStrategy(FlatBetting[MockCustomer](30.0))
    assert(updatedMock.betStrategy.betAmount === 30.0)

  test("Modifying the bet amount to a negative amount should throw IllegalArgumentException"):
    val startValue = 10.0
    val mock = MockCustomer(
      customerState = Playing(mockGame),
      bankroll = 40.0,
      betStrategy = FlatBetting[MockCustomer](startValue)
    )
    val newBet = -10.0
    val ex = intercept[IllegalArgumentException] {
      val updatedMock = mock.changeBetStrategy(FlatBetting[MockCustomer](-10.0))
    }
    assert(ex.getMessage === s"requirement failed: Bet amount must be positive, instead is $newBet")

  test("Placing a bet should return the correct bet format based, on the game played"):
    val targetList = List(15,23,36)
    val mock = MockCustomer(
      customerState = Playing(GameBuilder.blackjack(Vector2D.zero)),
      bankroll = 80.0,
      betStrategy = FlatBetting[MockCustomer](10.0, targetList: _*)
    )
    val bjBet = mock.placeBet()
    val roulette = mock.changeState(Playing(GameBuilder.roulette(Vector2D.zero)))
    val rBet = roulette.placeBet()
    val slot = mock.changeState(Playing(GameBuilder.slot(Vector2D.zero)))
    val sBet = slot.placeBet()
    assert(bjBet === BlackJackBet(10.0,targetList.head))
    assert(rBet === RouletteBet(10.0,targetList))
    assert(sBet === SlotBet(10.0))

  test("Placing a bet higher than the current bankroll should throw IllegalArgumentException"):
    val mock = MockCustomer(
      customerState = Playing(GameBuilder.blackjack(Vector2D.zero)),
      bankroll = 40.0,
      betStrategy = FlatBetting[MockCustomer](50.0, 15)
    )
    val ex = intercept[IllegalArgumentException] {
      mock.placeBet()
    }
    assert(ex.getMessage === s"requirement failed: Bet amount must be equal or less of the total bankroll, instead is ${mock.betStrategy.betAmount} when the bankroll is ${mock.bankroll}")
