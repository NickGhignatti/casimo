package model.managers

import alice.tuprolog.SolveInfo
import alice.tuprolog.Term
import model.entities.customers.Bankroll
import model.entities.customers.CustState.Idle
import model.entities.customers.CustState.Playing
import model.entities.customers.CustomerState
import model.entities.customers.HasBetStrategy
import utils.Scala2P.mkPrologEngine
/*TODO: provare a farlo con prolog (⊙_⊙;)
 */
class CustomerStrategyManager[
    A <: HasBetStrategy[A] & CustomerState[A] & Bankroll[A]
] extends BaseManager[Seq[A]]:

  def update(customers: Seq[A]): Seq[A] =
    val (playingCustomers, idleCustomers) =
      customers.partition(_.customerState match
        case Playing(_) => true
        case Idle       => false
      )

    val rules: String =
      """
        |%--- Profile
        |profile(john, vip).
        |
        |%--- Dynamic State
        |state(john, frustration, 0.6).  % 60% frustration
        |state(john, boredom, 0.2).      % 20% boredom
        |state(john, loss_streak, 2).
        |
        |%--- Current game
        |current_game(john, blackjack).
        |
        |%--- Trigger
        |trigger(vip_blackjack_frustrated, Cust) :-
        |  profile(Cust, vip),
        |  current_game(Cust, blackjack),
        |  state(Cust, frustration, F), F > 0.5.
        |
        |trigger(roulette_loss2, Cust) :-
        |  current_game(Cust, roulette),
        |  state(Cust, loss_streak, N), N >= 2.
        |
        |trigger(slot_frustrated, Cust) :-
        |  current_game(Cust, slot),
        |  state(Cust, frustration, F), F > 0.5.
        |
        |%--- Correspondent Strategy
        |strategy(Cust, Strategy) :-
        |  trigger(vip_blackjack_frustrated, Cust), Strategy = kelly;
        |  trigger(roulette_loss2, Cust), Strategy = martingale;
        |  trigger(slot_frustrated, Cust), Strategy = flat;
        |  Strategy = flat.  % fallback
        """.stripMargin

    val facts = createFacts(playingCustomers)

    val engine: Term => LazyList[SolveInfo] = mkPrologEngine(facts + rules)
    /*val startPos = Term.createTerm(
      s"pos(${player.position.x - padding.x},${player.position.y - padding.y})"
    )

    val input = Struct("build_distances", startPos)
    engine(input).headOption
     */

    idleCustomers ++ fetchStrategies(engine)

  private def createFacts(customer: Seq[A]): String = ???
  /*val facts = new StringBuilder

    facts ++=
      s"player(pos(${customer.position.x - padding.x},${customer.position.y - padding.y}), ${customer.visibility()}).\n"

    currentRoom.items.foreach(i =>
      facts ++=
        s"obstacle(pos(${i.position.get.x - padding.x},${i.position.get.y - padding.y})).\n"
    )

    currentRoom.getAliveEnemies.foreach(e =>
      facts ++=
        s"enemy(${e.id},pos(${e.position.x - padding.x},${e.position.y - padding.y})).\n" +
          s"obstacle(pos(${e.position.x - padding.x},${e.position.y - padding.y})).\n"
    )

    facts ++= s"distance(pos(${customer.position.x - padding.x},${customer.position.y - padding.y}), 0).\n"
    facts.toString()

  private case class Move(
                           id: String,
                           cur: Point2D,
                           next: Point2D,
                           cost: scala.Int
                         )*/

  private def fetchStrategies(engine: Term => LazyList[SolveInfo]): Seq[A] = ???
  /*val mVar = Var("M")

    engine(Struct("best_moves_all", mVar)).headOption.fold(Nil): info =>
      val listMoves = info.getVarValue("M").asInstanceOf[Struct]
      if listMoves.isEmptyList then Nil
      else
        import scala.jdk.CollectionConverters.*
        listMoves
          .listIterator()
          .asScala
          .toList
          .map: t =>
            val mv = t.asInstanceOf[Struct]
            Move(
              mv.getArg(0).getTerm.toString,
              asPoint(mv.getArg(1).getTerm),
              asPoint(mv.getArg(2).getTerm),
              asInt(mv.getArg(3).getTerm)
            )*/
/*
  private def groupMoves(moves: List[Move]): Map[String, List[Point2D]] =
    moves
      .groupMap(_.id)(m => (m.next, m.cost))
      .map((id, moves) =>
        val min = moves.map(_._2).min
        id -> moves.filter(_._2.equals(min)).map(_._1)
      )

  private def decideMoves(
                           moves: Map[String, List[Point2D]]
                         ): Map[String, Point2D] =
    val order = moves.toList.sortBy(_._2.size).map(_._1)
    val (_, chosen) =
      order.foldLeft((Set.empty[Point2D], Map.empty[String, Point2D])):
        case ((reserved, steps), id) =>
          moves(id).find(!reserved(_)) match
            case Some(p) => (reserved + p, steps.updated(id, p))
            case None    => (reserved, steps)

    chosen*/
