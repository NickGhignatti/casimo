package model.entities

import model.SimulationState
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D

import scala.util.Random

class TestSpawner extends AnyFunSuite:
  val spawner: Spawner = Spawner(Random.nextString(12), Vector2D.zero, 20, 2)

  test("Spawn the first 10 customers in the first tick"):
    val initialState = SimulationState(List.empty, List.empty, Some(spawner))
    val finalState = spawner.spawn(initialState)

    assert(initialState != finalState)
    assert(finalState.customers.size == 10)

  test("Spawn all the customers in the two ticks"):
    val initialState = SimulationState(List.empty, List.empty, Some(spawner))
    val midState = spawner.spawn(initialState)
    val finalState = spawner.spawn(midState)

    assert(midState != finalState && initialState != midState)
    assert(finalState.customers.size == 20)
