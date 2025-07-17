package model.entities.games

import model.entities.customers.CustState
import model.entities.customers.Customer
import utils.Result
import utils.Result.Failure

object GameResolver:
  def update(customers: List[Customer], games: List[Game]): List[Game] =
    // TODO: waiting for APIs
    val playingCustomers = customers.filter(customer =>
      customer.customerState match
        case CustState.Playing(game) => true
        case CustState.Idle          => false
    )
    playingCustomers.map { customer =>
      (customer.customerState: @unchecked) match
        case CustState.Playing(game) =>
          game.play(SlotBet(10.0)) match // TODO
            case Result.Success(value) =>
              value match
                case Result.Success(winValue)  => game.updateHistory(winValue)
                case Result.Failure(lostValue) => game.updateHistory(-lostValue)
            case Result.Failure(error) => game.updateHistory(0.0)
    }
