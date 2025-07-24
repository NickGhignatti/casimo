package update

import model.SimulationState
import model.data.DataManager
import model.entities.Wall
import model.setSpawner
import model.entities.customers.Customer
import model.entities.customers.DefaultMovementManager
import model.entities.games.GameBuilder
import model.entities.spawner.{ConstantStrategy, Spawner}
import org.scalatest.funsuite.AnyFunSuite
import update.Event.{
  AddCustomers,
  ResetSimulation,
  UpdateWalls,
  updateGamesList
}
import utils.Vector2D

class TestUpdate extends AnyFunSuite:
  val initState: SimulationState =
    SimulationState(List.empty, List.empty, None, List.empty)

  test("update should leave state unchanged when no events affect it"):
    val update = Update(DefaultMovementManager())
    val endState = update.update(initState, Event.SimulationTick)
    assert(endState === initState)

  test(
    "update should leave state unchanged except for customers if there is a spawner"
  ):
    val update = Update(DefaultMovementManager())
    val newState = initState.setSpawner(
      Spawner("spawner", Vector2D.zero, ConstantStrategy(1), 0.0, 1.0)
    )
    val endState = update.update(newState, Event.SimulationTick)
    assert(endState !== newState)
    assert(endState.customers.size > newState.customers.size)

  test("update should update the data manager"):
    val manager = DataManager(initState)
    val finalState =
      SimulationState(
        List.empty,
        List.fill(1)(GameBuilder.slot(Vector2D.zero)),
        None,
        List.empty
      )
    val update = Update(DefaultMovementManager())
    assert(
      update
        .updateSimulationDataManager(manager, finalState)
        .state == finalState
    )

  test("update should update the games when added"):
    val update = Update(DefaultMovementManager())
    val slot = GameBuilder.slot(Vector2D.zero)
    val firstState =
      update.update(
        initState,
        updateGamesList(List(slot))
      )
    assert(firstState.games == List(slot))
    val roulette = GameBuilder.roulette(Vector2D.zero)
    val secondState =
      update.update(firstState, updateGamesList(List(slot, roulette)))
    assert(secondState.games == List(slot, roulette))

  test("reset should return a new empty simulation"):
    val simulationState = SimulationState(
      Seq(Customer().withId("test1")),
      List(GameBuilder.slot(Vector2D.zero)),
      None,
      List.empty
    )
    val update = Update(DefaultMovementManager())
    assert(
      SimulationState.empty() == update.update(simulationState, ResetSimulation)
    )

  test("AddCustomers should set the spawner"):
    val update = Update(DefaultMovementManager())

    assert(
      update
        .update(initState, AddCustomers(ConstantStrategy(1)))
        .spawner
        .isDefined
    )

  test("UpdateWalls should update the walls in the casinò"):
    val update = Update(DefaultMovementManager())

    assert(
      update
        .update(initState, UpdateWalls(List(Wall(Vector2D.zero, 100, 100))))
        .walls
        .size == 1
    )
