package model.entities.games

import model.entities.Entity
import model.entities.games.GameType.Blackjack
import model.entities.games.GameType.Roulette
import model.entities.games.GameType.SlotMachine
import utils.Result
import utils.Result.Success
import utils.Vector2D

enum GameType:
  case SlotMachine
  case Roulette
  case Blackjack

class Game(
    val id: String,
    val position: Vector2D,
    val gameState: GameState,
    val strategy: GameStrategy
) extends Entity:

  def getGameType: GameType = strategy match
    case _: RouletteStrategyInstance  => Roulette
    case _: SlotStrategyInstance      => SlotMachine
    case _: BlackJackStrategyInstance => Blackjack

  def lock(): Result[Game, Game] = gameState.addPlayer() match
    case Success(newGameState) =>
      Result.Success(
        Game(id, position, newGameState, strategy)
      )
    case _ => Result.Failure(this)

  def unlock(): Result[Game, Game] = gameState.removePlayer() match
    case Success(newGameState) =>
      Result.Success(
        Game(id, position, newGameState, strategy)
      )
    case _ => Result.Failure(this)

  def play() = ???
