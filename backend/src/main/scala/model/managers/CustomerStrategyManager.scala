package model.managers

import model.GlobalConfig
import model.entities.customers.Bankroll
import model.entities.customers.CustomerState
import model.entities.customers.HasGameStrategy
/*TODO: make this manage which strategy the customer is using:
    checking the game is playing
    the bankroll available
    (optional) boredom and frustration
 */
//TODO: run scalafix -deprecation
class CustomerStrategyManager[
    A <: HasGameStrategy & CustomerState[A] & Bankroll[A]
] extends BaseManager[Seq[A]]:

  def update(slice: Seq[A])(using config: GlobalConfig): Seq[A] = ???
