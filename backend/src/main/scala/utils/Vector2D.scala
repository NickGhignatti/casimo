package utils

import scala.util.Random

/** A 2D vector with double precision coordinates.
  *
  * Represents a point or direction in 2D space with x and y components.
  * Provides common vector operations including arithmetic, dot product,
  * magnitude calculation, and normalization.
  *
  * @param x
  *   the x-coordinate component
  * @param y
  *   the y-coordinate component
  */
case class Vector2D(x: Double, y: Double):

  /** Adds two vectors component-wise.
    *
    * @param other
    *   the vector to add
    * @return
    *   a new vector with the sum of corresponding components
    */
  def +(other: Vector2D): Vector2D =
    Vector2D(this.x + other.x, this.y + other.y)

  /** Subtracts two vectors component-wise.
    *
    * @param other
    *   the vector to subtract
    * @return
    *   a new vector with the difference of corresponding components
    */
  def -(other: Vector2D): Vector2D =
    Vector2D(this.x - other.x, this.y - other.y)

  /** Multiplies the vector by a scalar value.
    *
    * @param scalar
    *   the scalar value to multiply by
    * @return
    *   a new vector with both components scaled
    */
  def *(scalar: Double): Vector2D = Vector2D(this.x * scalar, this.y * scalar)

  /** Divides the vector by a scalar value.
    *
    * @param scalar
    *   the scalar value to divide by (should not be zero)
    * @return
    *   a new vector with both components divided by the scalar
    */
  def /(scalar: Double): Vector2D = Vector2D(this.x / scalar, this.y / scalar)

  /** Computes the dot product of this vector with another vector.
    *
    * The dot product is useful for determining the angle between vectors and
    * for projection calculations.
    *
    * @param other
    *   the other vector
    * @return
    *   the dot product as a scalar value
    */
  def dot(other: Vector2D): Double = this.x * other.x + this.y * other.y

  /** Returns the negation of this vector (unary minus operator).
    *
    * @return
    *   a new vector with both components negated
    */
  def unary_- : Vector2D = Vector2D(-this.x, -this.y)

  /** Calculates the magnitude (length) of this vector.
    *
    * Uses the Euclidean distance formula: √(x² + y²)
    *
    * @return
    *   the magnitude as a non-negative double
    */
  def magnitude: Double = Math.sqrt(x * x + y * y)

  /** Returns a normalized version of this vector (unit vector).
    *
    * A normalized vector has magnitude 1.0 and points in the same direction. If
    * this vector has zero magnitude, returns the same vector unchanged.
    *
    * @return
    *   a new vector with magnitude 1.0, or the original vector if magnitude is
    *   0
    */
  def normalize: Vector2D = if (magnitude == 0.0) this else this / magnitude

  /** Generates a random vector within a square area around this vector.
    *
    * Creates a new vector where each component is randomly chosen within the
    * range [this.component - radius, this.component + radius].
    *
    * @param radius
    *   the maximum distance from this vector's components
    * @return
    *   a new random vector within the specified area
    */
  def around(radius: Double): Vector2D = Vector2D(
    Random.between(this.x - radius, this.x + radius),
    Random.between(this.y - radius, this.y + radius)
  )

/** Companion object for Vector2D containing utility methods and constants.
  */
object Vector2D:
  /** The zero vector (origin point).
    *
    * A constant representing the vector with both components equal to 0.0.
    */
  val zero: Vector2D = Vector2D(0.0, 0.0)

  /** Creates a zero vector.
    *
    * Alternative constructor that returns the zero vector without parameters.
    *
    * @return
    *   the zero vector Vector2D(0.0, 0.0)
    */
  def apply(): Vector2D = zero

  /** Calculates the Euclidean distance between two vectors.
    *
    * Computes the magnitude of the difference vector between the two points.
    *
    * @param u
    *   the first vector
    * @param v
    *   the second vector
    * @return
    *   the distance between the vectors as a non-negative double
    */
  def distance(u: Vector2D, v: Vector2D): Double =
    (v - u).magnitude

  /** Calculates the unit direction vector from one point to another.
    *
    * Returns a normalized vector pointing from the first vector to the second.
    * If the vectors are the same, returns the zero vector.
    *
    * @param from
    *   the starting vector
    * @param to
    *   the target vector
    * @return
    *   a unit vector pointing from 'from' to 'to'
    */
  def direction(from: Vector2D, to: Vector2D): Vector2D =
    (to - from).normalize
