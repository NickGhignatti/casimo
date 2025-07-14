package model.entities.games

case class GameHistory(gains: List[Double]):
  def overallGains: Double = gains.sum

  def update(gain: Double): GameHistory = this.copy(gains = gains :+ gain)
