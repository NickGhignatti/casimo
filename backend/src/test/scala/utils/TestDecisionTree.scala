package utils
import model.entities.customers.CustState.Playing
import model.entities.customers.{Customer, RiskProfile, StatusProfile}
import model.entities.customers.RiskProfile.{Casual, Impulsive, Regular, VIP}
import model.entities.games.{Blackjack, GameBuilder, Roulette}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class TestDecisionTree extends AnyFunSuite with Matchers:

  test("Leaf should return the function result"):
    val leaf = Leaf[Customer, Double](_.bankroll / 2)
    val c = Customer().withBankroll(1000.0)
    leaf.eval(c) shouldBe 500.0
  

  test("DecisionNode should choose the branches correctly"):
    val node = DecisionNode[Customer, String](_.boredom >= 50, Leaf(_ => "high"), Leaf(_ => "low"))
    node.eval(Customer().withBoredom(80.0)) shouldBe "high"
    node.eval(Customer().withBoredom(20.0)) shouldBe "low"
  

  test("MultiNode selects branch or throws"):
    val m = MultiNode[Customer, RiskProfile, String](
      keyOf = _.riskProfile,
      branches = Map(VIP -> Leaf(_ => "VIP profile"), Regular -> Leaf(_ => "Regular Profile")),
      default = Leaf(_ => "Unknown profile")
    )
    m.eval(Customer().withProfile(VIP)) shouldBe "VIP profile"
    m.eval(Customer().withProfile(Regular)) shouldBe "Regular Profile"
    m.eval(Customer().withProfile(Impulsive)) shouldBe "Unknown profile"
  

  test("Nested DecisionTree evaluation"):
    val tree: DecisionTree[Customer, String] =
      DecisionNode[Customer, String](
        predicate = _.riskProfile == VIP,
        trueBranch = MultiNode(
          keyOf = _.getGameOrElse.get.gameType,
          branches = Map(Blackjack -> Leaf(_ => "customer is a VIP playing Blackjack"), Roulette -> Leaf(_ => "customer is a VIP playing Roulette")),
          default = Leaf(_ => "customer is a VIP playing playing an unknown game")
        ),
        falseBranch = Leaf(_ => "customer is not a VIP")
      )
    tree.eval(Customer().withProfile(VIP).withCustomerState(Playing(GameBuilder.blackjack(Vector2D.zero)))) shouldBe "customer is a VIP playing Blackjack"
    tree.eval(Customer().withProfile(VIP).withCustomerState(Playing(GameBuilder.roulette(Vector2D.zero)))) shouldBe "customer is a VIP playing Roulette"
    tree.eval(Customer().withProfile(VIP).withCustomerState(Playing(GameBuilder.slot(Vector2D.zero)))) shouldBe "customer is a VIP playing playing an unknown game"
    tree.eval(Customer().withProfile(Casual)) shouldBe "customer is not a VIP"

  
