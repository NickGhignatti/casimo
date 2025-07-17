package model.entities.customers

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import utils.Vector2D
import model.entities.customers.*
import model.entities.customers.CustState.{Idle, Playing}
import model.entities.customers.RiskProfile.{Impulsive, Regular}
import model.entities.games.{Blackjack, GameBuilder, Roulette, SlotMachine}


class TestCustomerBuilder extends AnyFunSuite with Matchers {
  private val mockGame = GameBuilder.blackjack(Vector2D.zero)

  test("default builder produce customer with default values") {
    val cust = CustomerBuilder().build()
    cust.id should not be empty
    cust.position shouldEqual Vector2D.zero
    cust.direction shouldEqual Vector2D.zero
    cust.bankroll shouldBe 1000.0 +- 0.0001
    cust.riskProfile shouldEqual Regular
    cust.customerState shouldEqual Idle
    cust.betStrategy shouldBe a [FlatBetting[_]]
    cust.favouriteGames shouldBe (Seq(Roulette, Blackjack, SlotMachine))
    cust.isPlaying shouldBe false
  }

  test("builder `.withX` methods override single fields") {
    val customPos = Vector2D(10.0, 20.0)
    val customDir = Vector2D(1.0, 0.0)

    val cust = CustomerBuilder()
      .withId("abc-123")
      .withPosition(customPos)
      .withDirection(customDir)
      .withBankroll(250.0)
      .withRiskProfile(Impulsive)
      .withCustomerState(Playing(mockGame))
      .withBetStrategy(Martingale(10.0, 17))
      .withFavouriteGames(Seq(Roulette, Blackjack))
      .build()

    cust.id shouldEqual "abc-123"
    cust.position shouldEqual customPos
    cust.direction shouldEqual customDir
    cust.bankroll shouldBe 250.0
    cust.riskProfile shouldEqual Impulsive
    cust.customerState shouldEqual Playing(mockGame)
    cust.betStrategy shouldBe a [Martingale[_]]
    cust.favouriteGames shouldEqual Seq(Roulette, Blackjack)
  }

  test("random builder `.random()` generates varied data") {
    val b1 = CustomerBuilder.random().build()
    val b2 = CustomerBuilder.random().build()

    b1.id should not equal b2.id
    (b1.position.x != b2.position.x) || (b1.position.y != b2.position.y) shouldBe true
    b1.bankroll should not equal b2.bankroll
  }

  test("copying builder retains other defaults") {
    val cust = CustomerBuilder()
      .withBankroll(500.0)
      .build()
    cust.position shouldEqual Vector2D.zero
    cust.bankroll shouldBe 500.0
    cust.direction shouldEqual Vector2D.zero
  }
}
