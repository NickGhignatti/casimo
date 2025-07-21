package model.entities.customers

import model.entities.Positioned
import utils.Vector2D

trait Movable[T <: Movable[T]] extends Positioned:
  val direction: Vector2D

  def updatedPosition(newPosition: Vector2D): T

  def updatedDirection(newDirection: Vector2D): T

  def addedDirection(addingDirection: Vector2D): T =
    updatedDirection(direction + addingDirection)
