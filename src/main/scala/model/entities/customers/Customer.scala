package model.entities.customers

import model.entities.Entity
import model.entities.Movable
import utils.Vector2D

case class Customer(
    id: String,
    position: Vector2D,
    direction: Vector2D = Vector2D.zero
) extends Entity,
      Movable[Customer]:

  protected def updatedPosition(newPosition: Vector2D): Customer =
    this.copy(position = newPosition)
