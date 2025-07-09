package model.managers
import model.GlobalConfig
import model.entities.customers.Bankroll
import model.entities.customers.BoredomFrustration
import model.entities.customers.CustState.Idle
import model.entities.customers.CustState.Playing
import model.entities.customers.CustomerState

//TODO: implement reasonable rule like if you have less bankroll than the minimum bet of the game
class PersistenceManager[
    A <: BoredomFrustration[A] & CustomerState[A] & Bankroll[A]
] extends BaseManager[Seq[A]]:
  def update(customers: Seq[A])(using config: GlobalConfig): Seq[A] =
    customers.map { c =>
      c.customerState match
        case Playing(game) =>
          if c.boredom > 80 || c.frustration > 60 || c.bankroll < 0.1 /* game.gameState.minimumBet*/
          then c.changeState(Idle)
          else c
        case Idle => c
    }
