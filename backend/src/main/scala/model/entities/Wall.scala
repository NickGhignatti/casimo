package model.entities

import scala.util.Random

import utils.Vector2D

/** Trait for entities that have a position in 2D space.
  *
  * Provides the basic capability for an entity to exist at a specific
  * coordinate location within the simulation world.
  */
trait Positioned:
  val position: Vector2D

/** Trait for entities that have physical dimensions.
  *
  * Defines the size properties that determine how much space an entity occupies
  * in the simulation world. Used in conjunction with position for collision
  * detection and spatial calculations.
  */
trait Sized:
  val width: Double
  val height: Double

/** Trait providing collision detection capabilities for positioned and sized
  * entities.
  *
  * Combines position and size information to enable spatial collision
  * detection, point containment checks, and geometric calculations. Uses
  * axis-aligned bounding box (AABB) collision detection for efficient spatial
  * queries.
  */
trait Collidable extends Sized with Positioned:

  /** Checks if a point is contained within this entity's bounds.
    *
    * Uses inclusive bounds checking on the left and top edges, and exclusive on
    * the right and bottom edges to prevent edge overlap issues.
    *
    * @param point
    *   the 2D point to test for containment
    * @return
    *   true if the point is within this entity's rectangular bounds
    */
  final def contains(point: Vector2D): Boolean =
    point.x >= position.x &&
      point.x <= position.x + width &&
      point.y >= position.y &&
      point.y <= position.y + height

  /** Checks if this entity collides with another positioned entity.
    *
    * Performs axis-aligned bounding box collision detection by checking for
    * overlap on both horizontal and vertical axes. Both overlaps must be true
    * for a collision to be detected.
    *
    * @param other
    *   the other positioned entity to check collision against
    * @return
    *   true if the entities' bounding boxes overlap
    */
  final def collidesWith[E <: Positioned](other: E): Boolean =
    val horizontalOverlap =
      position.x <= other.position.x &&
        position.x + width >= other.position.x

    val verticalOverlap =
      position.y <= other.position.y &&
        position.y + height >= other.position.y

    horizontalOverlap && verticalOverlap

  /** Returns the top-left corner coordinates of this entity.
    *
    * @return
    *   the position vector representing the top-left corner
    */
  def topLeft: Vector2D = position

  /** Returns the top-right corner coordinates of this entity.
    *
    * @return
    *   the position vector representing the top-right corner
    */
  def topRight: Vector2D =
    Vector2D(position.x + width, position.y)

  /** Returns the bottom-left corner coordinates of this entity.
    *
    * @return
    *   the position vector representing the bottom-left corner
    */
  def bottomLeft: Vector2D =
    Vector2D(position.x, position.y + height)

  /** Returns the bottom-right corner coordinates of this entity.
    *
    * @return
    *   the position vector representing the bottom-right corner
    */
  def bottomRight: Vector2D =
    Vector2D(position.x + width, position.y + height)

  /** Returns all four corner vertices of this entity's bounding rectangle.
    *
    * @return
    *   sequence containing topLeft, topRight, bottomLeft, and bottomRight
    *   vertices
    */
  def vertices: Seq[Vector2D] = Seq(topLeft, topRight, bottomLeft, bottomRight)

  /** Returns the center point of this entity's bounding rectangle.
    *
    * @return
    *   the position vector representing the geometric center
    */
  def center: Vector2D = position + Vector2D(width, height) / 2

/** Combined trait for entities that are both collidable and have unique
  * identifiers.
  *
  * Represents simulation entities that can participate in collision detection
  * and can be uniquely identified within the system.
  */
trait CollidableEntity extends Collidable with Entity

/** Trait for entities whose dimensions can be modified at runtime.
  *
  * Provides methods to dynamically change an entity's width and height, useful
  * for entities that need to resize during simulation or for configuration
  * purposes.
  */
trait SizeChangingEntity extends Sized:

  /** Creates a copy of this entity with a new width.
    *
    * @param newWidth
    *   the new width value
    * @return
    *   a new instance with the updated width
    */
  def withWidth(newWidth: Double): this.type

  /** Creates a copy of this entity with a new height.
    *
    * @param newHeight
    *   the new height value
    * @return
    *   a new instance with the updated height
    */
  def withHeight(newHeight: Double): this.type

  /** Creates a copy of this entity with new dimensions.
    *
    * @param newWidth
    *   the new width value
    * @param newHeight
    *   the new height value
    * @return
    *   a new instance with the updated dimensions
    */
  def withSize(newWidth: Double, newHeight: Double): this.type

/** Represents a wall entity that acts as a collision barrier in the
  *
  * simulation.
  *
  * Walls are static rectangular obstacles that can block movement and provide
  * boundaries within the simulation space.They support dynamic resizing and
  * participate in the collision detection system .
  *
  * @param id
  *   unique identifier for this wall instance
  * @param position
  *   the 2D coordinates of the wall 's top -left corner
  * @param width
  *   the width of the wall in simulation units
  * @param height
  *   the height of the wall in simulation units
  */
case class Wall(
    id: String,
    position: Vector2D,
    width: Double,
    height: Double
) extends Positioned,
      Sized,
      Collidable,
      SizeChangingEntity,
      Entity:

  def withWidth(newWidth: Double): this.type =
    this.copy(width = newWidth).asInstanceOf[this.type]

  def withHeight(newHeight: Double): this.type =
    this.copy(height = newHeight).asInstanceOf[this.type]

  def withSize(newWidth: Double, newHeight: Double): this.type =
    this.copy(width = newWidth, height = newHeight).asInstanceOf[this.type]

/** Factory object for creating Wall instances.
  */
object Wall:

  /** Creates a new wall with an auto-generated unique identifier.
    *
    * Generates a random ID with "wall-" prefix for convenient wall creation
    * without manual ID management.
    *
    * @param position
    *   the 2D coordinates for the wall placement
    * @param width
    *   the width of the wall
    * @param height
    *   the height of the wall
    * @return
    *   a new Wall instance with generated ID
    */
  def apply(position: Vector2D, width: Double, height: Double): Wall =
    Wall("wall-" + Random.nextInt(), position, width, height)
