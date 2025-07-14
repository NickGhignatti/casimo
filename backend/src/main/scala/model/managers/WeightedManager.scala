package model.managers

import model.GlobalConfig
import model.entities.customers.Movable
import utils.Vector2D

private case class WeightedMovable[M <: Movable[M]](
    movable: Movable[M],
    weight: Double
) extends Movable[M]:
  export movable.{position, direction, updatedPosition}
  override def updatedDirection(newDirection: Vector2D): M =
    movable.updatedDirection(newDirection * weight)

extension [M <: Movable[M]](weight: Double)
  def *(manager: BaseManager[Movable[M]]): BaseManager[Movable[M]] =
    new BaseManager[Movable[M]]:
      override def update(slice: Movable[M])(using
          config: GlobalConfig
      ): Movable[M] =
        WeightedMovable(slice, weight) | manager
