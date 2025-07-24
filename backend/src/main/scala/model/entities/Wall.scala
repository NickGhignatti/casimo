package model.entities

import scala.util.Random

import utils.Vector2D

trait Positioned:
  val position: Vector2D

trait Sized:
  val width: Double
  val height: Double

trait Collidable extends Sized with Positioned:
  final def contains(point: Vector2D): Boolean =
    point.x >= position.x &&
      point.x <= position.x + width &&
      point.y >= position.y &&
      point.y <= position.y + height

  final def collidesWith[E <: Positioned](other: E): Boolean =
    val horizontalOverlap =
      position.x <= other.position.x &&
        position.x + width >= other.position.x

    val verticalOverlap =
      position.y <= other.position.y &&
        position.y + height >= other.position.y

    horizontalOverlap && verticalOverlap

  def topLeft: Vector2D = position

  def topRight: Vector2D =
    Vector2D(position.x + width, position.y)

  def bottomLeft: Vector2D =
    Vector2D(position.x, position.y + height)

  def bottomRight: Vector2D =
    Vector2D(position.x + width, position.y + height)

  def vertices: Seq[Vector2D] = Seq(topLeft, topRight, bottomLeft, bottomRight)

  def center: Vector2D = position + Vector2D(width, height)

trait CollidableEntity extends Collidable with Entity

trait SizeChangingEntity extends Sized:
  def withWidth(newWidth: Double): this.type
  def withHeight(newHeight: Double): this.type
  def withSize(newWidth: Double, newHeight: Double): this.type

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

object Wall:
  def apply(position: Vector2D, width: Double, height: Double): Wall =
    Wall("wall-" + Random.nextInt(), position, width, height)
