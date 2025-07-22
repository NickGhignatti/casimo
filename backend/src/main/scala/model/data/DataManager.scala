package model.data

import model.SimulationState

case class DataManager(state: SimulationState):
  def currentGamesBankroll: Double = state.games.map(_.bankroll).sum
  def currentCustomersBankroll: Double = state.customers.map(_.bankroll).sum
