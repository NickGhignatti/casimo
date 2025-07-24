package model.managers

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import model.managers.DecisionManager.*
import model.entities.customers.*
import model.entities.games.*
import model.entities.*
import model.entities.customers.CustState.Playing
import utils.*

class TestDecisionManager extends AnyFunSuite with Matchers:
  
  test("FrustrationAbove trigger returns true when exceeded") :
    val c = Customer().withFrustration(80)
    FrustrationAbove(60).eval(c) shouldBe true

  test("BankrollRatioAbove trigger works correctly") :
    val c = Customer().withBankroll(1000.0).updateBankroll(500.0)
    BankrollRatioAbove(1.4).eval(c) shouldBe true
    BankrollRatioAbove(2.0).eval(c) shouldBe false

  test("And trigger only passes if both do") :
    val c = Customer(900, 1000, 90, 70, Regular, Playing(Game("1", GameType.Blackjack)), flat)
    val trigger = And(FrustrationAbove(60), BoredomAbove(80))
    trigger.eval(c) shouldBe true

  test("Or trigger passes if at least one condition is met") :
    val c = Customer(950, 1000, 85, 30, Regular, Playing(Game("1", GameType.Blackjack)), flat)
    val trigger = Or(FrustrationAbove(50), BoredomAbove(80))
    trigger.eval(c) shouldBe true

  test("Always trigger always passes") :
    val c = Customer(1000, 1000, 0, 0, Regular, Playing(Game("1", GameType.Blackjack)), flat)
    Always.eval(c) shouldBe true

  test("Losses trigger for Martingale strategy works") :
    val c = Customer(1000, 1000, 10, 10, VIP, Playing(Game("1", GameType.Roulette)), martingale.copy(lossStreak = 4))
    Losses(3).eval(c) shouldBe true

  test("StopPlaying when boredom/frustration exceed thresholds") :
    val manager = DecisionManager[Customer](List(Game("1", GameType.Blackjack)))
    val customer = Customer(1000, 1000, 100, 100, Regular, Playing(Game("1", GameType.Blackjack)), flat)
    val decision = manager.buildDecisionTree.eval(customer)
    decision shouldBe a[StopPlaying[?]]

  test("ContinuePlaying when thresholds not exceeded") :
    val manager = DecisionManager[Customer](List(Game("1", GameType.Blackjack)))
    val customer = Customer(1000, 1000, 10, 10, Regular, Playing(Game("1", GameType.Blackjack)), flat)
    val decision = manager.buildDecisionTree.eval(customer)
    decision shouldBe a[ContinuePlaying[?]]

  test("ChangeStrategy if rule trigger matches") :
    val manager = DecisionManager[Customer](List(Game("1", GameType.Blackjack)))
    val customer = Customer(1000, 1000, 10, 10, VIP, Playing(Game("1", GameType.Blackjack)), martingale.copy(lossStreak = 3))
    val decision = manager.buildDecisionTree.eval(customer)
    decision shouldBe a[ChangeStrategy[?]]
