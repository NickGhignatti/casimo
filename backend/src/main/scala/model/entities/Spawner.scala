package model.entities

import scala.util.Random

import model.SimulationState
import model.entities.customers.Customer
import utils.Vector2D

case class Spawner(
    id: String,
    position: Vector2D,
    customerQuantity: Int,
    nTicks: Int
) extends Entity:

  def spawn(
      state: SimulationState
  ): SimulationState =
    state.copy(customers =
      state.customers ++ Seq.fill(customerQuantity / nTicks)(
        Customer(
          s"customer-${Random.nextInt()}",
          this.position.around(5.0),
          Vector2D(Random.between(0, 5), Random.between(0, 5)),
          bankroll = Random.between(30, 5000)
        )
      )
    )
