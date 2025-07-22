package model.managers.movements

import model.entities.Collidable
import model.entities.customers.Movable
import model.managers.WeightedManager
import model.managers.movements.AvoidWallsManager.Context
import model.managers.movements.FOV.canSee
import utils.Vector2D

case class AvoidWallsManager[C <: Movable[C]](
    avoidSquareSize: Double,
    weight: Double = 1
) extends WeightedManager[Context[C]]:
  override def update(slice: Context[C]): Context[C] =
    val movable = slice.movable
    if movable.position.canSee(slice.avoids)(
        movable.position + movable.direction
      )
    then slice
    else slice.copy(movable = movable.withDirection(Vector2D.zero))

  override def updatedWeight(weight: Double): WeightedManager[Context[C]] =
    copy(weight = weight)

object AvoidWallsManager:
  case class Context[C <: Movable[C]](movable: C, avoids: Seq[Collidable])

  private case class Square(center: Vector2D, size: Double) extends Collidable:
    override val width: Double = size
    override val height: Double = size
    override val position: Vector2D =
      Vector2D(center.x - size / 2, center.y - size / 2)
