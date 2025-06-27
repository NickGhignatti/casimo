package model.entities.customers

import utils.Vector2D

final class CustomerID

case class Customer(id: CustomerID, pos: Vector2D)

object Customer:
  def apply(pos: Vector2D): Customer =
    Customer(CustomerID(), pos)
