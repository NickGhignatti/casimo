package model.entities.customers

import utils.Vector2D

trait MovableWithPrevious[M <: MovableWithPrevious[M]] extends Movable[M]:
  def previousPosition: Option[Vector2D]
