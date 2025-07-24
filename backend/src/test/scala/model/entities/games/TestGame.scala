package model.entities.games

import model.entities.customers.Customer
import org.scalatest.funsuite.AnyFunSuite
import utils.Result.Failure
import utils.Result.Success
import utils.{Result, Vector2D}

class TestGame extends AnyFunSuite:
  val mockId = "test"

  test("lock should succeed when under capacity"):
    val game = GameBuilder.slot(Vector2D.zero)
    game.lock(mockId) match
      case Success(newGame) => assert(newGame.gameState.currentPlayers === 1)
      case _                => fail("Expected Success when locking game")

  test("lock should fail when at capacity"):
    val game = GameBuilder.slot(Vector2D.zero)
    val first = game.lock(mockId)
    assert(first.isSuccess, "First lock should succeed")

    val lockedGame = first.getOrElse(game)
    lockedGame.lock(mockId) match
      case Failure(newGame) => assert(newGame === lockedGame)
      case _                => fail("Expected Failure when locking full game")

  test("unlock should succeed when players present"):
    val game = GameBuilder.slot(Vector2D.zero)
    val first = game.lock(mockId)
    assert(first.isSuccess, "First lock should succeed")
    first.getOrElse(game).unlock(mockId) match
      case Success(newGame) => assert(newGame.gameState.currentPlayers === 0)
      case _                => fail("Expected Success when unlocking game")

  test("unlock should fail when no players"):
    val game = GameBuilder.slot(Vector2D.zero)
    game.unlock(mockId) match
      case Failure(newGame) => assert(newGame === game)
      case _ => fail("Expected Failure when unlocking empty game")

  test("GameType should be roulette when created a roulette"):
    val game = GameBuilder.roulette(Vector2D.zero)
    assert(game.gameType == Roulette)

  test("GameType should be slot when created a slot"):
    val game = GameBuilder.slot(Vector2D.zero)
    assert(game.gameType == SlotMachine)

  test("GameType should be blackjack when created a blackjack"):
    val game = GameBuilder.blackjack(Vector2D.zero)
    assert(game.gameType == Blackjack)

  test("Roulette should not fail if applied RouletteBet"):
    val game = GameBuilder.roulette(Vector2D.zero)
    assert(game.play(RouletteBet(10.0, List(10))).isSuccess)

  test("Roulette should fail if not applied RouletteBet"):
    val game = GameBuilder.roulette(Vector2D.zero)
    assert(game.play(SlotBet(10.0)).isFailure)

  test("SlotMachine should not fail if applied SlotBet"):
    val game = GameBuilder.slot(Vector2D.zero)
    assert(game.play(SlotBet(10.0)).isSuccess)

  test("SlotMachine should fail if not applied SlotBet"):
    val game = GameBuilder.slot(Vector2D.zero)
    assert(game.play(RouletteBet(10.0, List(10))).isFailure)

  test("BlackJack should not fail if applied BlackJackBet"):
    val game = GameBuilder.blackjack(Vector2D.zero)
    assert(game.play(BlackJackBet(10.0, 18)).isSuccess)

  test("BlackJack should fail if not applied RouletteBet"):
    val game = GameBuilder.blackjack(Vector2D.zero)
    assert(game.play(SlotBet(10.0)).isFailure)

  test("Roulette Gametype should remain unchanged after lock operations"):
    val rouletteGame = GameBuilder.roulette(Vector2D.zero)

    rouletteGame.lock(mockId) match
      case Success(lockedGame) =>
        assert(lockedGame.gameType == Roulette)
        assert(lockedGame.gameType == rouletteGame.gameType)
      case _ => fail("Lock should succeed")

  test("Slot GameType should remain unchanged after lock operations"):
    val slotGame = GameBuilder.slot(Vector2D.zero)

    slotGame.lock(mockId) match
      case Success(lockedGame) =>
        assert(lockedGame.gameType == SlotMachine)
        assert(lockedGame.gameType == slotGame.gameType)
      case _ => fail("Lock should succeed")

  test("BlackJack GameType should remain unchanged after lock operations"):
    val blackjackGame = GameBuilder.blackjack(Vector2D.zero)

    blackjackGame.lock(mockId) match
      case Success(lockedGame) =>
        assert(lockedGame.gameType == Blackjack)
        assert(lockedGame.gameType == blackjackGame.gameType)
      case _ => fail("Lock should succeed")

  test("Roulette GameType should remain unchanged after unlock operations"):
    val rouletteGame = GameBuilder.roulette(Vector2D.zero)
    val lockedGame = rouletteGame.lock(mockId) match
      case Success(lock) => lock
      case _             => fail("Lock should succedd")

    lockedGame.unlock(mockId) match
      case Success(unlockedGame) =>
        assert(unlockedGame.gameType == Roulette)
        assert(
          rouletteGame.gameState.currentPlayers == unlockedGame.gameState.currentPlayers
        )
      case _ => fail("Unlock should succeed")

  test("Slot GameType should remain unchanged after unlock operations"):
    val slotGame = GameBuilder.slot(Vector2D.zero)
    val lockedGame = slotGame.lock(mockId) match
      case Success(lock) => lock
      case _             => fail("Lock should succedd")

    lockedGame.unlock(mockId) match
      case Success(unlockedGame) =>
        assert(unlockedGame.gameType == SlotMachine)
        assert(
          slotGame.gameState.currentPlayers == unlockedGame.gameState.currentPlayers
        )
      case _ => fail("Unlock should succeed")

  test("BlackJack GameType should remain unchanged after unlock operations"):
    val blackjackGame = GameBuilder.blackjack(Vector2D.zero)
    val lockedGame = blackjackGame.lock(mockId) match
      case Success(lock) => lock
      case _             => fail("Lock should succedd")

    lockedGame.unlock(mockId) match
      case Success(unlockedGame) =>
        assert(unlockedGame.gameType == Blackjack)
        assert(
          blackjackGame.gameState.currentPlayers == unlockedGame.gameState.currentPlayers
        )
      case _ => fail("Unlock should succeed")

  private def checkPropertiesExceptHistory(
      oldGame: Game,
      newGame: Game
  ): Boolean =
    oldGame.gameType == newGame.gameType &&
      oldGame.position == newGame.position &&
      oldGame.gameState == newGame.gameState &&
      oldGame.id == newGame.id &&
      oldGame.width == newGame.width &&
      oldGame.height == newGame.height

  test(
    "updateHistory should return new Game instance with same properties except history"
  ):
    val oldRoulette = GameBuilder.roulette(Vector2D(5.0, 10.0))
    val newRoulette = oldRoulette.updateHistory("player1", 25.0)

    assert(checkPropertiesExceptHistory(oldRoulette, newRoulette))
    assert(oldRoulette ne newRoulette)

    val oldSlot = GameBuilder.slot(Vector2D(5.0, 10.0))
    val newSlot = oldSlot.updateHistory("player1", 25.0)

    assert(checkPropertiesExceptHistory(oldSlot, newSlot))
    assert(oldSlot ne newSlot)

    val oldBlackjack = GameBuilder.blackjack(Vector2D(5.0, 10.0))
    val newBlackjack = oldBlackjack.updateHistory("player1", 25.0)

    assert(checkPropertiesExceptHistory(oldBlackjack, newBlackjack))
    assert(oldBlackjack ne newBlackjack)

  test("isFull should delegate to gameState.isFull"):
    val game = GameBuilder.slot(Vector2D.zero)
    assert(!game.isFull)

    val lockedGame = game.lock(mockId).getOrElse(game)
    assert(lockedGame.isFull)

  test(
    "getLastRoundResult should delegate to gameHistory with correct player count"
  ):
    val game = GameBuilder.roulette(Vector2D.zero)
    val results = game.getLastRoundResult
    assert(results.isEmpty)

  test("geLastRoundResult should return the correct history gains"):
    val game = GameBuilder.roulette(Vector2D.zero)
    val c1 = Customer().withId("c1")
    val c2 = Customer().withId("c2")
    val c3 = Customer().withId("c3")

    val g1 = game.lock(c1.id)
    val g2 = g1.getOrElse(game).lock(c2.id).getOrElse(game)

    assert(g2.gameState.currentPlayers == 2)

    val nc1 = c1.play(game)
    val nc2 = c2.play(game)

    val newGames = GameResolver.update(List(nc1, nc2), List(g2))

    assert(newGames.head.getLastRoundResult.size == 2)

    val g3 = newGames.head.lock(c3.id).getOrElse(g2)
    val nc3 = c3.play(game)

    assert(g3.gameState.currentPlayers == 3)

    val newerGames = GameResolver.update(List(nc1, nc2, nc3), List(g3))

    assert(newerGames.head.getLastRoundResult.size == 3)
    assert(newerGames.head.gameHistory.gains.size == 5)

  test("games when builded should have predefined size"):
    val slot = GameBuilder.slot(Vector2D.zero)
    val roulette = GameBuilder.roulette(Vector2D.zero)
    val blackjack = GameBuilder.blackjack(Vector2D.zero)

    assert(slot.height == 20 && slot.width == 20)
    assert(roulette.height == 30 && roulette.width == 30)
    assert(blackjack.height == 40 && blackjack.width == 70)

  test("new games should have 0 as bankrolls"):
    val slot = GameBuilder.slot(Vector2D.zero)

    assert(slot.bankroll == 0)

  test("games after some play should not have bankrolls to 0"):
    val slot = GameBuilder.slot(Vector2D.zero)

    val res1 = slot.play(SlotBet(5.0))
    val newSlot = slot.updateHistory(
      mockId,
      res1.getOrElse(Success(5.0)) match
        case Result.Success(value) => -value
        case Result.Failure(error) => error
    )
    val res2 = newSlot.play(SlotBet(5.0))
    val newSlot2 = newSlot.updateHistory(
      mockId,
      res2.getOrElse(Success(5.0)) match
        case Result.Success(value) => -value
        case Result.Failure(error) => error
    )

    assert(newSlot2.bankroll != 0 && newSlot2.gameHistory.gains.size == 2)
