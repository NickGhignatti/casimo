package model.managers

import model.entities.customers.{Bankroll, BettingStrategy, BoredomFrustration, CustState, Customer, CustomerState, FlatBetting, HasBetStrategy}
import model.entities.customers.CustState.Idle
import model.entities.customers.CustState.Playing
import model.entities.games.GameBuilder
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D



val mockGame = GameBuilder.slot(Vector2D.zero)
class TestPersistenceManager extends AnyFunSuite:
  test("Customer should leave the table when too bored"):
    val testPersistenceManager =
      PersistenceManager[Customer](bThreshold = 80, fThreshold = 60)
    val mockCustomer = Customer()
      .withCustomerState(Playing(mockGame))
      .withBoredom(81.0)
      .withFrustration(50.0)
      .withBankroll(40.0)

    val shouldBeIdle = testPersistenceManager.update(Seq(mockCustomer)).head
    assert(shouldBeIdle.customerState === Idle)

  test("Customer should leave the table when too frustrated"):

    val testPersistenceManager =
      PersistenceManager[Customer](bThreshold = 80, fThreshold = 60)

    val mockCustomer = Customer()
      .withCustomerState(Playing(mockGame))
      .withBoredom(30.0)
      .withFrustration(61.0)
      .withBankroll(40.0)

    val shouldBeIdle = testPersistenceManager.update(Seq(mockCustomer)).head
    assert(shouldBeIdle.customerState === Idle)

  test(
    "Customer shouldn't stop playing if not bored or frustrated and have enough money"
  ):

    val testPersistenceManager =
      PersistenceManager[Customer](bThreshold = 80, fThreshold = 60)

    val mockCustomer = Customer()
      .withCustomerState(Playing(mockGame))
      .withBoredom(50.0)
      .withFrustration(50.0)
      .withBankroll(40.0)

    val shouldStillPlay = testPersistenceManager.update(Seq(mockCustomer)).head
    assert(shouldStillPlay.customerState === Playing(mockGame))

  test(
    "Customer should leave the table if is bankroll is less than the bet he want to make"
  ):

    val testPersistenceManager =
      PersistenceManager[Customer](bThreshold = 80, fThreshold = 60)

    val mockCustomer = Customer()
      .withCustomerState(Playing(mockGame))
      .withBoredom(50.0)
      .withFrustration(50.0)
      .withBankroll(50.0)
      .withBetStrategy(FlatBetting[Customer](15.0))

    val shouldStillPlay = testPersistenceManager.update(Seq(mockCustomer)).head
    val lowBankroll = shouldStillPlay.updateBankroll(-45.0)
    val shouldLeave = testPersistenceManager.update(Seq(lowBankroll)).head
    assert(shouldStillPlay.customerState === Playing(mockGame))
    assert(shouldLeave.customerState === Idle)

  test("Customer should Remain Idle if already Idle"):

    val testPersistenceManager =
      PersistenceManager[Customer](bThreshold = 80, fThreshold = 60)

    val mockCustomer = Customer()
      .withCustomerState(Idle)
      .withBoredom(50.0)
      .withFrustration(50.0)
      .withBankroll(40.0)

    val shouldBeIdle = testPersistenceManager.update(Seq(mockCustomer)).head
    assert(shouldBeIdle.customerState === Idle)
