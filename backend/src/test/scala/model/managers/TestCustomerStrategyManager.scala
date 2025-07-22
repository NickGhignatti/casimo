package model.managers

import model.entities.customers.CustState.Playing
import model.entities.customers.RiskProfile.{Regular, VIP}
import org.scalatest.funsuite.AnyFunSuite
import model.entities.customers.{BettingStrategy, Customer, Martingale, OscarGrind, defaultRedBet}
import model.entities.games.GameBuilder
import org.scalatest.matchers.should.Matchers
import utils.Vector2D

class TestCustomerStrategyManager extends AnyFunSuite, Matchers:

  private def makeManager = new CustomerStrategyManager[Customer]
  private val mockGame = GameBuilder.roulette(Vector2D.zero)
  test("VIP Martingale/Roulette triggered after 4 losses on roulette result in Oscar Grind played"):
    val cust = Customer().withId("testA")
      .withFrustration(10.0)
      .withBoredom(10.0)
      .withCustomerState(Playing(mockGame))
      .withBetStrategy(Martingale[Customer](10.0,40.0,List(5,6,7),4))
      .withBankroll(100.0)
      .withProfile(VIP)
    val out = makeManager.update(Seq(cust))
    assert(out.head.betStrategy.isInstanceOf[OscarGrind[Customer]])


  test("Don't triggering any changeBet should keep the current bet strategy"):
    val strat: BettingStrategy[Customer] = Martingale[Customer](10.0, defaultRedBet)
    val cust = Customer()
      .withCustomerState(Playing(mockGame)).play
      .withBetStrategy(strat)

    val out = makeManager.update(Seq(cust))
    out.head.betStrategy shouldEqual strat

  /*test("Regular Roulette triggered after surpassing the boredom threshold"):
    val cust = Customer()
      .withCustomerState(Playing(mockGame)).play
      .withBetStrategy(OscarGrind[Customer](10.0,1000.0,defaultRedBet))
      .withBoredom(70.0)

    val out = makeManager.update(Seq(cust))
    out.head.betStrategy shouldEqual Martingale(20.0,defaultRedBet)*/
