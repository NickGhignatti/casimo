package model.managers

/** A manager that can be tuned using the * operator
  * @tparam C
  *   the customer concrete type
  */
trait WeightedManager[C] extends BaseManager[C]:
  /** @return
    *   the weight which will be applied when using the `update` method
    */
  def weight: Double

  /** @param weight
    *   the new value for weight
    * @return
    *   a new manager with the given weight
    */
  def updatedWeight(weight: Double): WeightedManager[C]

object WeightedManager:
  extension [C](weight: Double)
    def *(manager: WeightedManager[C]): WeightedManager[C] =
      manager.updatedWeight(weight)
