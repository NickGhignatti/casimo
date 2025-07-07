package model.managers.movements

import model.GlobalConfig
import model.entities.Movable
import model.managers.BaseManager
import utils.Vector2D
import utils.Vector2D.distance

object Boids:

  case class MoverManager[M <: Movable[M]]() extends BaseManager[Seq[M]]:

    override def update(slice: Seq[M])(using config: GlobalConfig): Seq[M] =
      slice.map(boid => boid.updatedPosition(boid.position + boid.direction))

  case class SeparationManager[M <: Movable[M]](
      avoidRadius: Double
  ) extends BaseManager[Seq[M]]:

    override def update(slice: Seq[M])(using config: GlobalConfig): Seq[M] =
      slice.map(boid =>
        boid.updatedDirection(separation(boid, slice.map(_.position)))
      )

    private def separation(boid: M, positions: Seq[Vector2D]): Vector2D =
      if positions.isEmpty then Vector2D.zero
      else
        positions
          .filter(distance(boid.position, _) < avoidRadius)
          .map(pos => (boid.position - pos).normalize)
          .reduce(_ + _)

  case class CohesionManager[M <: Movable[M]]() extends BaseManager[Seq[M]]:
    override def update(slice: Seq[M])(using config: GlobalConfig): Seq[M] =
      slice.map(boid =>
        boid.updatedDirection(
          boid.direction + cohesion(boid, slice.map(_.position))
        )
      )

    private def cohesion(boid: M, positions: Seq[Vector2D]): Vector2D =
      if positions.isEmpty then Vector2D.zero
      else
        val center = positions.reduce(_ + _) / positions.size
        (center - boid.position).normalize

  case class AlignmentManager[M <: Movable[M]]() extends BaseManager[Seq[M]]:
    override def update(slice: Seq[M])(using config: GlobalConfig): Seq[M] =
      slice.map(boid =>
        boid.updatedDirection(
          boid.direction + alignment(boid, slice.map(_.direction))
        )
      )

    private def alignment(boid: M, velocities: Seq[Vector2D]): Vector2D =
      if velocities.isEmpty then Vector2D.zero
      else
        val average = velocities.reduce(_ + _) / velocities.size
        (average - boid.direction).normalize

  case class VelocityLimiterManager[M <: Movable[M]](maxSpeed: Double)
      extends BaseManager[Seq[M]]:
    extension (v: Vector2D)
      private def capped(max: Double): Vector2D =
        if v.magnitude < max then v else v.normalize * max

    override def update(slice: Seq[M])(using config: GlobalConfig): Seq[M] =
      slice.map(boid => boid.updatedDirection(boid.direction.capped(maxSpeed)))
