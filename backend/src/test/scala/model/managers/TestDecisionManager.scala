package model.managers

import model.Ticker
import model.entities.*
import model.entities.customers.*
import model.entities.customers.CustState.{Idle, Playing}
import model.entities.customers.RiskProfile.{Impulsive, VIP}
import model.entities.games.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import utils.*
import utils.TriggerDSL.*

class TestDecisionManager extends AnyFunSuite with Matchers:
  val normalTicker: Ticker = Ticker(60) // 12, 60, 42

  test("StopPlaying when boredom/frustration exceed thresholds") :
    val ticked =
      (0 until normalTicker.blackjackTick.toInt).foldLeft(normalTicker)((t, _) =>
        t.update()
      )
    val mockGame = GameBuilder.blackjack(Vector2D.zero)
    val customer = Customer().withBoredom(99).withFrustration(99).withCustomerState(Playing(mockGame))
    val mockGamePlayed = mockGame.lock(customer.id).getOrElse(null)
    val newGame = GameResolver.update(
      List(customer),
      List(mockGamePlayed),
      ticked
    )
    val manager = DecisionManager[Customer](newGame)

    val idle = manager.update(List(customer))
    idle.head.customerState shouldBe Idle

  test("Step Strategy should update correctly"):
    val ticked =
      (0 until normalTicker.blackjackTick.toInt).foldLeft(normalTicker)((t, _) =>
        t.update()
      )

    val mockGame = GameBuilder.blackjack(Vector2D.zero)

    val customer = Customer().withCustomerState(Playing(mockGame)).withBetStrategy(MartingaleStrat(10.0,defaultRedBet))
    val losingGame = Gain(customer.id,customer.betStrategy.betAmount)
    val mockGamePlayed = mockGame.copy(gameHistory = GameHistory(List(losingGame,losingGame,losingGame,losingGame))).lock(customer.id).getOrElse(null)
    val newGame = GameResolver.update(
      List(customer),
      List(mockGamePlayed),
      ticked
    )

    val manager = DecisionManager[Customer](newGame)
    val doubled = manager.update(List(customer))
    if newGame.head.getLastRoundResult.head.getMoneyGain > 0 then
      doubled.head.placeBet().amount shouldBe 20.0
    else
      doubled.head.placeBet().amount shouldBe 10.0

  test("ContinuePlaying when thresholds not exceeded") :
    val ticked =
      (0 until normalTicker.blackjackTick.toInt).foldLeft(normalTicker)((t, _) =>
        t.update()
      )
    val mockGame = GameBuilder.blackjack(Vector2D.zero)
    val customer = Customer().withCustomerState(Playing(mockGame))
    val mockGamePlayed = mockGame.lock(customer.id).getOrElse(null)
    val newGame = GameResolver.update(
      List(customer),
      List(mockGamePlayed),
      ticked
    )
    val manager = DecisionManager[Customer](newGame)

    val playing = manager.update(List(customer))
    playing.head.customerState shouldBe Playing(mockGame)

  test("ChangeStrategy if rule trigger matches") :
    val ticked =
      (0 until normalTicker.blackjackTick.toInt).foldLeft(normalTicker)((t, _) =>
        t.update()
      )
    val mockGame = GameBuilder.blackjack(Vector2D.zero)
    val customer = Customer()
      .withProfile(VIP)
      .withCustomerState(Playing(mockGame))
      .withBetStrategy(MartingaleStrat(10,defaultRedBet).copy(lossStreak = 4))
    val mockGamePlayed = mockGame.lock(customer.id).getOrElse(null)
    val newGame = GameResolver.update(
      List(customer),
      List(mockGamePlayed),
      ticked
    )
    val manager = DecisionManager[Customer](newGame)
    val changeStrat = manager.update(List(customer))
    changeStrat.head.betStrategy shouldBe OscarGrindStrat(customer.bankroll*0.05,customer.bankroll,defaultRedBet)

  test("updatePosition should change position if state was changed"):
    val spawnCustomer = Customer().withPosition(Vector2D(10.0,10.0)).withCustomerState(Playing(GameBuilder.slot(Vector2D.zero)))
    val oldCustomer = spawnCustomer.withPosition(Vector2D(20.0,20.0))
    val newCustomer = oldCustomer.withPosition(Vector2D.zero).changeState(Idle)
    val updatedCustomer = PostDecisionUpdater.updatePosition(List(oldCustomer),List(newCustomer))
    updatedCustomer.head.customerState shouldBe Idle
    updatedCustomer.head.position shouldBe oldCustomer.position

  test("Leave the casino when condition triggered"):
    val customer = Customer().withBoredom(99).withFrustration(99).withCustomerState(Idle).withProfile(Impulsive)
    val manager = DecisionManager[Customer](Nil)
    val nobody = manager.update(List(customer))
    nobody.isEmpty shouldBe true

  test("Unlock the game when stop playing"):
    val ticked =
      (0 until normalTicker.blackjackTick.toInt).foldLeft(normalTicker)((t, _) =>
        t.update()
      )
    val mockGame = GameBuilder.blackjack(Vector2D.zero)
    val secondGame = GameBuilder.slot(Vector2D.zero)
    val customer = Customer().withBoredom(99).withFrustration(99).withCustomerState(Playing(mockGame))
    val mockGamePlayed = mockGame.lock(customer.id).getOrElse(null)
    val newGame = GameResolver.update(
      List(customer),
      List(mockGamePlayed,secondGame),
      ticked
    )
    val manager = DecisionManager[Customer](newGame)
    val idle = manager.update(List(customer))
    val updatedGames = PostDecisionUpdater.updateGames(List(customer),idle,newGame)
    
    updatedGames.size shouldBe 2
    newGame.head.gameState.currentPlayers shouldBe 1
    updatedGames.head.gameState.currentPlayers shouldBe 0
