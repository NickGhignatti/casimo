package model.customers

import model.SimulationState
import utils.Vector2D

trait Customer:
  type C <: Customer
  def position: Vector2D
  def velocity: Vector2D
  def update(simulationState: SimulationState[C]): C

case class ContextImpl(boids: List[Boid]) extends Boid.Context

case class BoidCustomer(boid: Boid) extends Customer:
  type C = BoidCustomer

  def position: Vector2D = boid.position

  def velocity: Vector2D = boid.velocity

  def update(simulationState: SimulationState[BoidCustomer]): BoidCustomer =
    BoidCustomer(boid.update(ContextImpl(simulationState.customers.map(_.boid))))
