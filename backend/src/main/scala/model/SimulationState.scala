package model

import model.entities.Wall
import model.entities.customers.Customer
import model.entities.games.Game
import model.entities.spawner.Spawner
import utils.Vector2D

/** Represents the complete state of the casino simulation at a given point in
  * time.
  *
  * SimulationState encapsulates all entities and components that make up the
  * simulation, including customers, games, spawners, walls, and timing
  * information. It serves as the central data structure that is passed between
  * simulation updates, maintaining immutability for predictable state
  * transitions.
  *
  * The state includes all dynamic entities (customers, games) as well as static
  * environment elements (walls) that define the simulation's physical
  * boundaries and collision detection.
  *
  * @param customers
  *   sequence of all customer entities currently in the simulation
  * @param games
  *   list of all game entities available for interaction
  * @param spawner
  *   optional spawner entity for creating new customers
  * @param walls
  *   list of wall entities that define physical boundaries
  * @param ticker
  *   timing coordinator for managing simulation updates and scheduling
  */
case class SimulationState(
    customers: Seq[Customer],
    games: List[Game],
    spawner: Option[Spawner],
    walls: List[Wall],
    ticker: Ticker = Ticker(60.0),
    frameRate: Double = 60.0
)

/** Factory and utility object for creating SimulationState instances.
  */
object SimulationState:

  /** Creates an empty simulation state with no entities.
    *
    * Useful as a starting point for building custom simulation configurations
    * or for resetting the simulation to a clean state.
    *
    * @return
    *   empty SimulationState with default ticker at 60 FPS
    */
  def empty(): SimulationState =
    SimulationState(Seq.empty, List.empty, None, List.empty)

  /** Creates a simulation state with a rectangular boundary defined by walls.
    *
    * Constructs a basic casino floor layout with walls forming a rectangular
    * enclosure. The walls have a standard width of 5.0 units and are positioned
    * to create a complete boundary around the specified area.
    *
    * The wall configuration creates:
    *   - Top wall: full width across the top edge
    *   - Left wall: full height minus wall width on the left edge
    *   - Right wall: full height minus wall width on the right edge
    *   - Bottom wall: width minus 2×wall width at the bottom (accounting for
    *     side walls)
    *
    * @param x
    *   the x-coordinate of the top-left corner
    * @param y
    *   the y-coordinate of the top-left corner
    * @param length
    *   the total width of the enclosed area
    * @param height
    *   the total height of the enclosed area
    * @return
    *   SimulationState with boundary walls and default settings
    */
  def base(
      x: Double,
      y: Double,
      length: Double,
      height: Double
  ): SimulationState =
    val width = 5.0

    val topWall = Wall(Vector2D(x, y), length, width)
    val leftWall = Wall(Vector2D(x, y + width), width, height - width)
    val rightWall =
      Wall(
        Vector2D(x + length - width, y + width),
        width,
        height - width
      )
    val bottomWall =
      Wall(Vector2D(x + width, y + height - width), length - 2 * width, width)

    SimulationState
      .builder()
      .withWalls(List(topWall, leftWall, rightWall, bottomWall))
      .build()

  /** Builder class for constructing SimulationState instances using a fluent
    * API.
    *
    * Provides a convenient way to incrementally build complex simulation states
    * by adding entities one at a time or in groups. The builder pattern ensures
    * that all required components can be configured before creating the final
    * immutable state.
    *
    * @param customers
    *   current collection of customer entities
    * @param games
    *   current collection of game entities
    * @param spawner
    *   optional spawner entity
    * @param walls
    *   current collection of wall entities
    */
  case class Builder(
      customers: Seq[Customer],
      games: List[Game],
      spawner: Option[Spawner],
      walls: List[Wall]
  ):

    /** Sets the complete collection of customers for the simulation.
      *
      * @param customers
      *   the customer entities to include
      * @return
      *   updated builder with the specified customers
      */
    def withCustomers(customers: Seq[Customer]): Builder =
      this.copy(customers = customers)

    /** Adds a single customer to the existing collection.
      *
      * @param customer
      *   the customer entity to add
      * @return
      *   updated builder with the additional customer
      */
    def addCustomer(customer: Customer): Builder =
      this.copy(customers = this.customers :+ customer)

    /** Sets the complete collection of games for the simulation.
      *
      * @param games
      *   the game entities to include
      * @return
      *   updated builder with the specified games
      */
    def withGames(games: List[Game]): Builder =
      this.copy(games = games)

    /** Adds a single game to the existing collection.
      *
      * Games are prepended to the list for efficient insertion.
      *
      * @param game
      *   the game entity to add
      * @return
      *   updated builder with the additional game
      */
    def addGame(game: Game): Builder =
      this.copy(games = game :: this.games)

    /** Sets the spawner for the simulation.
      *
      * @param spawner
      *   the spawner entity to use for customer creation
      * @return
      *   updated builder with the specified spawner
      */
    def withSpawner(spawner: Spawner): Builder =
      this.copy(spawner = Some(spawner))

    /** Removes the spawner from the simulation configuration.
      *
      * @return
      *   updated builder with no spawner
      */
    def withoutSpawner(): Builder =
      this.copy(spawner = None)

    /** Sets the complete collection of walls for the simulation.
      *
      * @param walls
      *   the wall entities to include for boundary definition
      * @return
      *   updated builder with the specified walls
      */
    def withWalls(walls: List[Wall]): Builder =
      this.copy(walls = walls)

    /** Adds a single wall to the existing collection.
      *
      * Walls are prepended to the list for efficient insertion.
      *
      * @param wall
      *   the wall entity to add
      * @return
      *   updated builder with the additional wall
      */
    def addWall(wall: Wall): Builder =
      this.copy(walls = wall :: this.walls)

    /** Constructs the final SimulationState from the current builder
      * configuration.
      *
      * @return
      *   immutable SimulationState with default ticker settings
      */
    def build(): SimulationState =
      SimulationState(customers, games, spawner, walls)

  /** Creates a new empty builder for constructing SimulationState instances.
    *
    * @return
    *   new Builder with empty collections for all entity types
    */
  def builder(): Builder = Builder(Seq.empty, List.empty, None, List.empty)

