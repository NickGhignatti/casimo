package model.managers
import model.GlobalConfig
import model.entities.Bankroll
import model.entities.BoredomFrustration
import model.entities.CustState.Idle
import model.entities.CustomerState

//TODO: implement reasonable rule like if you have less bankroll than the minimum bet of the game
class PersistenceManager[
    A <: BoredomFrustration[A] & CustomerState[A] & Bankroll[A]
] extends BaseManager[Seq[A]]:
  def update(customers: Seq[A])(using config: GlobalConfig): Seq[A] =
    customers.map { c =>
      if c.boredom > 80 || c.frustration > 60 || c.bankroll <= 0.0 then
        c.changeState(Idle)
      else c
    }
