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
  private val games = Seq(
    GameBuilder.slot(Vector2D.zero),
    GameBuilder.blackjack(Vector2D.zero),
    GameBuilder.roulette(Vector2D.zero)
  )

  test("A customer should get closer to its favourite game"):
    val customer = Customer().withPosition(Vector2D(1, 1)).withFavouriteGames(Seq(SlotMachine))
    val context: Context[Customer] = Context(customer, games)
    val updatedCustomer =
      (context | GamesAttractivenessManager()).player | MoverManager()
    assert(
      distance(updatedCustomer.position, games.head.position) <
        distance(customer.position, games.head.position)
    )

  test("A customer close to its favourite game should sit and play"):
    val customer = Customer().withPosition(Vector2D(2, 0)).withFavouriteGames(Seq(SlotMachine))
    val context: Context[Customer] = Context(customer, games)
    val updatedContext =
      context | PlayerSitterManager(sittingRadius = 10)
    assert(updatedContext.player.isPlaying)
    assert(updatedContext.player.position == games.head.position)
    assert(updatedContext.player.direction == Vector2D.zero)
    assert(updatedContext.games.head.gameState.playersId == Seq(customer.id))
