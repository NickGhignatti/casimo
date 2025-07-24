package model.data

import model.SimulationState

/** A data manager that provides access to simulation state and computed
  * metrics.
  *
  * The DataManager serves as a facade for accessing and computing aggregate
  * information from the current simulation state, particularly financial
  * metrics related to games and customers.
  *
  * @param state
  *   the current simulation state containing games and customers
  */
case class DataManager(state: SimulationState):

  /** Calculates the total bankroll across all games in the current simulation
    * state.
    *
    * @return
    *   the sum of bankrolls from all games, or 0.0 if no games exist
    */
  def currentGamesBankroll: Double = state.games.map(_.bankroll).sum

  /** Calculates the total bankroll across all customers in the current
    * simulation state.
    *
    * @return
    *   the sum of bankrolls from all customers, or 0.0 if no customers exist
    */
  def currentCustomersBankroll: Double = state.customers.map(_.bankroll).sum
