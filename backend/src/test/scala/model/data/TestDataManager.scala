package model.data

import model.SimulationState
import org.scalatest.funsuite.AnyFunSuite

class TestDataManager extends AnyFunSuite:

  test("games bankroll should be 0 if no games are in the simulation state"):
    val simulationState =
      SimulationState(List.empty, List.empty, None, List.empty)
    val dataManager = DataManager(simulationState)
    assert(dataManager.currentGamesBankroll == 0.0)
