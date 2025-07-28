package model.entities.customers

import model.entities.Positioned
import utils.Vector2D

/** Defines the contract for an entity that can move within the simulation
  * environment.
  *
  * This trait extends `Positioned`, adding the concept of a `direction` vector
  * and methods to update both position and direction.
  *
  * @tparam T
  *   The concrete type of the entity that extends this trait, enabling
  *   F-bounded polymorphism for immutable updates.
  */
trait Movable[T <: Movable[T]] extends Positioned:
  /** The current direction vector of the entity.
    */
  val direction: Vector2D

  /** Returns a new instance of the entity with an updated position. This method
    * ensures immutability.
    *
    * @param newPosition
    *   The new position for the entity.
    * @return
    *   A new instance of the entity with the updated position.
    */
  def withPosition(newPosition: Vector2D): T

  /** Returns a new instance of the entity with an updated direction. This
    * method ensures immutability.
    *
    * @param newDirection
    *   The new direction vector for the entity.
    * @return
    *   A new instance of the entity with the updated direction.
    */
  def withDirection(newDirection: Vector2D): T

  /** Returns a new instance of the entity with its direction updated by adding
    * a given vector to the current direction.
    *
    * @param addingDirection
    *   The vector to add to the current direction.
    * @return
    *   A new instance of the entity with the added direction.
    */
  def addedDirection(addingDirection: Vector2D): T =
    withDirection(direction + addingDirection)
