package model.entities.customers

import model.entities.Player
import model.entities.games.Game
import model.entities.games.GameBuilder
import model.entities.games.GameType
import model.entities.games.SlotMachine
import model.managers.movements.Boids.MoverManager
import model.managers.movements.Context
import model.managers.movements.GamesAttractivenessManager
import model.managers.movements.PlayerSitterManager
import model.managers.|
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D
import utils.Vector2D.distance

class TestPlayer extends AnyFunSuite:
  private case class Customer(
      position: Vector2D,
      direction: Vector2D = Vector2D.zero,
      favouriteGames: Seq[GameType] = Seq.empty,
      isPlaying: Boolean = false
  ) extends Player[Customer]:

    override def updatedPosition(newPosition: Vector2D): Customer =
      copy(position = newPosition)

    override def updatedDirection(newDirection: Vector2D): Customer =
      copy(direction = newDirection)

    override val id: String = java.util.UUID.randomUUID().toString

    override def play: Customer = copy(isPlaying = true)

    override def stopPlaying: Customer = copy(isPlaying = false)

  private val games = Seq(
    GameBuilder.slot(Vector2D.zero),
    GameBuilder.blackjack(Vector2D.zero),
    GameBuilder.roulette(Vector2D.zero)
  )

  test("A customer should get closer to its favourite game"):
    val customer = Customer(Vector2D(1, 1), favouriteGames = Seq(SlotMachine))
    val context: Context[Customer] = Context(customer, games)
    val updatedCustomer =
      (context | GamesAttractivenessManager()).player | MoverManager()
    assert(
      distance(updatedCustomer.position, games.head.position) <
        distance(customer.position, games.head.position)
    )

  test("A customer close to its favourite game should sit and play"):
    val customer = Customer(Vector2D(2, 0), favouriteGames = Seq(SlotMachine))
    val context: Context[Customer] = Context(customer, games)
    val updatedContext =
      context | PlayerSitterManager(sittingRadius = 10)
    assert(updatedContext.player.isPlaying)
    assert(updatedContext.player.position == games.head.position)
    assert(updatedContext.player.direction == Vector2D.zero)
    assert(updatedContext.games.head.gameState.playersId == Seq(customer.id))
