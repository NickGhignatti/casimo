package model.managers.movements

import model.entities.Collidable
import model.entities.customers.Movable
import model.managers.BaseManager
import model.managers.movements.AvoidWallsManager.Context
import model.managers.movements.FOV.canSee
import utils.Vector2D

case class AvoidWallsManager[C <: Movable[C]]() extends BaseManager[Context[C]]:
  override def update(slice: Context[C]): Context[C] =
    val movable = slice.movable
    if movable.position.canSee(slice.avoids)(
        movable.position + movable.direction
      )
    then slice
    else slice.copy(movable = movable.withDirection(Vector2D.zero))

object AvoidWallsManager:
  case class Context[C <: Movable[C]](movable: C, avoids: Seq[Collidable])
