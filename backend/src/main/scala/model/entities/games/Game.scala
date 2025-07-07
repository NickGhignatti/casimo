package model.entities.games

import model.entities.Entity
import utils.Result
import utils.Result.Success
import utils.Vector2D

trait GameType

object GameType:
  object SlotMachine extends GameType
  object Roulette extends GameType
  object Blackjack extends GameType

class Game(
    val id: String,
    val position: Vector2D,
    val gameState: GameState,
    val strategy: GameStrategy
) extends Entity:

  def getGameType: GameType = strategy match
    case _: RouletteStrategyInstance  => GameType.Roulette
    case _: SlotStrategyInstance      => GameType.SlotMachine
    case _: BlackJackStrategyInstance => GameType.Blackjack

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
