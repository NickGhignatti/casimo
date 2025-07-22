package model.data

import model.SimulationState
import model.entities.customers.Customer
import model.entities.customers.DefaultMovementManager
import org.scalatest.funsuite.AnyFunSuite
import update.Event.SimulationTick
import update.Update

class TestDataManager extends AnyFunSuite:

  test("games bankroll should be 0 if no games are in the simulation state"):
    val simulationState =
      SimulationState(List.empty, List.empty, None, List.empty)
    val dataManager = DataManager(simulationState)
    assert(dataManager.currentGamesBankroll == 0.0)

  test(
    "customers bankroll should be 0 if no customers are in the simulation state"
  ):
    val simulationState =
      SimulationState(List.empty, List.empty, None, List.empty)
    val dataManager = DataManager(simulationState)
    assert(dataManager.currentCustomersBankroll == 0.0)

  test("customers bankroll should remain constant if there are no games"):
    val simulationState = SimulationState(
      List(
        Customer().withBankroll(30.0),
        Customer().withBankroll(50.0)
      ),
      List.empty,
      None,
      List.empty
    )
    val dataManager = DataManager(simulationState)
    assert(dataManager.currentCustomersBankroll == 80.0)
    val update = Update(DefaultMovementManager())
    val newSimulationState = update.update(simulationState, SimulationTick)
    val newDataManager =
      update.updateSimulationDataManager(dataManager, newSimulationState)
    assert(newDataManager.currentCustomersBankroll == 80.0)
