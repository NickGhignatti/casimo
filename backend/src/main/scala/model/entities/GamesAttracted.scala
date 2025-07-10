package model.entities

import model.entities.customers.Movable
import model.entities.games.GameType

trait GamesAttracted[T <: GamesAttracted[T]] extends Movable[T]:
  def favouriteGames: Seq[GameType]
