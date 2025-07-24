package model.managers

import model.entities.*
import model.entities.customers.*
import model.entities.customers.CustState.{Idle, Playing}
import model.entities.customers.RiskProfile.VIP
import model.entities.games.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import utils.*
import utils.TriggerDSL.*

class TestDecisionManager extends AnyFunSuite with Matchers:
  
  test("FrustrationAbove trigger returns true when exceeded") :
    val c = Customer().withFrustration(80)
    FrustAbove(60).eval(c) shouldBe true

  test("BankrollRatioAbove trigger works correctly") :
    val c = Customer().withBankroll(1000.0).updateBankroll(500.0)
    BrRatioAbove(1.4).eval(c) shouldBe true
    BrRatioAbove(2.0).eval(c) shouldBe false

  test("And trigger only passes if both do") :
    val c = Customer().withFrustration(80).withBoredom(90)
    val trigger: Trigger[Customer] = FrustAbove(60) && BoredomAbove(80)
    trigger.eval(c) shouldBe true

  test("Or trigger passes if at least one condition is met") :
    val c = Customer().withFrustration(90).withBoredom(20)
    val trigger: Trigger[Customer] = FrustAbove(50) || BoredomAbove(80)
    trigger.eval(c) shouldBe true

  test("Always trigger always passes") :
    val c = Customer()
    Always.eval(c) shouldBe true

  test("Losses trigger for Martingale strategy works") :
    val c = Customer().withBetStrategy(MartingaleStrat(10,defaultRedBet).copy(lossStreak = 4))
    Losses(3).eval(c) shouldBe true

  test("StopPlaying when boredom/frustration exceed thresholds") :
    val mockGame = GameBuilder.blackjack(Vector2D.zero)

    val customer = Customer().withBoredom(99).withFrustration(99).withCustomerState(Playing(mockGame))
    val losingGame = Gain(customer.id,customer.betStrategy.betAmount)
    val mockGamePlayed = mockGame.copy(gameHistory = GameHistory(List(losingGame,losingGame,losingGame,losingGame))).lock(customer.id).getOrElse(null)
    val manager = DecisionManager[Customer](List(mockGamePlayed))

    val idle = manager.update(List(customer))
    idle.head.customerState shouldBe Idle

  test("ContinuePlaying when thresholds not exceeded") :
    val mockGame = GameBuilder.blackjack(Vector2D.zero)
    val customer = Customer().withCustomerState(Playing(mockGame))
    val mockGamePlayed = mockGame.lock(customer.id).getOrElse(null)

    val manager = DecisionManager[Customer](List(mockGamePlayed))

    val playing = manager.update(List(customer))
    playing.head.customerState shouldBe Playing(mockGame)

  test("ChangeStrategy if rule trigger matches") :
    val mockGame = GameBuilder.blackjack(Vector2D.zero)
    val customer = Customer()
      .withProfile(VIP)
      .withCustomerState(Playing(mockGame))
      .withBetStrategy(MartingaleStrat(10,defaultRedBet).copy(lossStreak = 4))
    val mockGamePlayed = mockGame.lock(customer.id).getOrElse(null)
    val manager = DecisionManager[Customer](List(mockGamePlayed))
    val changeStrat = manager.update(List(customer))
    changeStrat.head.betStrategy shouldBe OscarGrindStrat(customer.bankroll*0.05,customer.bankroll,defaultRedBet)
