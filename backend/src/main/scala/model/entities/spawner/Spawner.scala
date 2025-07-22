package model.entities.spawner

import scala.util.Random

import model.SimulationState
import model.entities.Entity
import model.entities.customers.Customer
import utils.Vector2D

case class Spawner(
    id: String,
    position: Vector2D,
    strategy: SpawningStrategy,
    currentTime: Double = 0.0,
    ticksToSpawn: Double = 10.0
) extends Entity:

  def spawn(
      state: SimulationState
  ): SimulationState =
    if ((currentTime + 1) % ticksToSpawn == 0) {
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
    }
    state.copy(spawner = Some(this.copy(currentTime = currentTime + 1)))
