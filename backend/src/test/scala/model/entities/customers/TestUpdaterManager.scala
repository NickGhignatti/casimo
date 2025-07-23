package model.entities.customers

import model.SimulationState
import model.entities.games.GameBuilder
import model.entities.games.GameState
import model.managers.BaseManager
import model.managers.|
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D

class TestUpdaterManager extends AnyFunSuite:
  private val games = List(
    GameBuilder
      .slot(Vector2D(10, 10))
      .copy(
        id = "slot",
        gameState = GameState(
          currentPlayers = 2,
          maxAllowedPlayers = 2,
          playersId = List("1", "3")
        )
      ),
    GameBuilder
      .roulette(Vector2D.zero)
      .copy(
        id = "roulette"
      )
  )
  private val customers = Seq(
    Customer().withId("1").play(games.head),
    Customer().withId("2"),
    Customer().withId("3").play(games.head),
    Customer().withId("4")
  )
  private val state = SimulationState(
    customers = customers,
    games = games,
    walls = List(),
    spawner = Option.empty
  )

  test(
    "UpdaterManager should filter out customers which are playing and games which are full but output the same number of customers"
  ):
    case class IdentityManager() extends BaseManager[SimulationState]:
      override def update(slice: SimulationState): SimulationState =
        assert(slice.customers.map(_.id) == Seq("2", "4"))
        assert(slice.games.map(_.id) == Seq("roulette"))
        slice
    val filteredState = state | FilterManager(IdentityManager())
    assert(filteredState.customers.size == 4)
    assert(filteredState.games.size == 2)
