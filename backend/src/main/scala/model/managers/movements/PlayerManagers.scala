package model.managers.movements

import model.entities.Entity
import model.entities.Player
import model.entities.customers.Movable
import model.entities.games.Game
import model.managers.BaseManager
import model.managers.WeightedManager
import utils.Result
import utils.Vector2D
import utils.Vector2D.direction
import utils.Vector2D.distance

case class Context[P <: Player[P] & Entity & Movable[P]](
    player: P,
    games: Seq[Game]
):
  protected[movements] def bestGameAvailable: Option[Game] =
    extension [A](result: Result[A, A])
      private def option(): Option[A] =
        result match
          case Result.Success(value) => Some(value)
          case _                     => None
    games
      .find(_.gameType == player.favouriteGames.head)
      .flatMap(_.lock(player.id).option())

case class GamesAttractivenessManager[C <: Player[C] & Entity & Movable[C]](
    weight: Double = 1.0
) extends WeightedManager[Context[C]]:
  override def update(
      slice: Context[C]
  ): Context[C] =
    slice.copy(
      player =
        val customer = slice.player
        val bestGame = slice.bestGameAvailable
        bestGame match
          case Some(game) =>
            customer.withDirection(
              direction(customer.position, game.position) * weight
            )
          case _ => customer
    )

  override def updatedWeight(weight: Double): WeightedManager[Context[C]] =
    copy(weight = weight)

case class PlayerSitterManager[C <: Player[C] & Entity & Movable[C]](
    sittingRadius: Double
) extends BaseManager[Context[C]]:

  override def update(slice: Context[C]): Context[C] =
    val bestGame = slice.bestGameAvailable
    bestGame match
      case Some(game)
          if distance(slice.player.position, game.position) < sittingRadius =>
        slice.copy(
          player = slice.player
            .withPosition(game.position)
            .withDirection(Vector2D.zero)
            .play,
          games = slice.games
            .filter(_.id == game.id)
            .map(_ => game)
        )
      case _ => slice
