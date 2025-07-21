package utils

import scala.util.Random

case class Vector2D(x: Double, y: Double):
  def +(other: Vector2D): Vector2D =
    Vector2D(this.x + other.x, this.y + other.y)
  def -(other: Vector2D): Vector2D =
    Vector2D(this.x - other.x, this.y - other.y)
  def *(scalar: Double): Vector2D = Vector2D(this.x * scalar, this.y * scalar)
  def /(scalar: Double): Vector2D = Vector2D(this.x / scalar, this.y / scalar)
  def dot(other: Vector2D): Double = this.x * other.x + this.y * other.y

  def magnitude: Double = Math.sqrt(x * x + y * y)
  def normalize: Vector2D = if (magnitude == 0.0) this else this / magnitude

  def around(radius: Double): Vector2D = Vector2D(
    Random.between(this.x - radius, this.x + radius),
    Random.between(this.y - radius, this.y + radius)
  )

object Vector2D:
  val zero: Vector2D = Vector2D(0.0, 0.0)
  def apply(): Vector2D = zero

  def distance(u: Vector2D, v: Vector2D): Double =
    (v - u).magnitude

  def direction(from: Vector2D, to: Vector2D): Vector2D =
    (to - from).normalize

object Rotation:
  val right: Double = -Math.PI / 2
  val left: Double = Math.PI / 2

  extension (vector: Vector2D)
    def rotated(theta: Double): Vector2D =
      val cosTheta = Math.cos(theta)
      val sinTheta = Math.sin(theta)
      Vector2D(
        vector.x * cosTheta - vector.y * sinTheta,
        vector.x * sinTheta + vector.y * cosTheta
      )
