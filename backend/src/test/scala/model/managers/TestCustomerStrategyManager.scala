package model.managers

import model.entities.customers.CustState.Playing
import model.entities.customers.RiskProfile.{Regular, VIP}
import org.scalatest.funsuite.AnyFunSuite
import model.entities.customers.{Customer, Martingale, OscarGrind}
import model.entities.games.GameBuilder
import utils.Vector2D

class TestCustomerStrategyManager extends AnyFunSuite:

  private def makeManager = new CustomerStrategyManager[Customer]

  test("VIP Martingale/Roulette triggered after 4 losses on roulette result in Oscar Grind played") {
    val cust = Customer().withId("testA")
      .withFrustration(10.0)
      .withBoredom(10.0)
      .withCustomerState(Playing(GameBuilder.roulette(Vector2D.zero)))
      .withBetStrategy(Martingale[Customer](10.0,40.0,List(5,6,7),4))
      .withBankroll(100.0)
      .withProfile(VIP)
    val out = makeManager.update(Seq(cust))
    assert(out.head.betStrategy.isInstanceOf[OscarGrind[Customer]])
  }



case class TestCustomer(
                         id: Int,
                         frustration: Double,
                         boredom: Double,
                         lossStreak: Int,
                         bankrollStart: Double,
                         bankrollCurrent: Double,
                         currentGame: String,
                         profile: String,
                         var strategy: String = ""
                       )
//  test("Stop-loss triggers flat low bet") {
//    val cust = TestCustomer(3, 10, 10, 0, 100.0, 40.0, "slot", "vip")
//    val out = makeManager.update(Seq(cust))
//    assert(out.head.strategy.matches("FlatBet\\(4\\.0.*"))
//  }
//
//  test("Take-profit triggers flat low bet") {
//    val cust = TestCustomer(4, 10, 10, 0, 100.0, 160.0, "blackjack", "vip")
//    val out = makeManager.update(Seq(cust))
//    assert(out.head.strategy.matches("FlatBet\\(1\\.6.*"))
//  }
//
//  test("Default flat bet at 2%") {
//    val cust = TestCustomer(5, 10, 10, 0, 100.0, 80.0, "slot", "casual")
//    val out = makeManager.update(Seq(cust))
//    assert(out.head.strategy.matches("FlatBet\\(1\\.6.*"))
//  }
//
//  test("Stop_frust triggers 1% flat on frustration") {
//    val cust = TestCustomer(6, 60.0, 10, 0, 100.0, 100.0, "blackjack", "regular")
//    val out = makeManager.update(Seq(cust))
//    val base = 1.00
//    assert(out.head.strategy.matches(s"FlatBet\\($base.*"))
//  }
//
//  test("Stop_bored triggers 1% flat on boredom") {
//    val cust = TestCustomer(7, 10.0, 60.0, 0, 100.0, 100.0, "slot", "casual")
//    val out = makeManager.update(Seq(cust))
//    val base = 1.0
//    assert(out.head.strategy.matches(s"FlatBet\\($base.*"))
//  }
//
//  test("Parsing Params – martingale 3% of bankroll") {
//    val cust = TestCustomer(8, 10, 10, 2, 100.0, 200.0, "roulette", "regular")
//    val out = makeManager.update(Seq(cust))
//    val expected = 200.0 * 0.03
//    assert(out.head.strategy.contains(f"MartingaleStrategy\\($expected%.2f"))
//  }
//
//  test("VIP impulsive with loss streak uses martingale then oscar after 3") {
//    val c2 = TestCustomer(9, 10, 10, 2, 100.0, 200.0, "blackjack", "impulsive")
//    val c3 = c2.copy(lossStreak = 3)
//    val out2 = makeManager.update(Seq(c2))
//    val out3 = makeManager.update(Seq(c3))
//    assert(out2.head.strategy.contains("MartingaleStrategy"))
//    assert(out3.head.strategy.contains("OscarGrindStrategy"))
//  }

