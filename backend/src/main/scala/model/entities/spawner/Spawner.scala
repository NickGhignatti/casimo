package model.entities.spawner

import model.SimulationState
import model.entities.Entity
import model.entities.customers.Customer
import utils.Vector2D

import scala.util.Random

case class Spawner(
    id: String,
    position: Vector2D,
    strategy: SpawningStrategy,
    currentTime: Double = 0.0
) extends Entity:

  def spawn(
      state: SimulationState
  ): SimulationState =
    state.copy(
      customers =
        state.customers ++ Seq.fill(strategy.customersAt(currentTime))(
          Customer(
            s"customer-${Random.nextInt()}",
            this.position.around(5.0),
            Vector2D(Random.between(0, 5), Random.between(0, 5)),
            bankroll = Random.between(30, 5000)
          )
        ),
      spawner = Some(this.copy(currentTime = currentTime + 1))
    )
