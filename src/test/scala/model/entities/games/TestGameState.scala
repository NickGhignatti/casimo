package model.entities.games

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.TryValues

class TestGameState extends AnyFunSuite with TryValues:

  test("addPlayer when empty should succeed and increment currentPlayers"):
    val gameState = GameState(0, 5)
    val result = gameState.addPlayer()

    assert(result.getOrElse(gameState).currentPlayers === 1)

  test("addPlayer when full should fail and leave currentPlayers unchanged"):
    val gameState = GameState(5, 5)
    val result = gameState.addPlayer()

    assert(result.isFailure)
    assert(result.getOrElse(gameState).currentPlayers === 5)

  test("removePlayer when full should succeed and decrement currentPlayers"):
    val gameState = GameState(5, 5)
    val result = gameState.removePlayer()

    assert(result.isSuccess)
    assert(result.getOrElse(gameState).currentPlayers === 4)

  test("removePlayer when empty should fail and leave currentPlayers at zero"):
    val gameState = GameState(0, 5)
    val result = gameState.removePlayer()

    assert(result.isFailure)
    assert(result.getOrElse(gameState).currentPlayers === 0)
