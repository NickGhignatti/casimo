package model.entities.games

import model.Ticker
import model.entities.customers.CustState
import model.entities.customers.CustState.Playing
import model.entities.customers.Customer
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D

class TestGameResolver extends AnyFunSuite:
  val instantTicker: Ticker = Ticker(0, 1, 1, 1)
  val normalTicker: Ticker = Ticker(0) // 12, 60, 42

  test("game resolver should return a list of updated games with 1 element"):
    val mockGame = GameBuilder.slot(Vector2D.zero)
    val mockCustomer = Customer().withCustomerState(Playing(mockGame))

    val newGame = GameResolver.update(
      List(mockCustomer),
      List(mockGame),
      instantTicker.update()
    )

    assert(mockGame.gameHistory.gains != newGame.last.gameHistory.gains)

  test(
    "game resolver should return a list of updated games with 2 element also if customer is playing only a game"
  ):
    val mockGame = GameBuilder.slot(Vector2D.zero)
    val mockGame2 = GameBuilder.slot(Vector2D.zero)
    val mockCustomer = Customer().withCustomerState(Playing(mockGame))
    val newGame =
      GameResolver.update(
        List(mockCustomer),
        List(mockGame, mockGame2),
        instantTicker.update()
      )

    assert(
      mockGame.gameHistory.gains :+ mockGame2.gameHistory.gains != newGame.last.gameHistory.gains
    )

  test("if customers are not playing any game there should not be updates"):
    val mockGame = GameBuilder.slot(Vector2D.zero)
    val mockGame2 = GameBuilder.slot(Vector2D.zero)
    val mockCustomer = Customer()
    val newGame =
      GameResolver.update(
        List(mockCustomer),
        List(mockGame, mockGame2),
        instantTicker.update()
      )

    assert(
      mockGame.gameHistory.gains ::: mockGame2.gameHistory.gains == newGame.last.gameHistory.gains
    )

  test("GameResolver should update only a type of games after 12 ticks"):
    val ticked =
      (0 until normalTicker.slotTick.toInt).foldLeft(normalTicker)((t, _) =>
        t.update()
      )

    val mockSlotGame = GameBuilder
      .slot(Vector2D.zero)
      .lock("c1")
      .getOrElse(GameBuilder.slot(Vector2D.zero))
    val mockRoulette = GameBuilder
      .roulette(Vector2D.zero)
      .lock("c2")
      .getOrElse(GameBuilder.roulette(Vector2D.zero))
    val mockCustomer = Customer().withId("c1").play(mockSlotGame)
    val mockCustomer2 = Customer().withId("c2").play(mockRoulette)

    val newGames = GameResolver.update(
      List(mockCustomer, mockCustomer2),
      List(mockSlotGame, mockRoulette),
      ticked
    )

    assert(newGames.contains(mockRoulette) && !newGames.contains(mockSlotGame))

  test(
    "After that a game is played the flag should stay true for an update loop"
  ):
    val ticked =
      (0 until normalTicker.slotTick.toInt).foldLeft(normalTicker)((t, _) =>
        t.update()
      )

    val mockGame = GameBuilder
      .slot(Vector2D.zero)
      .lock("c1")
      .getOrElse(GameBuilder.slot(Vector2D.zero))
    val mockCustomer =
      Customer().withId("c1").withCustomerState(Playing(mockGame))

    val newGame = GameResolver.update(
      List(mockCustomer),
      List(mockGame),
      ticked
    )

    assert(newGame.head.lastRoundHasPlayed)
    assert(newGame.head.getLastRoundResult.nonEmpty)

    val newTick = ticked.update()
    val lastGame = GameResolver.update(List(mockCustomer), newGame, newTick)

    assert(!lastGame.head.lastRoundHasPlayed)
    assert(lastGame.head.getLastRoundResult.isEmpty)
