package model.managers.movements

import model.entities.Player
import model.entities.games.Game
import model.entities.games.GameBuilder
import model.entities.games.GameType
import model.entities.games.SlotMachine
import model.managers.movements.Boids.MoverManager
import model.managers.movements.PlayerManagers.GamesAttractivenessManager
import model.managers.movements.PlayerManagers.PlayerSitterManager
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

  private case class PlayerImpl(
      position: Vector2D,
      direction: Vector2D = Vector2D.zero,
      favouriteGames: Seq[GameType],
      isPlaying: Boolean = false
  ) extends Player[PlayerImpl]:
    override def play(game: Game): PlayerImpl = copy(isPlaying = true)

    override def stopPlaying: PlayerImpl = copy(isPlaying = false)

    override val id: String = java.util.UUID.randomUUID().toString

    override def withPosition(newPosition: Vector2D): PlayerImpl =
      copy(position = newPosition)

    override def withDirection(newDirection: Vector2D): PlayerImpl =
      copy(direction = newDirection)

  test("A customer should get closer to its favourite game"):
    import model.managers.movements.PlayerManagers.Context
    val customer = PlayerImpl(Vector2D(1, 1), favouriteGames = Seq(SlotMachine))
    val context = Context(customer, games)
    val updatedCustomer =
      (context | GamesAttractivenessManager()).player | MoverManager()
    assert(
      distance(updatedCustomer.position, games.head.position) <
        distance(customer.position, games.head.position)
    )

  test("A customer close to its favourite game should sit and play"):
    import model.managers.movements.PlayerManagers.Context
    val customer = PlayerImpl(Vector2D(2, 0), favouriteGames = Seq(SlotMachine))
    val context = Context(customer, games)
    val updatedContext =
      context | PlayerSitterManager(sittingRadius = 10)
    assert(updatedContext.player.isPlaying)
    assert(updatedContext.player.position == games.head.position)
    assert(updatedContext.player.direction == Vector2D.zero)
    assert(updatedContext.games.head.gameState.playersId == Seq(customer.id))
