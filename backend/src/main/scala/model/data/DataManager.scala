package model.data

import model.SimulationState

case class DataManager(state: SimulationState):
  def currentGamesBankroll: Double = state.games.map(_.bankroll).sum
