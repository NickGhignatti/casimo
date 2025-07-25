package model.spawner

import scala.language.implicitConversions
import scala.language.postfixOps

import model.SimulationState
import model.Ticker
import model.entities.customers.Customer
import model.entities.spawner.ConstantStrategy
import model.entities.spawner.GaussianStrategy
import model.entities.spawner.Spawner
import model.entities.spawner.StepStrategy
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D

class TestSpawner extends AnyFunSuite:
  val position: Vector2D = Vector2D(100, 200)
  val constantStrategy: ConstantStrategy = ConstantStrategy(3)
  val gaussianStrategy: GaussianStrategy = GaussianStrategy(100, 10, 2)
  val stepStrategy: StepStrategy = StepStrategy(2, 5, 9, 17)
  val instantTicker: Ticker =
    Ticker(60).withCurrentTick(1).withSpawnTick(1)

  test("Spawner should spawn correct number of customers based on strategy"):
    val spawner = Spawner("test-spawner", position, constantStrategy)
    val initialState =
      SimulationState(
        Seq.empty,
        List.empty,
        Some(spawner),
        List.empty,
        instantTicker
      )

    val newState = spawner.spawn(initialState)
    assert(newState.customers.size == 3)

  test("Spawner should update customers after spawning"):
    val spawner = Spawner("test-spawner", position, constantStrategy)
    val initialState =
      SimulationState(
        Seq.empty,
        List.empty,
        Some(spawner),
        List.empty,
        instantTicker
      )

    val newState = spawner.spawn(initialState)
    assert(newState.customers != initialState.customers)

  test("Spawner should position customers around spawner location"):
    val spawner = Spawner("test-spawner", position, constantStrategy)
    val initialState =
      SimulationState(
        Seq.empty,
        List.empty,
        Some(spawner),
        List.empty,
        instantTicker
      )

    val newState = spawner.spawn(initialState)
    for customer <- newState.customers do
      // Check if within 5 units in both x and y
      assert(math.abs(customer.position.x - position.x) <= 5.0)
      assert(math.abs(customer.position.y - position.y) <= 5.0)

  test("Spawner should create customers with valid properties"):
    val spawner = Spawner("test-spawner", position, constantStrategy)
    val initialState =
      SimulationState(
        Seq.empty,
        List.empty,
        Some(spawner),
        List.empty,
        instantTicker
      )

    val newState = spawner.spawn(initialState)
    for customer <- newState.customers do
      assert(customer.direction.x >= 0 && customer.direction.x < 5)
      assert(customer.direction.y >= 0 && customer.direction.y < 5)
      assert(customer.bankroll >= 30 && customer.bankroll < 5000)

  test("Spawner should work with GaussianStrategy"):
    val spawner =
      Spawner("gaussian-spawner", position, gaussianStrategy)
    val initialState =
      SimulationState(
        Seq.empty,
        List.empty,
        Some(spawner),
        List.empty,
        Ticker(60).withCurrentTick(10).withSpawnTick(1)
      )

    val newState = spawner.spawn(initialState)
    // At mean time, should spawn peak value
    assert(newState.customers.size == 100)

  test("Spawner should work with StepStrategy during active period"):
    val spawner = Spawner("step-spawner", position, stepStrategy)
    val initialState =
      SimulationState(
        Seq.empty,
        List.empty,
        Some(spawner),
        List.empty,
        Ticker(60).withCurrentTick(10).withSpawnTick(1)
      )

    val newState = spawner.spawn(initialState)
    // During active period (9-17), should spawn high rate
    assert(newState.customers.size == 5)

  test("Spawner should work with StepStrategy outside active period"):
    val spawner = Spawner("step-spawner", position, stepStrategy)
    val initialState =
      SimulationState(
        Seq.empty,
        List.empty,
        Some(spawner),
        List.empty,
        Ticker(5).withSpawnTick(1)
      )

    val newState = spawner.spawn(initialState)
    // Outside active period, should spawn low rate
    assert(newState.customers.size == 2)

  test("Spawner should maintain existing customers"):
    val existingCustomer =
      Customer("existing-1", Vector2D(50, 50), Vector2D(1, 1), 100)
    val spawner = Spawner("test-spawner", position, constantStrategy)
    val initialState =
      SimulationState(
        Seq(existingCustomer),
        List.empty,
        Some(spawner),
        List.empty,
        instantTicker
      )

    val newState = spawner.spawn(initialState)
    assert(newState.customers.size == 4)
    assert(newState.customers.exists(_.id == "existing-1"))

  test("Spawner should remain the same in the simulation state"):
    val spawner = Spawner("test-spawner", position, constantStrategy)
    val initialState =
      SimulationState(
        Seq.empty,
        List.empty,
        Some(spawner),
        List.empty,
        Ticker(7).withSpawnTick(1)
      )

    val newState = spawner.spawn(initialState)
    newState.spawner match
      case Some(updated) =>
        assert(updated == spawner)
      case None => fail("Spawner not found in state")

  test("Spawner with zero-spawn strategy should create no customers"):
    val zeroStrategy = ConstantStrategy(0)
    val spawner = Spawner("zero-spawner", position, zeroStrategy)
    val initialState =
      SimulationState(
        Seq.empty,
        List.empty,
        Some(spawner),
        List.empty,
        instantTicker
      )

    val newState = spawner.spawn(initialState)
    assert(newState.customers.isEmpty)

  test("Spawner when is not ready should not spawn"):
    val strategy = ConstantStrategy(1)
    val spawner = Spawner("test", position, strategy)
    val initialState =
      SimulationState(
        Seq.empty,
        List.empty,
        Some(spawner),
        List.empty,
        Ticker(0).withSpawnTick(2).update()
      )

    val newState = spawner.spawn(initialState)
    assert(newState.customers == initialState.customers)
