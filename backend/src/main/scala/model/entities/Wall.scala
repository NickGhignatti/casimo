package model.entities

import utils.Vector2D

trait Positioned:
  val position: Vector2D

trait Sized:
  val width: Double
  val height: Double

trait CollidableEntity extends Entity with Sized with Positioned:
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

trait SizeChangingEntity extends Sized:
  def withLength(newWidth: Double): this.type
  def withHeight(newHeight: Double): this.type
  def withSize(newWidth: Double, newHeight: Double): this.type

case class Wall(
    id: String,
    position: Vector2D,
    width: Double,
    height: Double
) extends Positioned,
      Sized,
      CollidableEntity,
      SizeChangingEntity:

  def withLength(newLength: Double): this.type =
    this.copy(width = newLength).asInstanceOf[this.type]

  def withHeight(newHeight: Double): this.type =
    this.copy(height = newHeight).asInstanceOf[this.type]

  def withSize(newWidth: Double, newHeight: Double): this.type =
    this.copy(width = newWidth, height = newHeight).asInstanceOf[this.type]
