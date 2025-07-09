package model.managers

import model.GlobalConfig
import model.entities.customers.CustState.{Idle, Playing}
import model.entities.customers.{
  Bankroll,
  BoredomFrustration,
  CustState,
  CustomerState
}
import model.entities.games.{Game, GameState, SlotStrategyInstance}
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D

case class MockCustomer(
    customerState: CustState = Idle,
    boredom: Double,
    frustration: Double,
    bankroll: Double
) extends CustomerState[MockCustomer],
      BoredomFrustration[MockCustomer],
      Bankroll[MockCustomer]:

  protected def changedState(newState: CustState): MockCustomer =
    this.copy(customerState = newState)

  protected def updatedBoredom(newBoredom: Double): MockCustomer =
    this.copy(boredom = newBoredom)

  protected def updatedFrustration(newFrustration: Double): MockCustomer =
    this.copy(frustration = newFrustration)

  protected def updatedBankroll(newBankroll: Double): MockCustomer =
    this.copy(bankroll = newBankroll)

//val mockGame = Game("test",Vector2D.zero,GameState(1,8),SlotStrategyInstance(5,() => true))
//class TestPersistenceManager extends AnyFunSuite:
//  test("Customer should leave the table when too bored"):
//    given config: GlobalConfig = GlobalConfig()
//    val testPersistenceManager = PersistenceManager[MockCustomer]()
//    val mockCustomer = MockCustomer(
//      customerState = Playing(mockGame),
//      boredom = 81,
//      frustration = 50,
//      bankroll = 40.0
//    )
//    val shouldBeIdle = testPersistenceManager.update(Seq(mockCustomer)).head
//    assert(shouldBeIdle.customerState === Idle)

//  test("Customer should leave the table when too frustrated"):
//    given config: GlobalConfig = GlobalConfig()
//
//    val testPersistenceManager = PersistenceManager[MockCustomer]()
//    val mockCustomer = MockCustomer(
//      customerState = Playing(mockGame),
//      boredom = 30,
//      frustration = 61,
//      bankroll = 40.0
//    )
//    val shouldBeIdle = testPersistenceManager.update(Seq(mockCustomer)).head
//    assert(shouldBeIdle.customerState === Idle)
//
//  test("Customer shouldn't stop playing if not bored or frustrated"):
//
//    given config: GlobalConfig = GlobalConfig()
//
//    val testPersistenceManager = PersistenceManager[MockCustomer]()
//    val mockCustomer = MockCustomer(
//      customerState = Playing(mockGame),
//      boredom = 50,
//      frustration = 50,
//      bankroll = 40.0
//    )
//    val shouldStillPlay = testPersistenceManager.update(Seq(mockCustomer)).head
//    assert(shouldStillPlay.customerState === Playing(mockGame))
//
//  test("Customer should leave the table if is bankroll is too low"):
//
//    given config: GlobalConfig = GlobalConfig()
//
//    val testPersistenceManager = PersistenceManager[MockCustomer]()
//    val mockCustomer = MockCustomer(
//      customerState = Playing(mockGame),
//      boredom = 50,
//      frustration = 50,
//      bankroll = 40.0
//    )
//    val shouldStillPlay = testPersistenceManager.update(Seq(mockCustomer)).head
//    val lowBankroll = shouldStillPlay.updateBankroll(-40.0)
//    val shouldLeave = testPersistenceManager.update(Seq(lowBankroll)).head
//    assert(shouldStillPlay.customerState === Playing(mockGame))
//    assert(shouldLeave.customerState === Idle)
//
//  test("Customer should Remain Idle if already Idle"):
//
//    given config: GlobalConfig = GlobalConfig()
//
//    val testPersistenceManager = PersistenceManager[MockCustomer]()
//    val mockCustomer = MockCustomer(
//      customerState = Idle,
//      boredom = 50,
//      frustration = 50,
//      bankroll = 40.0
//    )
//    val shouldBeIdle = testPersistenceManager.update(Seq(mockCustomer)).head
//    assert(shouldBeIdle.customerState === Idle)
