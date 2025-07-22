package model.entities.games

class Gain(from: String, of: Double):
  def getMoneyGain: Double = this.of
  def getCustomerWhichPlayed: String = this.from

case class GameHistory(gains: List[Gain]):
  def overallGains: Double = gains.map(_.getMoneyGain).sum

  def update(customerId: String, gain: Double): GameHistory =
    this.copy(gains = gains :+ Gain(customerId, gain))
