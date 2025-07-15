package model.managers.movements

import model.entities.GamesAttracted
import model.entities.customers.Movable
import model.entities.games.Game
import model.managers.BaseManager
import utils.Vector2D
import utils.Vector2D.direction

case class Context[C <: GamesAttracted[C]](customer: C, games: Seq[Game])
    extends Movable[Context[C]]:
  override def updatedPosition(newPosition: Vector2D): Context[C] =
    this.copy(customer = customer.updatedPosition(newPosition))

  override def updatedDirection(newDirection: Vector2D): Context[C] =
    this.copy(customer = customer.updatedDirection(newDirection))

  export customer.{position, direction}

case class GamesAttractivenessManager[C <: GamesAttracted[C]]()
    extends BaseManager[Context[C]]:
  override def update(
      slice: Context[C]
  ): Context[C] =
    slice.copy(
      customer =
        val customer = slice.customer
        val bestGame =
          slice.games.find(_.gameType == customer.favouriteGames.head)
        bestGame match
          case Some(game) =>
            customer.updatedDirection(
              direction(customer.position, game.position)
            )
          case _ => customer
    )
