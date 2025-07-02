package model.customers

import model.SimulationState
import model.customers.Boid.alignment
import model.customers.GameLikeness.gamesLikeness
import model.entities.games.Game
import utils.Vector2D

trait Customer:
  type C <: Customer
  def position: Vector2D
  def velocity: Vector2D
  def update(simulationState: SimulationState[C]): C

case class ContextImpl(
    boid: Boid,
    boids: List[Boid],
    temporaryVelocity: Vector2D = Vector2D(0, 0),
    games: Seq[Game],
    favouriteGames: Seq[Game]
) extends Boid.Context[ContextImpl]
    with GameLikeness.Context[ContextImpl]:

  override def boidUpdated(boid: Boid): ContextImpl = this.copy(boid = boid)

  override def updatedTemporaryVelocity(newVelocity: Vector2D): ContextImpl =
    this.copy(temporaryVelocity = newVelocity)

  override def updatedFavouriteGames(games: Seq[Game]): ContextImpl =
    this.copy(favouriteGames = games)

case class BoidCustomer(boid: Boid) extends Customer:
  type C = BoidCustomer

  def position: Vector2D = boid.position

  def velocity: Vector2D = boid.velocity

  def update(simulationState: SimulationState[BoidCustomer]): BoidCustomer =
    val context = ContextImpl(
      boid = boid,
      boids = simulationState.customers.map(_.boid),
      games = simulationState.games,
      favouriteGames = Seq()
    )
    BoidCustomer(
      (gamesLikeness[ContextImpl] andThen alignment)(context).boid
    )
