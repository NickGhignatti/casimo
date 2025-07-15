package model.entities.games

import model.entities.games.GameType._
import model.entities.games.dsl.use
import org.scalatest.funsuite.AnyFunSuite
import utils.Result.Failure
import utils.Result.Success
import utils.Vector2D

class TestGame extends AnyFunSuite:
  val strategy: SlotStrategyInstance = use(SlotStrategy) bet 5.0 when (true)

  test("lock should succeed when under capacity"):
    val game = Game("TestGame", Vector2D(0.0, 0.0), GameState(0, 1), strategy)
    game.lock() match
      case Success(newGame) => assert(newGame.gameState.currentPlayers === 1)
      case _                => fail("Expected Success when locking game")

  test("lock should fail when at capacity"):
    val game = Game("TestGame", Vector2D(0.0, 0.0), GameState(0, 1), strategy)
    val first = game.lock()
    assert(first.isSuccess, "First lock should succeed")

    val lockedGame = first.getOrElse(game)
    lockedGame.lock() match
      case Failure(newGame) => assert(newGame === lockedGame)
      case _                => fail("Expected Failure when locking full game")

  test("unlock should succeed when players present"):
    val game = Game("TestGame", Vector2D(0.0, 0.0), GameState(1, 1), strategy)
    game.unlock() match
      case Success(newGame) => assert(newGame.gameState.currentPlayers === 0)
      case _                => fail("Expected Success when unlocking game")

  test("unlock should fail when no players"):
    val game = Game("TestGame", Vector2D(0.0, 0.0), GameState(0, 1), strategy)
    game.unlock() match
      case Failure(newGame) => assert(newGame === game)
      case _ => fail("Expected Failure when unlocking empty game")

  test("GameType should be roulette when applied RouletteStrategy"):
    val rouletteStrategy = use(RouletteStrategy) bet 5.0 when (true)
    val game =
      Game("TestGame", Vector2D.zero, GameState(0, 1), rouletteStrategy)
    assert(game.getGameType == Roulette)

  test("GameType should be slot when applied SlotMachineStrategy"):
    val game =
      Game("TestGame", Vector2D.zero, GameState(0, 1), strategy)
    assert(game.getGameType == SlotMachine)

  test("GameType should be blackjack when applied BlackJackStrategy"):
    val blackJackStrategy = use(BlackJackStrategy) bet 5.0 when (true)
    val game =
      Game("TestGame", Vector2D.zero, GameState(0, 1), blackJackStrategy)
    assert(game.getGameType == Blackjack)
