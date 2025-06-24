package model.entities.games

import org.scalatest.funsuite.AnyFunSuite
import utils.Result.{Failure, Success}
import utils.Vector2D

class TestGame extends AnyFunSuite {

  test("lock should succeed when under capacity") {
    val game = Game("TestGame", Vector2D(0.0, 0.0), GameState(0, 1))
    game.lock() match {
      case Success(newGame) => assert(newGame.gameState.currentPlayers === 1)
      case _                => fail("Expected Success when locking game")
    }
  }

  test("lock should fail when at capacity") {
    val game = Game("TestGame", Vector2D(0.0, 0.0), GameState(0, 1))
    val first = game.lock()
    assert(first.isSuccess, "First lock should succeed")

    val lockedGame = first.unpack()
    lockedGame.lock() match {
      case Failure(newGame) => assert(newGame === lockedGame)
      case _                => fail("Expected Failure when locking full game")
    }
  }

  test("unlock should succeed when players present") {
    val game = Game("TestGame", Vector2D(0.0, 0.0), GameState(1, 1))
    game.unlock() match {
      case Success(newGame) => assert(newGame.gameState.currentPlayers === 0)
      case _                => fail("Expected Success when unlocking game")
    }
  }

  test("unlock should fail when no players") {
    val game = Game("TestGame", Vector2D(0.0, 0.0), GameState(0, 1))
    game.unlock() match {
      case Failure(newGame) => assert(newGame === game)
      case _ => fail("Expected Failure when unlocking empty game")
    }
  }
}
