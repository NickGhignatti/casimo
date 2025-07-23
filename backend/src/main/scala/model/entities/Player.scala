package model.entities

import model.entities.customers.Movable
import model.entities.games.Game
import model.entities.games.GameType

trait Player[T <: Player[T]] extends Movable[T] with Entity:
  def favouriteGames: Seq[GameType]
  def isPlaying: Boolean
  def play(game: Game): T
  def stopPlaying: T
