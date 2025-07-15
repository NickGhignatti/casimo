package model.managers

trait WeightedManager[C] extends BaseManager[C]:
  def weight: Double
  def updatedWeight(weight: Double): WeightedManager[C]

object WeightedManager:
  extension [C](weight: Double)
    def *(manager: WeightedManager[C]): WeightedManager[C] =
      manager.updatedWeight(weight)
