package model.entities.spawner

case class TimeManager(currentTime: Int, tickToSignal: Int):
  def isReady: Boolean = currentTime == tickToSignal
  def tick: TimeManager = this.copy(currentTime = currentTime + 1)
