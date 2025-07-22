package model.managers

import alice.tuprolog._
import model.entities.Entity
import model.entities.customers.Bankroll
import model.entities.customers.BoredomFrustration
import model.entities.customers.CustState.Playing
import model.entities.customers.CustomerState
import model.entities.customers.FlatBetting
import model.entities.customers.HasBetStrategy
import model.entities.customers.Martingale
import model.entities.customers.OscarGrind
import model.entities.customers.StatusProfile
import model.entities.games.Blackjack
import model.entities.games.Game
import model.entities.games.Roulette
import model.entities.games.SlotMachine

class CustomerStrategyManager[
    A <: HasBetStrategy[A] & StatusProfile & CustomerState[A] & Bankroll[A] &
      Entity & BoredomFrustration[A]
](games: List[Game])
    extends BaseManager[Seq[A]]:

  def update(customers: Seq[A]): Seq[A] =
    val (playing, idle) = customers.partition(_.customerState match
      case Playing(_) => true
      case _          => false
    )
    val updateStepStrategy = playing.map(c =>
      val updatedGame = games.find(_.id == c.getGameOrElse.get.id).get

      val gain = -updatedGame.getLastRoundResult
        .filter(g => g.getCustomerWhichPlayed == c.id)
        .head
        .getMoneyGain
      c.updateAfter(gain)
    )
    val facts = createFacts(playing)
    val prologTheory = facts + rules
    val engine = new Prolog()
    engine.setTheory(new Theory(prologTheory))
    val updated = fetchStrategies(engine, playing)

    idle ++ updated

  private val rules: String =
    """
    % --- Customer Profile
profile(Cust, vip).
profile(Cust, regular).
profile(Cust, impulsive).
profile(Cust, casual).

% --- State
state(Cust, frustration, Frust).      % 0–100
state(Cust, boredom, Bored).          % 0–100
state(Cust, loss_streak, N).          %  ≥ 0
state(Cust, bankroll_current, Cur).
state(Cust, bankroll_start, Start).

% --- Current Game State
current_game(Cust, blackjack).
current_game(Cust, roulette).
current_game(Cust, slot).

% --- Logic Trigger

  % --- VIP Trigger
trigger(vip_blackjack_loss4, C) :-
    profile(C, vip),
    current_game(C, blackjack),
    state(C, loss_streak, N),
     nonvar(N),N >= 4,!.

trigger(vip_roulette_loss4, C) :-
    profile(C, vip),
    current_game(C, roulette),
    state(C, loss_streak, N),
     nonvar(N), N >= 4, !.

trigger(vip_slot_frustrated, C) :-
    profile(C, vip),
    current_game(C, slot),
    state(C, frustration, F),
    nonvar(F), F > 60, !.


  % --- Regular Trigger
trigger(regular_blackjack_loss3, C) :-
    profile(C, regular),
    current_game(C, blackjack),
    state(C, loss_streak, N),
    nonvar(N), N >= 3, !.


trigger(regular_roulette_bored, C) :-
    profile(C, regular),
    current_game(C, roulette),
    state(C, boredom, B),
    nonvar(B), B > 60, !.


trigger(regular_slot_frustrated, C) :-
	profile(C, regular),
    current_game(C, slot),
    state(C, frustration, F),
    nonvar(F), F > 70, !.


  % --- Impulsive Trigger
trigger(impulsive_blackjack_loss3, C) :-
    profile(C, impulsive),
    current_game(C, blackjack),
    state(C, loss_streak, N),
    nonvar(N), N >= 3, !.

trigger(impulsive_roulette_loss4, C) :-
    profile(C, impulsive),
    current_game(C, roulette),
    state(C, loss_streak, N),
    nonvar(N), N >= 4, !.

trigger(impulsive_slot_frustrated, C) :-
    profile(C, impulsive),
    current_game(C, slot),
    state(C, frustration, F),
    nonvar(F), F > 50, !.

% --- Strategy Link (name, params)

% VIP
strategy(C, oscar, [Bet]) :- trigger(vip_blackjack_loss4, C), base_bet(C, 3, Bet).
strategy(C, oscar, [Bet]) :- trigger(vip_roulette_loss4, C), base_bet(C, 3, Bet).
strategy(C, flat,  [Bet]) :- trigger(vip_slot_frustrated, C), base_bet(C, 1, Bet).

% Regular
strategy(C, flat,  [Bet]) :- trigger(regular_blackjack_loss3, C), base_bet(C, 2, Bet),!.
strategy(C, martingale, [Bet]) :- trigger(regular_roulette_bored, C), base_bet(C, 2, Bet),!.
strategy(C, flat,  [Bet]) :- trigger(regular_slot_frustrated,C), base_bet(C, 1, Bet).

% Casual


% Impulsive
strategy(C, oscar, [Bet]) :- trigger(impulsive_blackjack_loss3, C), base_bet(C, 3, Bet).
strategy(C, flat,  [Bet]) :- trigger(impulsive_roulette_loss4, C), base_bet(C, 3, Bet).
strategy(C, flat,  [Bet]) :- trigger(impulsive_slot_frustrated, C), base_bet(C, 2, Bet).


% --- Base bet percentage: Base is (Current * Percent) // 100
base_bet(C, Percent, Base) :-
    state(C, bankroll_current, Cur),
    Base is (Cur * Percent) // 100, !.

    """

  private def createFacts(cs: Seq[A]): String =
    cs.map { c =>
      val id = s"'${c.id}'"
      val fr = f"${c.frustration.toInt}"
      val bo = f"${c.boredom.toInt}"
      val bs = f"${c.bankroll.toInt}"
      val b0 = f"${c.startingBankroll.toInt}"

      val ls = c.betStrategy match
        case martingale: Martingale[A] => martingale.lossStreak
        case oscarGrind: OscarGrind[A] => oscarGrind.lossStreak
        case _                         => 0

      val gmRaw = c.getGameOrElse.get.gameType match
        case Blackjack   => "blackjack"
        case Roulette    => "roulette"
        case SlotMachine => "slot"
      val gm = gmRaw.replaceAll("[^a-z0-9_]", "_")
      // Clean profile from illegal prolog character
      val pr = c.riskProfile.toString.toLowerCase.replaceAll("[^a-z0-9_]", "_")
      s"""
         |profile($id, ${c.riskProfile.toString.toLowerCase}).
         |state($id, frustration, $fr).
         |state($id, boredom, $bo).
         |state($id, loss_streak, $ls).
         |state($id, bankroll_start, $b0).
         |state($id, bankroll_current, $bs).
         |current_game($id, $gm).
       """.stripMargin
    }.mkString("\n")

  private def fetchStrategies(engine: Prolog, playing: Seq[A]): Seq[A] =
    playing.map { c =>
      val id = s"'${c.id}'"
      val goal = s"strategy($id, Strat, Params)."
      val solve = engine.solve(goal)
      if solve.isSuccess then
        val name = solve.getTerm("Strat").toString
        val params = solve.getTerm("Params").asInstanceOf[Struct]
        val base = params.getArg(0).toString.toDouble
        val strat = name match
          case "flat"       => FlatBetting[A](base, c.betStrategy.option)
          case "martingale" => Martingale[A](base, c.betStrategy.option)
          case "oscar" => OscarGrind[A](base, c.bankroll, c.betStrategy.option)
          case _       => FlatBetting[A](base, c.betStrategy.option)
        c.changeBetStrategy(strat)
      else c
    }
