package model.games

import utils.{Result, Vector2D}

class Game(val id: String, val position: Vector2D, val allowedPlayer: Int) {
  def lock(): Result[String, String] = ???
  def unlock(): Result[String, String] = ???
  def play() = ???
}
