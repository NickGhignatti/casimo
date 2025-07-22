package model

import model.entities.Wall
import model.entities.customers.Customer
import model.entities.games.Game
import model.entities.spawner.Spawner

case class SimulationState(
    customers: Seq[Customer],
    games: List[Game],
    spawner: Option[Spawner],
    walls: List[Wall]
)

object SimulationState:
  def empty(): SimulationState =
    SimulationState(Seq.empty, List.empty, None, List.empty)
