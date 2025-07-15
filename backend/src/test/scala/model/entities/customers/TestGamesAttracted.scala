package model.entities.customers

import model.entities.GamesAttracted
import model.entities.games.Game
import model.entities.games.GameBuilder
import model.entities.games.GameType
import model.entities.games.SlotMachine
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
    GameBuilder.slot(Vector2D.zero),
    GameBuilder.blackjack(Vector2D.zero),
    GameBuilder.roulette(Vector2D.zero)
  )

  test("A customer should get closer to its favourite game"):
    val customer = Customer(Vector2D(1, 1), favouriteGames = Seq(SlotMachine))
    val context: Context[Customer] = Context(customer, games)
    val updatedCustomer =
      context | GamesAttractivenessManager() | MoverManager()
    assert(
      distance(updatedCustomer.position, games.head.position) <
        distance(customer.position, games.head.position)
    )
