package model.managers
import model.GlobalConfig
import model.entities.customers.CustState.Idle
import model.entities.customers.{Bankroll, BoredomFrustration, CustomerState}

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
