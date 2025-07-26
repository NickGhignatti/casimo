package model

import model.entities.Wall
import model.entities.customers.Customer
import model.entities.games.BlackJackGame
import model.entities.games.GameBuilder
import model.entities.games.RouletteGame
import model.entities.games.SlotMachineGame
import model.entities.spawner.ConstantStrategy
import model.entities.spawner.Spawner
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D

class TestSimulationState extends AnyFunSuite:
  val wall1: Wall = Wall("wall1", Vector2D.zero, 100, 100)
  val customer1: Customer = Customer().withId("customer1")
  val customer2: Customer = Customer().withId("customer2")
  val customer3: Customer = Customer().withId("customer3")
  val game1: SlotMachineGame = GameBuilder.slot(Vector2D.zero)
  val game2: RouletteGame = GameBuilder.roulette(Vector2D.zero)
  val game3: BlackJackGame = GameBuilder.blackjack(Vector2D.zero)
  val spawner: Spawner = Spawner("spawner", Vector2D.zero, ConstantStrategy(3))

  test("builder should start with empty collections"):
    val state = SimulationState.builder().build()

    assert(state.customers == Seq.empty)
    assert(state.games == List.empty)
    assert(state.spawner.isEmpty)
    assert(state.walls == List.empty)

  test("addCustomer should add single customer"):
    val state = SimulationState
      .builder()
      .addCustomer(customer1)
      .build()

    assert(state.customers.size == 1)
    assert(state.customers.contains(customer1))

  test("withCustomers should set entire customer collection"):
    val customers = Seq(customer1, customer2, customer3)
    val state = SimulationState
      .builder()
      .withCustomers(customers)
      .build()

    assert(state.customers.size == 3)
    assert(state.customers == customers)

  test("addGame should add single game"):
    val state = SimulationState
      .builder()
      .addGame(game1)
      .build()

    assert(state.games.size == 1)
    assert(state.games.contains(game1))

  test("withGames should set entire games collection"):
    val games = List(game1, game2, game3)
    val state = SimulationState
      .builder()
      .withGames(games)
      .build()

    assert(state.games.size == 3)
    assert(state.games == games)

  test("withSpawner should set spawner"):
    val state = SimulationState
      .builder()
      .withSpawner(spawner)
      .build()

    assert(state.spawner.contains(spawner))

  test("withoutSpawner should remove spawner"):
    val state = SimulationState
      .builder()
      .withSpawner(spawner)
      .withoutSpawner()
      .build()

    assert(state.spawner.isEmpty)

  test("addWall should add single wall"):
    val state = SimulationState
      .builder()
      .addWall(wall1)
      .build()

    assert(state.walls.size == 1)
    assert(state.walls.contains(wall1))

  test("builder should support method chaining for all operations"):
    val state = SimulationState
      .builder()
      .addCustomer(customer1)
      .addCustomer(customer2)
      .addGame(game1)
      .addGame(game2)
      .withSpawner(spawner)
      .withWalls(List(wall1))
      .build()

    assert(
      state.customers.contains(customer1) && state.customers.contains(customer2)
    )
    assert(state.games.contains(game1) && state.games.contains(game2))
    assert(state.spawner.contains(spawner))
    assert(state.walls.contains(wall1))

  test("simulation state should allow to add entites after"):
    val state = SimulationState
      .empty()
      .addCustomer(customer1)
      .addGame(game1)
      .addWall(wall1)

    assert(state.customers == List(customer1))
    assert(state.games == List(game1))
    assert(state.walls == List(wall1))

  test("empty simulation state should have spawner if setted"):
    val state = SimulationState
      .empty()
      .setSpawner(spawner)

    assert(state.spawner.contains(spawner))

  test("update framerate should update both framerate and ticker"):
    val initialState = SimulationState.empty()
    val updatedState = initialState.updateFrameRate(120.0)

    assert(initialState.frameRate != updatedState.frameRate)
    assert(updatedState.frameRate == 120)
    assert(initialState.ticker != updatedState.ticker)
