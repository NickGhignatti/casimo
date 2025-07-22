package model.entities.games

case class GainHistory(gains: List[Double]):
  def overallGains: Double = gains.sum

  def update(gain: Double): GainHistory = this.copy(gains = gains :+ gain)
