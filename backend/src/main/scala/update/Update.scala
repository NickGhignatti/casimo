package update

import scala.annotation.tailrec
import scala.util.Random
import model.GlobalConfig
import model.SimulationState
import model.entities.Spawner
import model.entities.customers.Customer
import model.entities.customers.DefaultMovementManager
import update.Event.*
import utils.Vector2D

object Update:

  @tailrec
  def update(state: SimulationState, event: Event): SimulationState =
    event match
      case SimulationTick =>
        println("Simulation tick event received, updating state...")
        state.spawner match
          case None => update(state, UpdateCustomersPosition)
          case Some(value) if value.customerQuantity == state.customers.size =>
            update(state, UpdateCustomersPosition)
          case Some(value) =>
            update(value.spawn(state), UpdateCustomersPosition)
      case UpdateCustomersPosition =>
        println("Updating customers' positions...")
        given GlobalConfig = GlobalConfig()
        val boidManager = DefaultMovementManager()
        val newCustPos = boidManager.update(state.customers)
        update(state.copy(customers = newCustPos), UpdateGames)
      case UpdateGames =>
        println("Updating games...")
        update(state, UpdateSimulationBankrolls)
      case UpdateSimulationBankrolls =>
        println("Updating simulation bankrolls...")
        update(state, UpdateCustomersState)
      case UpdateCustomersState =>
        println("Updating customers' state...")
        state

      case AddCustomers(n) =>
        println("Adding customers to the state...")
        state.copy(spawner =
          Some(Spawner(Random.nextString(12), Vector2D.zero, n, 10))
        )
