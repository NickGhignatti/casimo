package model.managers.movements

import model.GlobalConfig
import model.entities.customers.Movable
import model.managers.BaseManager
import model.managers.WeightedManager
import utils.Vector2D
import utils.Vector2D.distance

object Boids:
  case class State[M <: Movable[M]](boid: M, others: Seq[M])
      extends Movable[State[M]]:
    override def updatedPosition(newPosition: Vector2D): State[M] =
      this.copy(boid = boid.updatedPosition(newPosition))

    override def updatedDirection(newDirection: Vector2D): State[M] =
      this.copy(boid = boid.updatedDirection(newDirection))

    def directionAdded(addingDirection: Vector2D): State[M] =
      this.copy(boid = boid.updatedDirection(boid.direction + addingDirection))

    export boid.{position, direction}

  case class MoverManager[M <: Movable[M]]() extends BaseManager[M]:

    override def update(slice: M)(using config: GlobalConfig): M =
      slice.updatedPosition(slice.position + slice.direction)

  case class SeparationManager[M <: Movable[M]](
      avoidRadius: Double,
      weight: Double = 1
  ) extends WeightedManager[State[M]]:

    override def update(slice: State[M])(using config: GlobalConfig): State[M] =
      slice.directionAdded(
        separation(slice.boid, slice.others.map(_.position)) * weight
      )

    private def separation(boid: M, positions: Seq[Vector2D]): Vector2D =
      if positions.isEmpty then Vector2D.zero
      else
        positions
          .filter(distance(boid.position, _) < avoidRadius)
          .map(pos => (boid.position - pos).normalize)
          .reduce(_ + _)

    override def updatedWeight(weight: Double): WeightedManager[State[M]] =
      this.copy(weight = weight)

  case class CohesionManager[M <: Movable[M]](weight: Double = 1)
      extends WeightedManager[State[M]]:
    override def update(slice: State[M])(using config: GlobalConfig): State[M] =
      slice.directionAdded(
        cohesion(
          slice.boid,
          slice.others.map(_.position)
        ) * weight
      )

    private def cohesion(boid: M, positions: Seq[Vector2D]): Vector2D =
      if positions.isEmpty then Vector2D.zero
      else
        val center = positions.reduce(_ + _) / positions.size
        (center - boid.position).normalize

    override def updatedWeight(weight: Double): WeightedManager[State[M]] =
      this.copy(weight = weight)

  case class AlignmentManager[M <: Movable[M]](weight: Double = 1)
      extends WeightedManager[State[M]]:
    override def update(slice: State[M])(using config: GlobalConfig): State[M] =
      slice.directionAdded(
        alignment(
          slice.boid,
          slice.others.map(_.direction)
        ) * weight
      )

    private def alignment(boid: M, velocities: Seq[Vector2D]): Vector2D =
      if velocities.isEmpty then Vector2D.zero
      else
        val average = velocities.reduce(_ + _) / velocities.size
        (average - boid.direction).normalize

    override def updatedWeight(weight: Double): WeightedManager[State[M]] =
      this.copy(weight = weight)

  case class VelocityLimiterManager[M <: Movable[M]](maxSpeed: Double)
      extends BaseManager[M]:
    extension (v: Vector2D)
      private def capped(max: Double): Vector2D =
        if v.magnitude < max then v else v.normalize * max

    override def update(slice: M)(using config: GlobalConfig): M =
      slice.updatedDirection(slice.direction.capped(maxSpeed))

  case class PerceptionLimiterManager[M <: Movable[M]](perceptionRadius: Double)
      extends BaseManager[State[M]]:
    override def update(slice: State[M])(using config: GlobalConfig): State[M] =
      slice.copy(others =
        slice.others.filter(other =>
          distance(slice.boid.position, other.position) <= perceptionRadius
        )
      )
