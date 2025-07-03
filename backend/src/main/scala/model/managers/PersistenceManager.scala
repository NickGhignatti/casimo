package model.managers
import model.GlobalConfig
import model.entities.CustState.Idle
import model.entities.{BoredomFrustration, CustomerState}

//TODO: add complex behaviour, maybe based on riskProfile
class PersistenceManager[A <: BoredomFrustration[A] & CustomerState[A]]
    extends BaseManager[Seq[A]]:
  def update(customers: Seq[A])(using config: GlobalConfig): Seq[A] =
    customers.map { c =>
      if c.boredom > 80 || c.frustration > 60 then c.changeState(Idle)
      else c
    }
