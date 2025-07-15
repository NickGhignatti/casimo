package model.managers

import model.entities.customers.Bankroll
import model.entities.customers.CustomerState
import model.entities.customers.HasBetStrategy
/*TODO: make this manage which strategy the customer is using:
    checking the game is playing
    the bankroll available
    (optional) boredom and frustration
 */
class CustomerStrategyManager[
    A <: HasBetStrategy[A] & CustomerState[A] & Bankroll[A]
] extends BaseManager[Seq[A]]:

  def update(slice: Seq[A]): Seq[A] = ???
