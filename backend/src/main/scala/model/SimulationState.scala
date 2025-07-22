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

  case class Builder(
      customers: Seq[Customer],
      games: List[Game],
      spawner: Option[Spawner],
      walls: List[Wall]
  ):

    def withCustomers(customers: Seq[Customer]): Builder =
      this.copy(customers = customers)

    def addCustomer(customer: Customer): Builder =
      this.copy(customers = this.customers :+ customer)

    def withGames(games: List[Game]): Builder =
      this.copy(games = games)

    def addGame(game: Game): Builder =
      this.copy(games = game :: this.games)

    def withSpawner(spawner: Spawner): Builder =
      this.copy(spawner = Some(spawner))

    def withoutSpawner(): Builder =
      this.copy(spawner = None)

    def withWalls(walls: List[Wall]): Builder =
      this.copy(walls = walls)

    def addWall(wall: Wall): Builder =
      this.copy(walls = wall :: this.walls)

    def build(): SimulationState =
      SimulationState(customers, games, spawner, walls)

  def builder(): Builder = Builder(Seq.empty, List.empty, None, List.empty)

extension (state: SimulationState)
  def addCustomer(customer: Customer): SimulationState =
    state.copy(customers = state.customers :+ customer)

  def addGame(game: Game): SimulationState =
    state.copy(games = game :: state.games)

  def setSpawner(spawner: Spawner): SimulationState =
    state.copy(spawner = Some(spawner))

  def addWall(wall: Wall): SimulationState =
    state.copy(walls = wall :: state.walls)
