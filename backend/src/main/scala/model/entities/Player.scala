package model.entities

import model.entities.games.GameType

trait Player[T <: Player[T]]:
  val isPlaying: Boolean
  val favouriteGames: Seq[GameType]

  def withFavouriteGames(newFavGame: Seq[GameType]): T
  def play: T
  def stopPlaying: T
