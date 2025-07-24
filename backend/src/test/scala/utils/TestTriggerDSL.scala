package utils
import model.entities.customers.{Customer, MartingaleStrat, defaultRedBet}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import utils.TriggerDSL.{Always, BoredomAbove, BrRatioAbove, FrustAbove, Losses, Trigger}

class TestTriggerDSL extends AnyFunSuite with Matchers:
  test("FrustrationAbove trigger returns true when exceeded") :
    val c = Customer().withFrustration(80)
    FrustAbove(60).eval(c) shouldBe true
  
  test("BankrollRatioAbove trigger works correctly") :
    val c = Customer().withBankroll(1000.0).updateBankroll(500.0)
    BrRatioAbove(1.4).eval(c) shouldBe true
    BrRatioAbove(2.0).eval(c) shouldBe false
  
  test("And trigger only passes if both do") :
    val c = Customer().withFrustration(80).withBoredom(90)
    val trigger: Trigger[Customer] = FrustAbove(60) && BoredomAbove(80)
    trigger.eval(c) shouldBe true
  
  test("Or trigger passes if at least one condition is met") :
    val c = Customer().withFrustration(90).withBoredom(20)
    val trigger: Trigger[Customer] = FrustAbove(50) || BoredomAbove(80)
    trigger.eval(c) shouldBe true
  
  test("Always trigger always passes") :
    val c = Customer()
    Always.eval(c) shouldBe true
  
  test("Losses trigger for Martingale strategy works") :
    val c = Customer().withBetStrategy(MartingaleStrat(10,defaultRedBet).copy(lossStreak = 4))
    Losses(3).eval(c) shouldBe true
