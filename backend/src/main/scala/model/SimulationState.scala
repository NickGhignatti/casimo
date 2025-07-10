package model

import model.entities.Spawner
import model.entities.customers.Customer
import model.entities.games.Game

case class SimulationState(
    customers: Seq[Customer],
    games: List[Game],
    spawner: Option[Spawner]
)
