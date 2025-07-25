package model.entities.games

import model.Ticker
import model.entities.customers
import model.entities.customers.CustState
import model.entities.customers.Customer
import utils.Result
import utils.Result.Failure
import utils.Result.Success

/** Resolves game interactions between customers and games in the simulation.
  *
  * The GameResolver is responsible for processing all active games by
  * identifying playing customers, executing their bets, and updating game
  * histories with the results. It handles the coordination between customer
  * actions and game state updates in each simulation tick.
  */
object GameResolver:
  private def playGame(game: Game, customers: List[Customer]): Game =
    val playingCustomers = customers.filter(c =>
      c.customerState match
        case CustState.Playing(customerGame) => game.id == customerGame.id
        case CustState.Idle                  => false
    )
    playingCustomers.foldLeft(game)((g, c) =>
      g.play(c.placeBet()) match
        case Result.Success(value) =>
          value match
            case Result.Success(lostValue) => g.updateHistory(c.id, -lostValue)
            case Result.Failure(winValue)  => g.updateHistory(c.id, winValue)
        case Result.Failure(error) => g.updateHistory(c.id, 0.0)
    )

  /** Updates all games by processing customer interactions for each game.
    *
    * Applies the playGame logic to every game in the simulation, ensuring that
    * all customer bets are processed and game histories are updated accordingly
    *
    * @param customers
    *   the complete list of customers in the simulation
    * @param games
    *   the complete list of games in the simulation
    * @return
    *   updated list of games with new history entries from customer
    *   interactions
    */
  def update(
      customers: List[Customer],
      games: List[Game],
      ticker: Ticker
  ): List[Game] =
    games.map(g =>
      if ticker.isGameReady(g.gameType) then
        playGame(g.setPlaying(true), customers)
      else g.setPlaying(false)
    )
