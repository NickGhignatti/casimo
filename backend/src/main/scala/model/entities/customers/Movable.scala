package model.entities.customers

import utils.Vector2D

trait Movable[T <: Movable[T]]:
  val direction: Vector2D
  val position: Vector2D

  def withPosition(newPosition: Vector2D): T

  def withDirection(newDirection: Vector2D): T
