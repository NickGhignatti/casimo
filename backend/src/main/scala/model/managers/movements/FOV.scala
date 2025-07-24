package model.managers.movements

import model.entities.Collidable
import utils.Vector2D

object FOV:
  extension (from: Vector2D)
    def canSee(obstacles: Seq[Collidable])(point: Vector2D): Boolean =
      val segments = obstacles.flatMap(_.edges)
      isVisible(from, point, segments)

    def canSeeCollidable(obstacles: Seq[Collidable])(collidable: Collidable): Boolean =
      collidable.vertices.exists(from.canSee(obstacles)(_))


  private type Segment = (Vector2D, Vector2D)
  extension (collidable: Collidable)
    private def edges: Seq[Segment] =
      Seq(
        (collidable.topLeft, collidable.topRight),
        (collidable.topRight, collidable.bottomRight),
        (collidable.bottomRight, collidable.bottomLeft),
        (collidable.bottomLeft, collidable.topLeft)
      )

  private def isVisible(
      pointOfView: Vector2D,
      point: Vector2D,
      obstacles: Seq[Segment]
  ): Boolean =
    obstacles.forall { obs =>
      !segmentsIntersect(pointOfView, point, obs._1, obs._2)
    }

  private enum Orientation:
    case Collinear
    case Clockwise
    case CounterClockwise

  import Orientation.*
  private def orientation(p: Vector2D, q: Vector2D, r: Vector2D): Orientation =
    val valCalc = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y)
    valCalc match
      case 0                => Collinear
      case _ if valCalc > 0 => Clockwise
      case _                => CounterClockwise

  private def onSegment(p: Vector2D, q: Vector2D, r: Vector2D): Boolean =
    q.x >= Math.min(p.x, r.x) && q.x <= Math.max(p.x, r.x) &&
      q.y >= Math.min(p.y, r.y) && q.y <= Math.max(p.y, r.y)

  private def segmentsIntersect(
      p1: Vector2D,
      q1: Vector2D,
      p2: Vector2D,
      q2: Vector2D
  ): Boolean =
    val o1 = orientation(p1, q1, p2)
    val o2 = orientation(p1, q1, q2)
    val o3 = orientation(p2, q2, p1)
    val o4 = orientation(p2, q2, q1)

    if o1 != o2 && o3 != o4 then true
    else if o1 == Collinear && onSegment(p1, p2, q1) then true
    else if o2 == Collinear && onSegment(p1, q2, q1) then true
    else if o3 == Collinear && onSegment(p2, p1, q2) then true
    else if o4 == Collinear && onSegment(p2, q1, q2) then true
    else false
