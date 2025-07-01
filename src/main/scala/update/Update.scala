package update

import model.SimulationState
import model.customers.{Boid, BoidCustomer, ContextImpl, Customer}
import update.Event.*
import utils.Vector2D

import scala.annotation.tailrec
import scala.util.Random

object Update:

  @tailrec
  def update(state: SimulationState[BoidCustomer], event: Event): SimulationState[BoidCustomer] =
    event match
      case SimulationTick =>
        println("Simulation tick event received, updating state...")
        update(state, UpdateCustomersPosition)
      case UpdateCustomersPosition =>
        println("Updating customers' positions...")
        val updatedCustomers = state.customers.map(_.update(state))
        update(state.copy(customers = updatedCustomers), UpdateGames)
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
        val newCustomers = List.fill(50)(
          BoidCustomer(Boid(
            Vector2D(
              x = Random.between(10.0, 750.0),
              y = Random.between(10.0, 450.0)
            ))
          )
        )
        val updateCustomers = state.customers ++ newCustomers
        state.copy(customers = updateCustomers)
