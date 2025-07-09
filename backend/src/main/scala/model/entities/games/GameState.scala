package model.entities.games

import utils.Result

case class GameState(
    currentPlayers: Int,
    maxAllowedPlayers: Int,
    playersId: List[String]
):
  def addPlayer(id: String): Result[GameState, GameState] =
    if (currentPlayers < maxAllowedPlayers)
      Result.Success(
        GameState(currentPlayers + 1, maxAllowedPlayers, playersId :+ id)
      )
    else
      Result.Failure(this)

  def removePlayer(id: String): Result[GameState, GameState] =
    if (currentPlayers > 0)
      Result.Success(
        GameState(
          currentPlayers - 1,
          maxAllowedPlayers,
          playersId.filterNot(s => s != id)
        )
      )
    else
      Result.Failure(this)
