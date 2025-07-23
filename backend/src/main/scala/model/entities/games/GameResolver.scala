package model.entities.games

import model.entities.customers
import model.entities.customers.CustState
import model.entities.customers.Customer
import utils.Result
import utils.Result.Failure
import utils.Result.Success

object GameResolver:
  private def playGame(game: Game, customers: List[Customer]): Game =
    val playingCustomers = customers.filter(c =>
      c.customerState match
        case CustState.Playing(customerGame) => game.id == customerGame.id
        case CustState.Idle                  => false
    )
    playingCustomers.foldRight(game)((c, g) =>
      val result = g.play(c.placeBet())
      if result.isSuccess then
        result.getOrElse(Success(0.0)) match
          case Result.Success(lostValue) => g.updateHistory(c.id, -lostValue)
          case Result.Failure(winValue)  => g.updateHistory(c.id, winValue)
      else g
    )

  def update(customers: List[Customer], games: List[Game]): List[Game] =
    games.map(g => playGame(g, customers))
