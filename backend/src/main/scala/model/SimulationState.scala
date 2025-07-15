package model

import model.entities.customers.Customer
import model.entities.games.Game
import model.entities.spawner.Spawner

case class SimulationState(
    customers: Seq[Customer],
    games: List[Game],
    spawner: Option[Spawner]
)
