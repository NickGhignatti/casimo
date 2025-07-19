package model.entities.customers

import model.entities.customers.CustState.Idle
import model.entities.customers.CustState.Playing
import model.entities.games.BlackJackBet
import model.entities.games.GameBuilder
import model.entities.games.RouletteBet
import model.entities.games.SlotBet
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import utils.Result
import utils.Vector2D

private case class MockCustomer(
    customerState: CustState = Idle,
    bankroll: Double,
    betStrategy: BettingStrategy[MockCustomer]
) extends CustomerState[MockCustomer],
      Bankroll[MockCustomer],
      HasBetStrategy[MockCustomer]:

  protected def changedState(newState: CustState): MockCustomer =
    this.copy(customerState = newState)

  protected def updatedBankroll(newBankroll: Double): MockCustomer =
    this.copy(bankroll = newBankroll)

  protected def changedBetStrategy(
      newStrat: BettingStrategy[MockCustomer]
  ): MockCustomer =
    this.copy(betStrategy = newStrat)


class TestBettingStrategy extends AnyFunSuite with Matchers:
  private val mockGame = GameBuilder.slot(Vector2D.zero)

  test(
    "creating a customer with a Bet strategy should store the type of strategy"
  ):
    val mock = MockCustomer(
      customerState = Playing(mockGame),
      bankroll = 40.0,
      betStrategy = FlatBetting[MockCustomer](10)
    )
    assert(mock.betStrategy.isInstanceOf[FlatBetting[MockCustomer]])

  test(
    "creating a bet strategy with a negative bet amount should throw IllegalArgumentException"
  ):
    val negativeBet = -50.0
    val ex = intercept[IllegalArgumentException] {
      MockCustomer(
        customerState = Playing(mockGame),
        bankroll = 40.0,
        betStrategy = FlatBetting[MockCustomer](negativeBet)
      )
    }

    assert(
      ex.getMessage === s"requirement failed: Bet amount must be positive, instead is $negativeBet"
    )

  test(
    "Modifying the bet amount to a new positive amount should update the amount"
  ):
    val mock = MockCustomer(
      customerState = Playing(mockGame),
      bankroll = 40.0,
      betStrategy = FlatBetting[MockCustomer](10)
    )
    val updatedMock = mock.changeBetStrategy(FlatBetting[MockCustomer](30.0))
    assert(updatedMock.betStrategy.betAmount === 30.0)

  test(
    "Modifying the bet amount to a negative amount should throw IllegalArgumentException"
  ):
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
    assert(
      ex.getMessage === s"requirement failed: Bet amount must be positive, instead is $newBet"
    )

  test(
    "Placing a bet should return the correct bet format based, on the game played"
  ):
    val targetList = List(15, 23, 36)
    val mock = MockCustomer(
      customerState = Playing(GameBuilder.blackjack(Vector2D.zero)),
      bankroll = 80.0,
      betStrategy = FlatBetting[MockCustomer](10.0, targetList)
    )
    val bjBet = mock.placeBet()
    val roulette =
      mock.changeState(Playing(GameBuilder.roulette(Vector2D.zero)))
    val rBet = roulette.placeBet()
    val slot = mock.changeState(Playing(GameBuilder.slot(Vector2D.zero)))
    val sBet = slot.placeBet()
    assert(bjBet === BlackJackBet(10.0, targetList.head))
    assert(rBet === RouletteBet(10.0, targetList))
    assert(sBet === SlotBet(10.0))

  test(
    "Placing a bet higher than the current bankroll should throw IllegalArgumentException"
  ):
    val mock = MockCustomer(
      customerState = Playing(GameBuilder.blackjack(Vector2D.zero)),
      bankroll = 40.0,
      betStrategy = FlatBetting[MockCustomer](50.0, 15)
    )
    val ex = intercept[IllegalArgumentException] {
      mock.placeBet()
    }
    assert(
      ex.getMessage === s"requirement failed: Bet amount must be equal or less of the total bankroll, instead is ${mock.betStrategy.betAmount} when the bankroll is ${mock.bankroll}"
    )

  test("Martingale strategy should double the bet every loss"):
    val targetList =
      List(1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 35)

    val mock = MockCustomer(
      customerState = Playing(GameBuilder.roulette(Vector2D.zero)),
      bankroll = 1000.0,
      betStrategy = Martingale[MockCustomer](10.0, targetList)
    )
    val firstBet = mock.placeBet()
    val newBettingStrat =
      mock.updateAfter(Result.Failure(mock.betStrategy.betAmount))
    val secondBet = newBettingStrat.placeBet()
    val lastBettingStrat = newBettingStrat.updateAfter(
      Result.Failure(newBettingStrat.betStrategy.betAmount)
    )
    val thirdBet = lastBettingStrat.placeBet()
    assert(firstBet.amount == 10.0)
    assert(secondBet.amount == 20.0)
    assert(thirdBet.amount == 40.0)

  test("Martingale strategy should reset to baseBet when winning"):
    val targetList =
      List(1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 35)

    val mock = MockCustomer(
      customerState = Playing(GameBuilder.blackjack(Vector2D.zero)),
      bankroll = 1000.0,
      betStrategy = Martingale[MockCustomer](10.0, targetList)
    )
    val firstBet = mock.placeBet()
    val newBettingStrat =
      mock.updateAfter(Result.Failure(mock.betStrategy.betAmount))
    val secondBet = newBettingStrat.placeBet()
    val lastBettingStrat = newBettingStrat.updateAfter(
      Result.Success(newBettingStrat.betStrategy.betAmount)
    )
    val thirdBet = lastBettingStrat.placeBet()
    assert(firstBet.amount == 10.0)
    assert(secondBet.amount == 20.0)
    assert(thirdBet.amount == 10.0)

  test("In Oscar grind maintain betAmount on failure"):
    val bankroll = 100.0
    val mock = MockCustomer(
      customerState = Playing(GameBuilder.blackjack(Vector2D.zero)),
      bankroll = bankroll,
      betStrategy = OscarGrind[MockCustomer](5.0, bankroll, 17)
    )
    val bet = mock.placeBet()
    val mockLose = mock.updateBankroll(-bet.amount)
    val lose = mockLose.updateAfter(Result.Failure(bet.amount))
    val mockWin = lose.updateBankroll(bet.amount)
    val win = mockWin.updateAfter(Result.Success(mock.betStrategy.betAmount))
    val newBet = win.placeBet()
    val mockAnotherLose = win.updateBankroll(- newBet.amount)
    val anotherLose = mockAnotherLose.updateAfter(Result.Failure(mock.betStrategy.betAmount))

    lose.betStrategy.betAmount shouldEqual 5.0
    anotherLose.betStrategy.betAmount shouldEqual 10.0

  test("In Oscar Grind resets betAmount to baseBet when bankroll increased above startingBankroll"):
    val bankroll = 100.0
    val mock = MockCustomer(
      customerState = Playing(GameBuilder.blackjack(Vector2D.zero)),
      bankroll = bankroll,
      betStrategy = OscarGrind[MockCustomer](5.0, bankroll, 17)
    )
    val bet = mock.placeBet()
    val mock2 = mock.updateBankroll(bet.amount)
    val afterWin = mock2.updateAfter(Result.Success(mock.betStrategy.betAmount))
    val stratAfterWin = afterWin.betStrategy.asInstanceOf[OscarGrind[MockCustomer]]

    stratAfterWin.betAmount shouldEqual 5.0
    stratAfterWin.startingBankroll shouldEqual 105.0


  test("In Oscar grind increments betAmount by baseBet on success that don't surpass starting bankroll "):
    val bankroll = 100.0
    val mock = MockCustomer(
      customerState = Playing(GameBuilder.blackjack(Vector2D.zero)),
      bankroll = bankroll,
      betStrategy = OscarGrind[MockCustomer](5.0, bankroll, 17)
    )
    val bet = mock.placeBet()
    val mockLose = mock.updateBankroll(- bet.amount)
    val lose = mockLose.updateAfter(Result.Failure(bet.amount))
    val mockWin = lose.updateBankroll(bet.amount)
    val stratAfterWin = mockWin.updateAfter(Result.Success(mock.betStrategy.betAmount)).betStrategy.asInstanceOf[OscarGrind[MockCustomer]]

    stratAfterWin.betAmount shouldEqual 10.0
    stratAfterWin.startingBankroll shouldEqual 100.0
