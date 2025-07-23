package model.managers

import model.entities.customers.BettingStrategy
import model.entities.customers.CustState.Playing
import model.entities.customers.Customer
import model.entities.customers.Martingale
import model.entities.customers.OscarGrind
import model.entities.customers.RiskProfile.VIP
import model.entities.customers.defaultRedBet
import model.entities.games.Gain
import model.entities.games.Game
import model.entities.games.GameBuilder
import model.entities.games.GameHistory
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import utils.Vector2D

class TestCustomerStrategyManager extends AnyFunSuite, Matchers:

  private val mockGame = GameBuilder.roulette(Vector2D.zero)

  private def makeManager(
      games: List[Game]
  ): CustomerStrategyManager[Customer] =
    new CustomerStrategyManager[Customer](games)
  test(
    "VIP Martingale/Roulette triggered after 4 losses on roulette result in Oscar Grind played"
  ):
    val cust = Customer()
      .withId("testA")
      .withFrustration(10.0)
      .withBoredom(10.0)
      .withCustomerState(Playing(mockGame))
      .withBetStrategy(Martingale[Customer](10.0, List(5, 6, 7)))
      .withBankroll(1000.0)
      .withProfile(VIP)
    val losingGame = Gain(cust.id, cust.betStrategy.betAmount)
    val custAfterLosing = cust
      .updateAfter(-cust.betStrategy.betAmount)
      .updateAfter(-cust.betStrategy.betAmount)
      .updateAfter(-cust.betStrategy.betAmount)
      .updateAfter(-cust.betStrategy.betAmount)
    val mockGamePlayed = mockGame
      .copy(gameHistory =
        GameHistory(List(losingGame, losingGame, losingGame, losingGame))
      )
      .lock(cust.id)
      .option()
      .get

    val out = makeManager(List(mockGamePlayed)).update(Seq(custAfterLosing))
    assert(out.head.betStrategy.isInstanceOf[OscarGrind[Customer]])

  test("Don't triggering any changeBet should keep the current bet strategy"):
    val strat: BettingStrategy[Customer] =
      Martingale[Customer](10.0, defaultRedBet)
    val cust = Customer()
      .withCustomerState(Playing(mockGame))
      .withBetStrategy(strat)
    val losingGame = Gain(cust.id, cust.betStrategy.betAmount)
    val mockGamePlayed = mockGame
      .copy(gameHistory = GameHistory(List(losingGame)))
      .lock(cust.id)
      .option()
      .get
    val out = makeManager(List(mockGamePlayed)).update(Seq(cust))
    out.head.betStrategy shouldEqual strat

  test("Regular Roulette triggered after surpassing the boredom threshold"):
    val cust = Customer()
      .withCustomerState(Playing(mockGame))
      .withBetStrategy(OscarGrind[Customer](10.0, 1000.0, defaultRedBet))
      .withBoredom(70.0)

    val losingGame = Gain(cust.id, cust.betStrategy.betAmount)
    val mockGamePlayed = mockGame
      .copy(gameHistory = GameHistory(List(losingGame)))
      .lock(cust.id)
      .option()
      .get
    val out = makeManager(List(mockGamePlayed)).update(Seq(cust))
    out.head.betStrategy shouldEqual Martingale(20.0, defaultRedBet)
