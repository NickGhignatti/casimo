package model.entities.games

import model.entities.Entity
import utils.Result.Success
import utils.{Result, Vector2D}

class Game(
    val id: String,
    val position: Vector2D,
    val gameState: GameState
) extends Entity {

  def lock(): Result[Game, Game] = gameState.addPlayer() match {
    case Success(newGameState) =>
      Result.Success(
        Game(id, position, newGameState)
      )
    case _ => Result.Failure(this)
  }

  def unlock(): Result[Game, Game] = gameState.removePlayer() match {
    case Success(newGameState) =>
      Result.Success(
        Game(id, position, newGameState)
      )
    case _ => Result.Failure(this)
  }

  def play() = ???
}
