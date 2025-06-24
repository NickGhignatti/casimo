package model.entities.games

import org.junit.Assert.*
import org.junit.Test
import utils.Result.{Failure, Success}
import utils.{Result, Vector2D}

class TestGame {

  @Test
  def testLockWhenAllowed(): Unit =
    val game = Game("TestGame", Vector2D(0.0, 0.0), GameState(0, 1))
    game.lock() match {
      case Success(newGame) => assertEquals(1, newGame.gameState.currentPlayers)
      case _                => fail("Expected success when locking game")
    }

  @Test
  def testLockWhenFull(): Unit = {
    val game = Game("TestGame", Vector2D(0.0, 0.0), GameState(0, 1))
    val oldGameResult: Result[Game, Game] = game.lock()
    assertTrue(oldGameResult.isSuccess)
    val oldGame: Game = oldGameResult.unpack()
    oldGame.lock() match {
      case Failure(newGame) => assertEquals(oldGame, newGame)
      case _                => fail("Expected failure when locking full game")
    }
  }

  @Test
  def testUnlockWhenPossible(): Unit = {
    val game = Game("TestGame", Vector2D(0.0, 0.0), GameState(1, 1))
    game.unlock() match {
      case Success(newGame) => assertEquals(0, newGame.gameState.currentPlayers)
      case _                => fail("Expected success when unlocking game")
    }
  }

  @Test
  def testUnlockWhenNotPossible(): Unit = {
    val game = Game("TestGame", Vector2D(0.0, 0.0), GameState(0, 1))
    game.unlock() match {
      case Failure(newGame) => assertEquals(game, newGame)
      case _ => fail("Expected failure when unlocking empty game")
    }
  }
}
