package model.managers

import model.GlobalConfig
import model.entities.customers.Bankroll
import model.entities.customers.BoredomFrustration
import model.entities.customers.CustState
import model.entities.customers.CustState.Idle
import model.entities.customers.CustState.Playing
import model.entities.customers.CustomerState
import model.entities.games.Game
import model.entities.games.GameState
import model.entities.games.SlotStrategyInstance
import model.entities.customers.CustState.{Idle, Playing}
import model.entities.customers.{
  Bankroll,
  BettingStrategy,
  BoredomFrustration,
  CustState,
  CustomerState,
  FlatBetting,
  HasBetStrategy
}
import model.entities.games.{
  BlackJackGame,
  Game,
  GameBuilder,
  GameState,
  SlotStrategyInstance
}
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D

case class MockCustomer(
    customerState: CustState = Idle,
    boredom: Double,
    frustration: Double,
    bankroll: Double,
    betStrategy: BettingStrategy[MockCustomer] = FlatBetting[MockCustomer](10)
) extends CustomerState[MockCustomer],
      BoredomFrustration[MockCustomer],
      Bankroll[MockCustomer],
      HasBetStrategy[MockCustomer]:

  protected def changedState(newState: CustState): MockCustomer =
    this.copy(customerState = newState)

  protected def updatedBoredom(newBoredom: Double): MockCustomer =
    this.copy(boredom = newBoredom)

  protected def updatedFrustration(newFrustration: Double): MockCustomer =
    this.copy(frustration = newFrustration)

  protected def updatedBankroll(newBankroll: Double): MockCustomer =
    this.copy(bankroll = newBankroll)

  protected def changedBetStrategy(
      newStrat: BettingStrategy[MockCustomer]
  ): MockCustomer =
    this.copy(betStrategy = newStrat)

val mockGame = GameBuilder.slot(Vector2D.zero)
class TestPersistenceManager extends AnyFunSuite:
  test("Customer should leave the table when too bored"):
    given config: GlobalConfig = GlobalConfig()
    val testPersistenceManager =
      PersistenceManager[MockCustomer](bThreshold = 80, fThreshold = 60)
    val mockCustomer = MockCustomer(
      customerState = Playing(mockGame),
      boredom = 81.0,
      frustration = 50.0,
      bankroll = 40.0
    )
    val shouldBeIdle = testPersistenceManager.update(Seq(mockCustomer)).head
    assert(shouldBeIdle.customerState === Idle)

  test("Customer should leave the table when too frustrated"):
    given config: GlobalConfig = GlobalConfig()

    val testPersistenceManager =
      PersistenceManager[MockCustomer](bThreshold = 80, fThreshold = 60)
    val mockCustomer = MockCustomer(
      customerState = Playing(mockGame),
      boredom = 30.0,
      frustration = 61.0,
      bankroll = 40.0
    )
    val shouldBeIdle = testPersistenceManager.update(Seq(mockCustomer)).head
    assert(shouldBeIdle.customerState === Idle)

  test(
    "Customer shouldn't stop playing if not bored or frustrated and have enough money"
  ):

    given config: GlobalConfig = GlobalConfig()

    val testPersistenceManager =
      PersistenceManager[MockCustomer](bThreshold = 80, fThreshold = 60)
    val mockCustomer = MockCustomer(
      customerState = Playing(mockGame),
      boredom = 50.0,
      frustration = 50.0,
      bankroll = 40.0
    )
    val shouldStillPlay = testPersistenceManager.update(Seq(mockCustomer)).head
    assert(shouldStillPlay.customerState === Playing(mockGame))

  test(
    "Customer should leave the table if is bankroll is less than the bet he want to make"
  ):

    given config: GlobalConfig = GlobalConfig()

    val testPersistenceManager =
      PersistenceManager[MockCustomer](bThreshold = 80, fThreshold = 60)
    val mockCustomer = MockCustomer(
      customerState = Playing(mockGame),
      boredom = 50.0,
      frustration = 50.0,
      bankroll = 50.0
    )
    val shouldStillPlay = testPersistenceManager.update(Seq(mockCustomer)).head
    val lowBankroll = shouldStillPlay.updateBankroll(-45.0)
    val shouldLeave = testPersistenceManager.update(Seq(lowBankroll)).head
    assert(shouldStillPlay.customerState === Playing(mockGame))
    assert(shouldLeave.customerState === Idle)

  test("Customer should Remain Idle if already Idle"):

    given config: GlobalConfig = GlobalConfig()

    val testPersistenceManager =
      PersistenceManager[MockCustomer](bThreshold = 80, fThreshold = 60)
    val mockCustomer = MockCustomer(
      customerState = Idle,
      boredom = 50,
      frustration = 50,
      bankroll = 40.0
    )
    val shouldBeIdle = testPersistenceManager.update(Seq(mockCustomer)).head
    assert(shouldBeIdle.customerState === Idle)
