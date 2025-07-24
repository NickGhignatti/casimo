package model.entities.customers

import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D

class TestPreviousPosition extends AnyFunSuite:
  private val customer = Customer()
  test("The customer previous position is initially none"):
    assert(customer.previousPosition.isEmpty)

  test(
    "After being moved a customer previous position contains its previous position"
  )
  val movedCustomer = customer.withPosition(Vector2D(1, 1))
  assert(movedCustomer.previousPosition.get == customer.position)
