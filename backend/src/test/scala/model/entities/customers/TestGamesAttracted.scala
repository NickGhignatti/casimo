package model.entities.customers

import model.entities.GamesAttracted
import model.entities.games.dsl.use
import model.entities.games.{BlackJackStrategy, Game, GameState, GameType, RouletteStrategy, SlotStrategy}
import model.managers.movements.Boids.MoverManager
import model.managers.movements.{Context, GamesAttractivenessManager}
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D
import utils.Vector2D.distance
import model.given_GlobalConfig

import scala.util.chaining.scalaUtilChainingOps

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

  private val mover = MoverManager[Customer]()

  test("A customer should get closer to its favourite game"):
    val customer = Customer(Vector2D(1, 1), favouriteGames = Seq(GameType.SlotMachine))
    val manager = GamesAttractivenessManager[Customer]()
    val context: Context[Customer] = Context(Seq(customer), games)
    manager.update(context).customers pipe mover.update match
      case Seq(updatedCustomer) =>
        assert(distance(updatedCustomer.position, games.head.position) <
          distance(customer.position, games.head.position))
      case _ => fail("Expected one updated customer")
