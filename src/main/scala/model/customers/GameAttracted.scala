package model.customers

import model.SimulationState
import model.entities.games.Game

trait GameAttracted(boid: Boid, favouriteGame: Game) extends Customer:
  override def update(simulationState: SimulationState): SimulationState =
    val attraction = favouriteGame.position - boid.position
    boid
      .copy(velocity = boid.velocity + attraction)
      .update(simulationState)
