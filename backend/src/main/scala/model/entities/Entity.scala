package model.entities

import utils.Vector2D

trait Entity:
  val id: String

// TRAIT MOVABLE CAN BE DONE BY COMPOSITION
//trait Movable[E <: Entity]
//  def move(e: E): E
//
//
//object Movable
//  implicit val customerMovable: Movable[Customer] =
//    (c: Customer) => c.copy(position = c.position + c.direction)
//

trait Movable[T <: Movable[T]]:
  val direction: Vector2D
  val position: Vector2D

  def updatedPosition(newPosition: Vector2D): T

  def updatedDirection(newDirection: Vector2D): T
