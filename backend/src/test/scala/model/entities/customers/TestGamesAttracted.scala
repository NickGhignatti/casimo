package model.entities.customers

import model.entities.GamesAttracted
import model.entities.games.BlackJackStrategy
import model.entities.games.Game
import model.entities.games.GameState
import model.entities.games.GameType
import model.entities.games.RouletteStrategy
import model.entities.games.SlotStrategy
import model.entities.games.dsl.use
import model.given_GlobalConfig
import model.managers.movements.Boids.MoverManager
import model.managers.movements.Context
import model.managers.movements.GamesAttractivenessManager
import model.managers.|
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D
import utils.Vector2D.distance

class TestGamesAttracted extends AnyFunSuite:
  private case class Customer(
    position: Vector2D,
    direction: Vector2D = Vector2D.zero,
    favouriteGames: Seq[GameType] = Seq.empty
  ) extends GamesAttracted[Customer]:

    override def updatedPosition(newPosition: Vector2D): Customer =
      copy(position = newPosition)

    override def updatedDirection(newDirection: Vector2D): Customer =
      copy(direction = newDirection)

  private val games = Seq(
    Game("Slot", Vector2D(0.0, 0.0), GameState(0, 1), use(SlotStrategy) bet 5.0 when true),
    Game("BlackJack", Vector2D(1.0, 0.0), GameState(0, 1), use(BlackJackStrategy) bet 5.0 when true),
    Game("Roulette", Vector2D(0.0, 1.0), GameState(0, 1), use(RouletteStrategy) bet 5.0 when true),
  )

  test("A customer should get closer to its favourite game"):
    val customer = Customer(Vector2D(1, 1), favouriteGames = Seq(GameType.SlotMachine))
    val context: Context[Customer] = Context(customer, games)
    val updatedCustomer = context | GamesAttractivenessManager() | MoverManager()
    assert(distance(updatedCustomer.position, games.head.position) <
      distance(customer.position, games.head.position))
