package model.entities.customers

import scala.util.Random

final class CustomerID

case class Customer(id: CustomerID, x: Double, y: Double)

object Customer:
  def apply(x: Double, y: Double): Customer =
    Customer(CustomerID(), x, y)

type CustomerList = List[Customer]
