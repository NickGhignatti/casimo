package model.managers

import model.entities._
import model.entities.customers.CustState.Idle
import model.entities.customers.CustState.Playing
import model.entities.customers.RiskProfile.VIP
import model.entities.customers._
import model.entities.games._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import utils._

class TestDecisionManager extends AnyFunSuite with Matchers:

  test("StopPlaying when boredom/frustration exceed thresholds"):
    val mockGame = GameBuilder.blackjack(Vector2D.zero)

    val customer = Customer()
      .withBoredom(99)
      .withFrustration(99)
      .withCustomerState(Playing(mockGame))
    val losingGame = Gain(customer.id, customer.betStrategy.betAmount)
    val mockGamePlayed = mockGame
      .copy(gameHistory =
        GameHistory(List(losingGame, losingGame, losingGame, losingGame))
      )
      .lock(customer.id)
      .option()
      .get
    val manager = DecisionManager[Customer](List(mockGamePlayed))

    val idle = manager.update(List(customer))
    idle.head.customerState shouldBe Idle

  test("ContinuePlaying when thresholds not exceeded"):
    val mockGame = GameBuilder.blackjack(Vector2D.zero)
    val customer = Customer().withCustomerState(Playing(mockGame))
    val mockGamePlayed = mockGame.lock(customer.id).option().get

    val manager = DecisionManager[Customer](List(mockGamePlayed))

    val playing = manager.update(List(customer))
    playing.head.customerState shouldBe Playing(mockGame)

  test("ChangeStrategy if rule trigger matches"):
    val mockGame = GameBuilder.blackjack(Vector2D.zero)
    val customer = Customer()
      .withProfile(VIP)
      .withCustomerState(Playing(mockGame))
      .withBetStrategy(MartingaleStrat(10, defaultRedBet).copy(lossStreak = 4))
    val mockGamePlayed = mockGame.lock(customer.id).option().get
    val manager = DecisionManager[Customer](List(mockGamePlayed))
    val changeStrat = manager.update(List(customer))
    changeStrat.head.betStrategy shouldBe OscarGrindStrat(
      customer.bankroll * 0.05,
      customer.bankroll,
      defaultRedBet
    )

  test("updatePosition should change position if state was changed"):
    val spawnCustomer = Customer()
      .withPosition(Vector2D(10.0, 10.0))
      .withCustomerState(Playing(GameBuilder.slot(Vector2D.zero)))
    val oldCustomer = spawnCustomer.withPosition(Vector2D(20.0, 20.0))
    val newCustomer = oldCustomer.withPosition(Vector2D.zero).changeState(Idle)
    val updatedCustomer =
      PostDecisionUpdater.updatePosition(List(oldCustomer), List(newCustomer))
    updatedCustomer.head.customerState shouldBe Idle
    updatedCustomer.head.position shouldBe oldCustomer.position
    updatedCustomer.head.favouriteGame should not be oldCustomer.favouriteGame
