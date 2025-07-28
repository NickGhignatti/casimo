package model.managers

import model.entities.Entity
import model.entities.customers.Bankroll
import model.entities.customers.CustState.Idle
import model.entities.customers.CustState.Playing
import model.entities.customers.CustomerState
import model.entities.games.Game

case class CustomerBankrollManager[
    A <: CustomerState[A] & Bankroll[A] & Entity
](games: List[Game])
    extends BaseManager[Seq[A]]:
  def update(customers: Seq[A]): Seq[A] =
    customers.map { c =>
      c.customerState match
        case Playing(game) =>
          val updatedGame = games.find(_.id == game.id).get
          val gains = updatedGame.getLastRoundResult
            .filter(g => g.getCustomerWhichPlayed == c.id)
          if gains.nonEmpty then c.updateBankroll(-gains.head.getMoneyGain)
          else c

        case Idle => c
    }
