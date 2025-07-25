package model.managers.movements

import model.entities.Collidable
import model.entities.customers.Movable
import model.managers.BaseManager
import model.managers.movements.AvoidObstaclesManager.Context
import model.managers.movements.FOV.canSee
import utils.Vector2D

/** This manager will zero the movable direction if it is going to collide with
  * an obstacle
  * @tparam C
  *   the customer concrete type
  */
case class AvoidObstaclesManager[C <: Movable[C]]()
    extends BaseManager[Context[C]]:
  override def update(slice: Context[C]): Context[C] =
    val movable = slice.movable
    if movable.position.canSee(slice.avoids)(
        movable.position + movable.direction
      )
    then slice
    else slice.copy(movable = movable.withDirection(Vector2D.zero))

object AvoidObstaclesManager:
  case class Context[C <: Movable[C]](movable: C, avoids: Seq[Collidable])
