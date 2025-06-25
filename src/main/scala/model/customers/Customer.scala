package model.customers

import model.SimulationState
import utils.Vector2D

trait Customer:
  def position: Vector2D
  def update(simulationState: SimulationState): Customer

