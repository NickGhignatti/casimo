package model.entities.customers

import model.entities.customers.CustState.Playing
import model.entities.games.Game

/** Defines the contract for an entity that possesses a customer state.
  *
  * This trait tracks whether a customer is `Playing` a game or `Idle`,
  * providing methods to change this state and query their current activity.
  *
  * @tparam T
  *   The concrete type of the entity that extends this trait, enabling
  *   F-bounded polymorphism for immutable updates.
  */
trait CustomerState[T <: CustomerState[T]]:
  /** The current state of the customer (Playing or Idle).
    */
  val customerState: CustState

  /** Changes the customer's state to a new specified state. This is a
    * convenience method that delegates to `withCustomerState`.
    *
    * @param newState
    *   The new `CustState` to apply.
    * @return
    *   A new instance of the entity with the updated customer state.
    */
  def changeState(newState: CustState): T =
    withCustomerState(newState)

  /** Returns a new instance of the entity with the updated customer state. This
    * method must be implemented by concrete classes to ensure immutability.
    *
    * @param newState
    *   The new `CustState` for the customer.
    * @return
    *   A new instance of the entity with the updated customer state.
    */
  def withCustomerState(newState: CustState): T

  /** Returns an `Option` containing the `Game` object if the customer is
    * currently `Playing`, otherwise returns `Option.empty`.
    *
    * @return
    *   An `Option[Game]` representing the game the customer is playing, if any.
    */
  def getGameOrElse: Option[Game] =
    customerState match
      case Playing(game) => Some(game)
      case _             => Option.empty

  /** Checks if the customer is currently in the `Playing` state.
    *
    * @return
    *   `true` if the customer is playing a game, `false` otherwise.
    */
  def isPlaying: Boolean = customerState match
    case Playing(_) => true
    case _          => false

/** Enumeration representing the possible states of a customer.
  */
enum CustState:
  /** Indicates the customer is currently playing a specific game.
    * @param game
    *   The `Game` instance the customer is playing.
    */
  case Playing(game: Game)

  /** Indicates the customer is idle and not currently participating in a game.
    */
  case Idle
