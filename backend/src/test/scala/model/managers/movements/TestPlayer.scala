package model.managers.movements

import model.entities.Player
import model.entities.customers.BoredomFrustration
import model.entities.games.Game
import model.entities.games.GameBuilder
import model.entities.games.GameType
import model.entities.games.SlotMachine
import model.managers.movements.Boids.MoverManager
import model.managers.movements.PlayerManagers.Context
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
      favouriteGame: GameType,
      isPlaying: Boolean = false,
      boredom: Double = 0,
      frustration: Double = 0
  ) extends Player[PlayerImpl],
        BoredomFrustration[PlayerImpl]:
    override def play(game: Game): PlayerImpl = copy(isPlaying = true)

    override def stopPlaying: PlayerImpl = copy(isPlaying = false)

    override val id: String = java.util.UUID.randomUUID().toString

    override def withPosition(newPosition: Vector2D): PlayerImpl =
      copy(position = newPosition)

    override def withDirection(newDirection: Vector2D): PlayerImpl =
      copy(direction = newDirection)

    override def withBoredom(newBoredom: Double): PlayerImpl =
      copy(boredom = newBoredom)

    override def withFrustration(newFrustration: Double): PlayerImpl =
      copy(frustration = newFrustration)

  test("A player should get closer to its favourite game"):
    val player = PlayerImpl(Vector2D(1, 1), favouriteGame = SlotMachine)
    val context = Context(player, games)
    val updatedPlayer =
      (context | GamesAttractivenessManager(0.1)).player | MoverManager()
    assert(
      distance(updatedPlayer.position, games.head.position) <
        distance(player.position, games.head.position)
    )

  test(
    "A player should no move if its favourite game is not present and its frustration should increase"
  ):
    val player = PlayerImpl(Vector2D(1, 1), favouriteGame = SlotMachine)
    val context = Context(player, games.filterNot(_.gameType == SlotMachine))
    val updatedPlayer = (context | GamesAttractivenessManager(0.1)).player
    assert(updatedPlayer.direction == Vector2D.zero)
    assert(updatedPlayer.frustration == 0.1)

  test("A player close to its favourite game should sit and play"):
    import model.managers.movements.PlayerManagers.Context
    val player = PlayerImpl(Vector2D(2, 0), favouriteGame = SlotMachine)
    val context = Context(player, games)
    val updatedContext =
      context | PlayerSitterManager(sittingRadius = 10)
    assert(updatedContext.player.isPlaying)
    assert(updatedContext.player.position == games.head.center)
    assert(updatedContext.player.direction == Vector2D.zero)
    assert(updatedContext.games.head.gameState.playersId == Seq(player.id))
