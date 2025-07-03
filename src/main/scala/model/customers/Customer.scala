package model.customers

import model.SimulationState
import model.entities.games.Game
import utils.Vector2D

trait Customer:
  def id: String
  def position: Vector2D
  def velocity: Vector2D
  def update(simulationState: SimulationState): SimulationState
