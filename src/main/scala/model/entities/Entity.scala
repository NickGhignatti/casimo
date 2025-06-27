package model.entities

import utils.Vector2D

trait Entity:
  val id: String
  val position: Vector2D

// TRAIT MOVABLE CAN BE DONE BY COMPOSITION
//trait Movable[E <: Entity]
//  def move(e: E): E
//
//
//object Movable
//  implicit val customerMovable: Movable[Customer] =
//    (c: Customer) => c.copy(position = c.position + c.direction)
//

trait Movable:
  val direction: Vector2D

  def move(): Movable
