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

  class Builder:
    private var customers: Seq[Customer] = Seq.empty
    private var games: List[Game] = List.empty
    private var spawner: Option[Spawner] = None
    private var walls: List[Wall] = List.empty

    def withCustomers(customers: Seq[Customer]): Builder =
      this.customers = customers
      this

    def addCustomer(customer: Customer): Builder =
      this.customers = this.customers :+ customer
      this

    def withGames(games: List[Game]): Builder =
      this.games = games
      this

    def addGame(game: Game): Builder =
      this.games = game :: this.games
      this

    def withSpawner(spawner: Spawner): Builder =
      this.spawner = Some(spawner)
      this

    def withoutSpawner(): Builder =
      this.spawner = None
      this

    def withWalls(walls: List[Wall]): Builder =
      this.walls = walls
      this

    def addWall(wall: Wall): Builder =
      this.walls = wall :: this.walls
      this

    def build(): SimulationState =
      SimulationState(customers, games, spawner, walls)

  def builder(): Builder = new Builder()

extension (state: SimulationState)
  def addCustomer(customer: Customer): SimulationState =
    state.copy(customers = state.customers :+ customer)

  def addGame(game: Game): SimulationState =
    state.copy(games = game :: state.games)

  def setSpawner(spawner: Spawner): SimulationState =
    state.copy(spawner = Some(spawner))

  def addWall(wall: Wall): SimulationState =
    state.copy(walls = wall :: state.walls)
