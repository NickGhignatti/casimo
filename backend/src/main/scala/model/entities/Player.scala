package model.entities

import model.entities.customers.Movable
import model.entities.games.Game
import model.entities.games.GameType

trait Player[T <: Player[T]] extends Movable[T] with Entity:
  def favouriteGame: GameType
  def isPlaying: Boolean
  def play(game: Game): T
  def stopPlaying: T

trait ChangingFavouriteGamePlayer[T <: ChangingFavouriteGamePlayer[T]]
    extends Player[T]:
  def withFavouriteGame(gameType: GameType): T
