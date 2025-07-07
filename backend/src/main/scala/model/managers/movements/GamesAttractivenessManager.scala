package model.managers.movements

import model.GlobalConfig
import model.entities.GamesAttracted
import model.entities.games.Game
import model.managers.BaseManager
import utils.Vector2D.direction

case class Context[C <: GamesAttracted[C]](customers: Seq[C], games: Seq[Game])

case class GamesAttractivenessManager[C <: GamesAttracted[C]]()
    extends BaseManager[Context[C]]:
  override def update(
      slice: Context[C]
  )(using config: GlobalConfig): Context[C] =
    slice.copy(
      customers = slice.customers.map { customer =>
        val bestGame =
          slice.games.find(_.getGameType == customer.favouriteGames.head)
        bestGame match
          case Some(game) =>
            customer.updatedDirection(
              direction(customer.position, game.position)
            )
          case _ => customer
      }
    )