/** Extension methods for SimulationState providing convenient state
  * modification operations.
  *
  * These methods offer a functional approach to state updates, creating new
  * immutable instances rather than modifying existing state. They provide
  * convenient alternatives to using the copy method directly.
  */
extension (state: SimulationState)

  /** Creates a new SimulationState with an additional customer.
    *
    * @param customer
    *   the customer entity to add
    * @return
    *   new SimulationState including the specified customer
    */
  def addCustomer(customer: Customer): SimulationState =
    state.copy(customers = state.customers :+ customer)

  /** Creates a new SimulationState with an additional game.
    *
    * Games are prepended to the list for efficient insertion.
    *
    * @param game
    *   the game entity to add
    * @return
    *   new SimulationState including the specified game
    */
  def addGame(game: Game): SimulationState =
    state.copy(games = game :: state.games)

  /** Creates a new SimulationState with an updated spawner.
    *
    * @param spawner
    *   the spawner entity to set
    * @return
    *   new SimulationState with the specified spawner
    */
  def setSpawner(spawner: Spawner): SimulationState =
    state.copy(spawner = Some(spawner))

  /** Creates a new SimulationState with an additional wall.
    *
    * Walls are prepended to the list for efficient insertion.
    *
    * @param wall
    *   the wall entity to add
    * @return
    *   new SimulationState including the specified wall
    */
  def addWall(wall: Wall): SimulationState =
    state.copy(walls = wall :: state.walls)

  /** Creates a new SimulationState with an updated framerate.
    *
    * Ticker is updated according to framerate
    *
    * @param frameRate
    *   the new framerate
    * @return
    *   new SimulationState with the new framerate
    */
  def updateFrameRate(frameRate: Double): SimulationState =
    state.copy(frameRate = frameRate, ticker = Ticker(frameRate))
