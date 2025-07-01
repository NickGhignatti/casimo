package utils

case class Vector2D(x: Double, y: Double):
  def +(other: Vector2D): Vector2D =
    Vector2D(this.x + other.x, this.y + other.y)
  def -(other: Vector2D): Vector2D =
    Vector2D(this.x - other.x, this.y - other.y)
  def /(scalar: Double): Vector2D = Vector2D(this.x / scalar, this.y / scalar)
  def dot(other: Vector2D): Double = this.x * other.x + this.y * other.y

  def magnitude: Double = Math.sqrt(x * x + y * y)
  def normalize: Vector2D = if (magnitude == 0) this else this / magnitude

object Vector2D:
  val Zero: Vector2D = Vector2D(0, 0)
  def distance(v1: Vector2D, v2: Vector2D): Double = (v1 - v2).magnitude
  extension (scalar: Double)
    def *(v: Vector2D): Vector2D = Vector2D(v.x * scalar, v.y * scalar)
