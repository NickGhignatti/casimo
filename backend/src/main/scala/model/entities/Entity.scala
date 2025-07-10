package model.entities

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
