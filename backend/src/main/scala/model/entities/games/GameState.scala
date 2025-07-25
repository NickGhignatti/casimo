package model.entities.games

import utils.Result

/** Represents the current state of player occupancy for a game.
  *
  * Tracks the number of players currently engaged with a game, enforces
  * capacity limits, and maintains a list of player identifiers. This immutable
  * data structure ensures thread-safe state management and provides methods for
  * adding and removing players with proper validation.
  *
  * @param currentPlayers
  *   the number of players currently in the game
  * @param maxAllowedPlayers
  *   the maximum capacity of players for this game
  * @param playersId
  *   the list of unique identifiers for all current players
  */
case class GameState(
    currentPlayers: Int,
    maxAllowedPlayers: Int,
    playersId: List[String]
):
  /** Checks if the game has reached its maximum player capacity.
    *
    * @return
    *   true if the game is at full capacity, false otherwise
    */
  def isFull: Boolean = currentPlayers == maxAllowedPlayers

  /** Attempts to add a new player to the game.
    *
    * Validates that the game has available capacity before adding the player.
    * If successful, returns a new GameState with incremented player count and
    * the player's ID added to the list. If the game is full, returns the
    * current state as a failure.
    *
    * @param id
    *   the unique identifier of the player to add
    * @return
    *   Success with updated GameState if space available, Failure with current
    *   state if full
    */
  def addPlayer(id: String): Result[GameState, GameState] =
    if (currentPlayers < maxAllowedPlayers)
      Result.Success(
        GameState(currentPlayers + 1, maxAllowedPlayers, playersId :+ id)
      )
    else
      Result.Failure(this)

  /** Attempts to remove a player from the game.
    *
    * Validates that there are players to remove before proceeding. If
    * successful, returns a new GameState with decremented player count and the
    * player's ID removed from the list. If no players are present, returns the
    * current state as a failure.
    *
    * @param id
    *   the unique identifier of the player to remove
    * @return
    *   Success with updated GameState if players exist, Failure with current
    *   state if empty
    */
  def removePlayer(id: String): Result[GameState, GameState] =
    if (currentPlayers > 0)
      Result.Success(
        GameState(
          currentPlayers - 1,
          maxAllowedPlayers,
          playersId.filterNot(s => s == id)
        )
      )
    else
      Result.Failure(this)
