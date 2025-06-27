package utils

case class Vector2D(x: Double, y: Double):
  def +(other: Vector2D): Vector2D =
    Vector2D(this.x + other.x, this.y + other.y)
  def -(other: Vector2D): Vector2D =
    Vector2D(this.x - other.x, this.y - other.y)
  def *(scalar: Double): Vector2D = Vector2D(this.x * scalar, this.y * scalar)
  def /(scalar: Double): Vector2D = Vector2D(this.x / scalar, this.y / scalar)
  def dot(other: Vector2D): Double = this.x * other.x + this.y * other.y

  private def magnitude: Double = Math.sqrt(x * x + y * y)
  def normalize: Vector2D = if (magnitude == 0) this else this / magnitude
