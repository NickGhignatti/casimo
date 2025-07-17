package model.entities

import model.entities.customers.Movable
import model.entities.games.GameType

trait Player[T <: Player[T]] extends Movable[T] with Entity:
  def favouriteGames: Seq[GameType]
  def play: T
  def stopPlaying: T
