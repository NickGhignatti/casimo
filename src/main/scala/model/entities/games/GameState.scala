package model.entities.games

import utils.Result

case class GameState(currentPlayers: Int, maxAllowedPlayers: Int) {
  def addPlayer(): Result[GameState, GameState] =
    if (currentPlayers < maxAllowedPlayers) {
      Result.Success(GameState(currentPlayers + 1, maxAllowedPlayers))
    } else {
      Result.Failure(this)
    }

  def removePlayer(): Result[GameState, GameState] =
    if (currentPlayers > 0) {
      Result.Success(GameState(currentPlayers - 1, maxAllowedPlayers))
    } else {
      Result.Failure(this)
    }
}
