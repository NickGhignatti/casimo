package model.managers.movements

import model.GlobalConfig
import model.entities.Movable
import model.managers.BaseManager
import utils.Vector2D
import utils.Vector2D.distance

case class SeparationManager[M <: Movable[M]](
    perceptionRadius: Double,
    avoidRadius: Double,
    alignmentWeight: Double,
    cohesionWeight: Double,
    separationWeight: Double
) extends BaseManager[Seq[M]]:

  override def update(slice: Seq[M])(using config: GlobalConfig): Seq[M] =
    slice.map(boid =>
      boid.updatedDirection(separation(boid, slice.map(_.position)))
    )

  private def separation(boid: Movable[_], positions: Seq[Vector2D]): Vector2D =
    if positions.isEmpty then Vector2D.zero
    else
      positions
        .filter(distance(boid.position, _) < avoidRadius)
        .map(pos => (boid.position - pos).normalize)
        .reduce(_ + _)

case class MoverManager[M <: Movable[M]]() extends BaseManager[Seq[M]]:

  override def update(slice: Seq[M])(using config: GlobalConfig): Seq[M] =
    slice.map(boid => boid.updatedPosition(boid.position + boid.direction))
