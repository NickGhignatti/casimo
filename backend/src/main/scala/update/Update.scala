package update

import scala.annotation.tailrec
import scala.util.{Random, Try}
import scala.concurrent.*
import scala.concurrent.duration.*

import model.SimulationState
import model.entities.customers.Customer
import update.Event._
import utils.Vector2D

object Update:

  @tailrec
  def update(state: SimulationState, event: Event): SimulationState =
    event match
      case SimulationTick =>
        println("Simulation tick event received, updating state...")
        update(state, UpdateCustomersPosition)
      case UpdateCustomersPosition =>
        println("Updating customers' positions...")
        val newCustPos = state.customers.map(c => c.move())
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
        val newCustomers = List.fill(50)(
          Customer(
            s"customer-${Random.nextInt()}",
            Vector2D(
              x = Random.between(10.0, 750.0),
              y = Random.between(10.0, 450.0)
            ),
            Vector2D(Random.between(0, 5), Random.between(0, 5)),
            bankroll = Random.between(30, 5000)
          )
        )
        val updateCustomers = state.customers ++ newCustomers
        state.copy(customers = updateCustomers)
