package model.entities.games

import model.entities.customers
import model.entities.customers.CustState
import model.entities.customers.Customer
import utils.Result
import utils.Result.Failure

object GameResolver:
  private def playGame(game: Game, customers: List[Customer]): Game =
    val playingCustomers = customers.filter(c =>
      c.customerState match
        case CustState.Playing(customerGame) => game.id == customerGame.id
        case CustState.Idle                  => false
    )
    playingCustomers.foldRight(game)((c, g) =>
      g.play(c.placeBet()) match
        case Result.Success(value) =>
          value match
            case Result.Success(winValue)  => g.updateHistory(winValue)
            case Result.Failure(lostValue) => game.updateHistory(-lostValue)
        case Result.Failure(error) => game.updateHistory(0.0)
    )

  def update(customers: List[Customer], games: List[Game]): List[Game] =
    games.map(g => playGame(g, customers))
