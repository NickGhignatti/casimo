package model.entities

import model.SimulationState
import model.entities.customers.Customer
import utils.Vector2D

import scala.util.Random

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
          Vector2D(
            x = Random.between(10.0, 750.0),
            y = Random.between(10.0, 450.0)
          ),
          Vector2D(Random.between(0, 5), Random.between(0, 5)),
          bankroll = Random.between(30, 5000)
        )
      )
    )
