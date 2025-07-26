package model.managers.movements

import model.entities.customers.Movable
import model.managers.WeightedManager
import utils.Vector2D

case class RandomMovementManager[M <: Movable[M]](weight: Double = 1)
    extends WeightedManager[M]:
  override def updatedWeight(weight: Double): WeightedManager[M] =
    copy(weight = weight)

  override def update(slice: M): M = slice.addedDirection(
    Vector2D(random(), random()) * weight
  )

  private def random(): Double = (2 * Math.random()) - 1
