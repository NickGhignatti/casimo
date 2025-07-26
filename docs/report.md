# Casimo Documentation

## Adopted Development Process

### Methodology
A **SCRUM**-inspired development process was adopted, as suggested, in order to ensure an agile and iterative management of the project activities.

Throughout the entire project lifecycle, the quality of the process improved in parallel with the growth of the development team,
which successfully tackled the project challenges, gradually strengthening its synergy and cohesion.

### Roles
- **Product Owner**: Luca Patrignani,  responsible for monitoring the project’s progress, ensuring alignment with business objectives, coordinating the development team, and also serving as a developer.
- **Team member**: Nicolò Ghignatti, member of the dev team which is responsible for the project's progress.
- **Stakeholder**: Marco Galeri, project sponsor and responsible for the product’s quality and usability. He also serves as a developer.

### YouTrack
To track all development activities, we used YouTrack as our project management tool:
- **Task creation** → Each new feature, bug, or refactoring activity was modeled as an issue, with a detailed description and time estimate.
- **Backlog** → We maintained a well-organized and up-to-date backlog: during Sprint Planning, we selected the most urgent tasks and moved them to the next sprint, while lower-priority requests were deferred to future sprints.
- **Timesheet** → We logged the time spent on each task, allowing for more accurate estimates of the resources needed for future activities.

### Task division
The development process was divided into three main themes:
- Games logic (assigned to Ghignatti): responsible for implementing the core game logic.
- Customer movement behaviour (assigned to Patrignani): responsible for implementing the customer movement.
- Customer in-game behaviour (assigned to Galeri): responsible for implementing the customer in-game behaviour.

### Planned meetings/interactions
Each week the team holds a sprint start meeting in which the team establishes the tasks to be completed in the next sprint and an effort estimation.
Then each task is assigned to a team member according to both the overall effort of each member and the theme of the task.
When a blocker is encountered during the development of a task, the team holds a meeting to discuss the issue and pair programming methodology is applied.
At the end of the week a retrospective meeting is held to review the progress of the tasks and to discuss any issues that may have arisen during the week.

### Choice of test/build/continuous integration tools
The team has chosen to use GitHub Actions for continuous integration and deployment. In particular a pipeline has been set up to build and run tests of the project.
If the build is successful the application is deployed to the GitHub Pages of the repository.
The documentation is also automatically generated and deployed to the GitHub Pages of the repository.

### Test
To ensure the quality and correctness of the implemented features, the Test Driven Development (TDD) paradigm was adopted.
This approach allows for timely identification and correction of potential bugs at the level of individual components during the development phases, ensuring a continuous feedback cycle.
The TDD development process consists of three main steps:
- Red Phase (testing): a test is written to describe the expected behavior of a component or feature. Since the implementation is not yet in place, the test initially fails.
- Green Phase (implementation): the component or feature is then implemented to ensure that the previously written test passes successfully.
  -Refactor Phase: after the test passes, the code is refactored to improve its quality and readability, ensuring that the test continues to pass.
## Requirement Specification
### Business requirements
The application is intended to be used by the manager of a [casino](https://en.wikipedia.org/wiki/Casino) who wants to simulate the behaviour of customers inside a given configuration of the casino in order to predict the revenue of the facility.
The manager can configure the spacial organization of the casino (such walls and games) and the behaviour of both games and customers.

### Domain model
- **Customer**: who enters the casino and plays games.
- **Game**: a game that can be played by customers, such as roulette, blackjack and slot machine.
- **Door**: a door that allows customers to enter the casino. It is where the customers enter the casino. It will be called **Spawner** from now on
```mermaid
classDiagram
class Customer {
  +bankroll: Double
  +position: Vector2D
  +direction: Vector2D
}
GameType <|-- Blackjack
GameType <|-- Roulette
GameType <|-- SlotMachine

class Door {
  +position: Vector2D
}

class Obstacle {
    +position: Vector2D
    +width: Double
    +height: Double
}

Door --> Customer : allows entry
Customer --> Game : plays
Customer  --> Customer : moves influenced by other
Customer --> GameType : likes
Game --> GameType : is a
Customer --> Game : moves towards its favourite
Customer --> Obstacle : avoids
Game --|> Obstacle
Wall --|> Obstacle
```
### Functional requirements

#### User requirements
##### Customers
The customers move around the casino according to a [boid](https://en.wikipedia.org/wiki/Boids)-like model. This modeling is taken by the first assigment of PCD course, which is available at [this repo](https://github.com/pcd-2024-2025/assignment-01). Customers are modeled by a `position` and a `velocity` and three rules are applied to them:
- **Separation**: Customers try to maintain a minimum distance from each other
```
function calculate_separation(boid, nearby_boids){
  force = (0, 0)
  for each other_boid in nearby_boids {
    if distance(boid.position, other_boid.position) < AVOID_RADIUS
      force += normalize(boid.position - other_boid.position)
  }
  return force
}
```
- **Alignment**: Customers try to align their velocity with the average velocity of their neighbors
```
function calculate_alignment(boid, nearby_boids){
  average_velocity = (0, 0)
  if size(nearby_boids) > 0 {
    for each other_boid in nearby_boids 
      average_velocity += other_boid.velocity
    average_velocity /= size(nearby_boids)
    return normalize(average_velocity - boid.velocity)
  } else {
    return (0, 0)
  }
}
```
- **Cohesion**: Customers try to move towards the average position of their neighbors
```
function calculate_cohesion(boid, nearby_boids){
  center_of_mass = (0, 0)
  if size(nearby_boids) > 0 {
    for each other_boid in nearby_boids
      center_of_mass += other_boid.position
    center_of_mass /= size(nearby_boids)
    return normalize(center_of_mass - boid.position) 
  } else {
	return (0, 0)
  }
}
```
- **Games attraction**: Customers are attracted by their favourite game, in particular the customer looks around for the nearest game of its favourite type and moves towards it. If no game of its liking is found, this behaviour won't affect its movements and the frustration index of the customer will increase of `INCREASE_FRUSTRATION`. When a customer stands up from a game, it picks randomly a new favourite game, excluding its previous favourite game.

- **Collisions with walls and games**: Customers can't collide with obstacles, which are games and walls. When the resulting velocity would make the customer collide with obstacles, its velocity is set to zero and the movement canceled.

- **Random movement**: Customers change their directions randomly, as they are touring the casino.

Each customer is affected only by the boids within a certain distance, defined by the `PERCEPTION_RADIUS`. If a boid is outside this radius, it is not considered in the calculations.
Each customer updates its position and velocity according to the following algorithm:
```
nearby_boids = collect_nearby_boids(b, boids)

separation = calculate_separation(b, nearby_boids)
alignment = calculate_alignment(b, nearby_boids)
cohesion = calculate_cohesion(b, nearby_boids)
game_attraction = calculate_game_attraction(b, games)

/* Combine forces and update velocity */
b.velocity += SEPARATION_WEIGHT * separation
b.velocity += ALIGNMENT_WEIGHT * alignment
b.velocity += COHESION_WEIGHT * cohesion
b.velocity += GAME_ATTRACTION_WEIGHT * game_attraction
b.velocity += RANDOM_DIRECTION_WEIGHT * random_direction()

/* Limit speed to MAX_SPEED */
if magnitude(b.velocity) > MAX_SPEED
b.velocity = normalize(b.velocity) * MAX_SPEED

if b.canSee(b.position + b.velocity) {
    /* Update position */
    b.position += b.velocity
} else {
    b.direction = (0, 0)
}
    
```
Other parameters that influence the boids behavior are:
- `MAX_SPEED`: maximum speed limit for boids
- `PERCEPTION_RADIUS`: distance within which a boid perceives others
- `AVOID_RADIUS`: minimum distance to avoid collisions
- `SEPARATION_WEIGHT`: weight for separation force
- `ALIGNMENT_WEIGHT`: weight for alignment force
- `COHESION_WEIGHT`: weight for cohesion force
- `GAME_ATTRACTION_WEIGHT`: weight for game attraction force
- `RANDOM_DIRECTION_WEIGHT`: weight for the random movements.

All of these parameters can be configured by the user in order to simulate different scenarios.
When a customer is close to a game of its liking, that is the distance between the customer's and game's position is less than `SITTING_RADIUS`, the player sits and plays the game. While a customer is playing it does not move.

##### Games
The Games module manages the placement, configuration, and behavior of the games within the casino.
The following functional requirements are designed to ensure a realistic simulation of the interaction between games and customers.

The user must be able to insert and place games on the casino map via the graphical interface. Each game occupies one or more tiles and cannot overlap with walls or other games.

The system must support various types of games, including (but not limited to):
- Slot Machine
- Roulette
- Black Jack

Each type can have different rules that influence the simulation.

The system must allow simulated customers to interact with games based on their strategies:
- Game selection based on win probability, payout, physical proximity, etc.
- In-game decisions (e.g., how much to bet, when to stop)

Each game must maintain a consistent internal state, for example:
- List of active players
- Number of rounds played
- Aggregate statistics (win percentages, average bet amounts)

#### System requirements

- The system must be able to display the **casino map**, including walls and games
- The system must be able to display **information about simulation entities**, such as customers and games
- The system must support the **creation, modification, and deletion** of:
  - Walls
  - Games (e.g., Slot Machine, Roulette)
  - Simulation parameters

- The system must be able to **execute the simulation**, including:
  - Simulating customer movement across the casino
  - Managing game selection strategies and in-game behaviors
  - Ensuring no physical overlap between customers and walls or other entities

- The system must be able to **log simulation data in real time**, including:
  - Customers bankrolls
  - Games bankrolls

- The system must allow **parameter tuning via GUI**, including:
  - Customer behavior variables
  - Time-based flow curves

- The system must support **visual monitoring of internal states**, including:
  - Current bankroll

- The system must be able to **validate the simulation setup**, ensuring consistency and completeness before execution


### Non-functional requirements
- Performance:
  - The system should simulate at least 50 concurrent customers with <100ms average update latency
  - Real-time logging must not cause performance degradation
- Scalability:
  - The simulation engine should support scaling up to 500+ entities (customers + games) with graceful degradation
- Portability:
  - The application should be cross-platform or easily portable across supported OSs
- Maintainability:
  - Modular architecture with clear separation between GUI, simulation logic, and data layers
- Usability:
  - GUI must allow intuitive drag-and-drop for map and game design
  - All configurable parameters should be accessible via forms or sliders
- Reliability:
  - The simulation must recover gracefully from internal errors without crashing
  - Consistent logging should allow for post-mortem debugging

### Implementation requirements
- Scala 3.3.5
- Scalatest 3.2.19
- Scalacheck 3.2.19
- ScalaJs 2.8.0
- Laminar 17.0.0
- SBT as automation tool 1.10.11
- Scaladoc
- Scalafmt 3.7.15
- Scalafix
- Codecov

### Optional requirements
- Monitoring of each customer's internal state (e.g., current bankroll, mood level) via GUI
- A DSL to define custom (including non-Gaussian) curves related to:
  - Customer flow over time
  - Initial bankroll distribution of players
  - Other dynamic simulation parameters
- A GUI for monitoring real-time and aggregated data regarding the casino's performance

## Architectural Design
### Overall architecture
The application rely on a MVU (Model-View-Update) architecture, which is a purely functional architecture.
Core concepts of this architecture are:
- **Model**: Represents the state of the application, a pure, immutable data structure where all state changes produce new instances.
- **View**: A function that takes the model and produces a view, which is a description of what the user interface should look like.
- **Update**: A function that takes the current model and an event (or action) and produces a new model, representing the new state of the application.

![MVU Architecture Diagram](resources/mvu_architecture_schema.png)

Cornerstone of this architecture is the unidirectional data flow, where at the center of the architecture is the update function, which process messages
sent by the view and produces a new model. This kind of update function is what allow this architecture to simulate the loop of a traditional simulation application,
maintaining the purely functional nature of the application.



### Description of architectural patterns used

### Any distributed system components

### Crucial technological choices for architecture
#### Scala.js + Laminar: Reactive Frontend & Continuous Deployment

The application rely on a browser-native UI built using **Scala.js** with **Laminar**. This choice was driven by the ability to have a continuous deployment via GitHub Pages while also adopting well to the MVU architecture.

Laminar **fine-grained reactivity** ensures that view components update automatically when the model changes, perfectly complementing the MVU dataflow `Model → View → Update` cycle. Rather than manually propagating new state, Laminar delivers updates precisely where they’re needed.
Also, DOM changes are direct without the need of virtual-DOM abstraction, avoiding performance bottlenecks and potential stale-state problem common with other web framework.

- **Continuous Deployment**  
  Since Scala.js outputs JavaScript and HTML, our pipeline can **automatically build, test, and deploy** the application on every change. This continuous deployment setup ensures the latest version is always live without manual intervention needed.
### Diagrams

## Detailed Design
### Relevant design choices
An important design choice in our application is the use of a MVU architecture, which dictates how the application is structured and how data flows through it.
Cornerstone component is the update function, which has been designed in a way to simulate a loop in order to allow a better management of the simulation state.
![MVU Update Function Diagram](resources/update_loop.png)

#### Update
The Update system represents the core simulation engine responsible for managing the state transitions and event processing
in a casino simulation environment. Built using functional programming principles and tail recursion optimization,
the system processes discrete simulation events in a deterministic sequence, ensuring consistent state management across
all simulation components including customers, games, walls, and spawners.

The Update system follows an event-driven architecture combined with the State pattern, where simulation state transitions
are triggered by specific events processed through a central update loop. The design emphasizes immutability and functional
composition, using tail recursion to ensure stack safety during extended simulation runs.

```mermaid
stateDiagram-v2
    [*] --> Initialized

    Initialized --> Spawning : SimulationTick (with spawner)
    Initialized --> CustomerUpdate : SimulationTick (no spawner)

    Spawning --> CustomerUpdate : Spawn Complete

    CustomerUpdate --> GameUpdate : Customers Positioned

    GameUpdate --> BankrollUpdate : Games Resolved

    BankrollUpdate --> CustomerStateUpdate : Finances Updated

    CustomerStateUpdate --> WaitingForTick : Cycle Complete

    WaitingForTick --> Spawning : Next SimulationTick (with spawner)
    WaitingForTick --> CustomerUpdate : Next SimulationTick (no spawner)

    WaitingForTick --> ConfigurationChange : Configuration Events
    ConfigurationChange --> WaitingForTick : Configuration Applied

    WaitingForTick --> [*] : ResetSimulation

    note right of Spawning
        Spawner creates new customers
        based on configured strategy
    end note

    note right of GameUpdate
        GameResolver processes customer
        interactions with games
    end note
```

```mermaid
flowchart TD
    A[Event Received] --> B{Event Type?}

    B -->|SimulationTick| C{Spawner Exists?}
    C -->|Yes| D[Execute Spawn Logic]
    C -->|No| E[Skip Spawn]
    D --> F[Trigger UpdateCustomersPosition]
    E --> F

    B -->|UpdateCustomersPosition| G[Apply Customer Manager]
    G --> H[Trigger UpdateGames]

    B -->|UpdateGames| I[Resolve Game Interactions]
    I --> J[Update Game States]
    J --> K[Trigger UpdateSimulationBankrolls]

    B -->|UpdateSimulationBankrolls| L[Process Financial Updates]
    L --> M[Trigger UpdateCustomersState]

    B -->|UpdateCustomersState| N[Finalize Customer State]
    N --> O[Return Updated State]

    B -->|AddCustomers| P[Create/Update Spawner]
    P --> Q[Return State with Spawner]

    B -->|UpdateWalls| R[Replace Wall Configuration]
    R --> S[Return State with New Walls]

    B -->|UpdateGamesList| T[Replace Game Configuration]
    T --> U[Return State with New Games]

    B -->|ResetSimulation| V[Create Empty State]
    V --> W[Return Reset State]
```
The system processes all state changes through discrete events, providing clear separation of concerns and making the simulation deterministic and testable.
Each event type triggers specific state transformation logic.
The `SimulationState` serves as the context, while different events represent state transition triggers. The `Update` class
acts as the state manager, coordinating transitions between different simulation phases.


#### Customer Composition

We choose to implement the `Customer` behavior using **F‑bounded polymorphic traits**. This choice brings some great feature enabling a **modular** and **extensible** design.

```scala 3
case class Customer(
                           id: String,
                           bankroll: Double,
                           customerState: CustState = Idle
                   ) extends Entity,
        Bankroll[Customer],
        CustomerState[Customer]:

  protected def updatedBankroll(newRoll: Double): Customer =
    this.copy(bankroll = newRoll)

  protected def changedState(newState: CustState): Customer =
    this.copy(customerState = newState)
```
```scala 3
trait Bankroll[T <: Bankroll[T]]:
  val bankroll: Double
  require(
    bankroll >= 0,
    s"Bankroll amount must be positive, instead is $bankroll"
  )

  def updateBankroll(netValue: Double): T =
    val newBankroll = bankroll + netValue
    require(
      newBankroll >= 0,
      s"Bankroll amount must be positive, instead is $newBankroll"
    )
    updatedBankroll(newBankroll)

  protected def updatedBankroll(newBankroll: Double): T
```

The key strength of this design are:

- **Strong type safety**  
  F‑bounded traits restrict generic parameters to subtypes of the trait itself, preventing accidental type error at compile time.

- **Precise APIs and seamless mvu updates**  
  By encoding the concrete subtype via `C <: Trait[C]`, trait methods can return `C` directly, enabling `.copy(...)` function in the Customer producing a new instance in a clean and optimize way. This avoids casts or losing type specificity in method returns making updating state easier.

```scala 3
val newCustomer = Customer(id = "myCustomer", bankroll = 50.0)
val updatedCustomer = newCustomer.updateBankroll(-20.0)
// ad-hoc method for update that checks that bankroll don't go below zero
```
- **Modular and extensible architecture**  
  Each behavior (e.g., bankroll, boredom, status) is isolated within its own trait. This allows introducing new behaviour without altering existing implementations by just defining the trait and mix it in.
```scala 3
case class Customer(
                           id: String,
                           bankroll: Double,
                           customerState: CustState = Idle,
                           boredom: Double
                   ) extends Entity,
        Bankroll[Customer],
        CustomerState[Customer],
        Boredom[Customer]: // Just adding a new behaviour to the Customer by composition

```
By leveraging these traits composition system, our `Customer` model stays **type safe**, **cohesive**, and easy to evolve, supporting future expansion of behaviors and customer types without compromising the maintainability.

#### Customers spawner

The `Spawner` system manages the generation of entities (customers) in the simulation using configurable spawning strategies.
The design follows these core principles:
- Decoupling : Spawners are unaware of strategy implementation details
- Flexibility : Strategies can be combined and extended
- Time-based : Strategies react to simulation time progression
- Immutability : Strategies are pure functions of time

The responsibilities of this entity are:
- Position management (where entities spawn)
- Time tracking (when entities spawn)
- Delegation to strategy (how many entities spawn)

The `SpawnerStrategy` is the entity designed to return the number of customers, following a specific strategy, to spawn which is designed to use
generic and famous spawning behaviours and permit the user to define custom spawning strategies thanks to a scala DSL.

Kind of generic behaviour the `SpawningStrategy` should provide are:
- `constant` : mechanism to spawn a fixed number of customers every tick
- `gaussian` : mechanism to simulate a gaussian behaviour of the spawner
- `step` : mechanism where the spawn rates change abruptly at specific times

The behaviour of a `SpawningStrategy` can be modelled in an interface like:
```scala 3
trait SpawningStrategy:
  def customersAt(time: Double): Int
```
```mermaid
sequenceDiagram
    participant SimulationLoop
    participant Spawner
    participant SpawningStrategy
    
    SimulationLoop->>Spawner: spawn(state) at time T
    activate Spawner
    
    Spawner->>SpawningStrategy: customersAt(currentTime)
    activate SpawningStrategy
    
    SpawningStrategy-->>Spawner: count: Int
    deactivate SpawningStrategy
    
    Spawner->>Spawner: Generate 'count' customers
    Spawner->>Spawner: Update currentTime += 1
    
    Spawner-->>SimulationLoop: Updated state
    deactivate Spawner
    
    Note right of SpawningStrategy: Strategy implementations:
    alt ConstantStrategy
        SpawningStrategy-->>Spawner: Fixed rate
    else GaussianStrategy
        SpawningStrategy-->>Spawner: Rate based on bell curve
    else StepStrategy
        SpawningStrategy-->>Spawner: High rate if time in [start,end]
    end
```

By designing the creation of these strategies through a builder we can allow to combine strategies or customize them by applying factors.
Or even better, we designed an internal DSL which boost the creativity of the user: it allows to customize the predefined
strategies or to create your own one.

#### Walls

The `Wall` is a foundational element in our casino simulation application, serving as impassable barriers that define physical boundaries.

The `Wall` was designed with these core principles:
- Immutability: `Wall` state changes create new instances
- Reactivity: UI automatically updates when walls change
- Composability: Built from reusable traits
- Interactivity: Intuitive drag-and-drop placement
- Collision Awareness: Prevents invalid placements

Due to the numerous behaviours the `Wall` entity should have design it as a mixin is a great solution, follows the list of
behaviours that the entity has:
- `Positioned` : express that the entity has a position
- `Sized` : which express that the entity has a size
- `CollidableEntity` : which express the fact that the entity can collide with others entity
- `SizeChangingEntity` : which express the resize behaviour of the entity

#### Games
The core abstraction is provided by the `Game` trait, representing a generic gambling station in the Casino.
It’s designed to model concurrency and fairness while allowing flexibility across different game types.
Three concrete implementations — `RouletteGame`, `SlotMachineGame`, and `BlackJackGame` — extend this trait to specialize behavior based on game logic and betting styles.

```mermaid
classDiagram
  class Game {
    <<trait>>
  }
  class SlotMachineGame
  class RouletteGame
  class BlackJackGame

  Game <|-- SlotMachineGame
  Game <|-- RouletteGame
  Game <|-- BlackJackGame

```
To deal with the concurrency on this kind of entity was decided to create some functions which allow to lock/unlock the game.
This functions will alter the `GameState` which represent the current state of the `Game`, with all the customers that are
currently playing that specific game.
Thanks to the implementation of a monad which allow to manage two different states (`Result`), deal with the error in case
of the impossibility to play the game is easy.

Also keeping track of the gains and loss of our game is important, to avoid to overload of task our games a `GameHistory` entity was designed.
The behaviour of this entity is simple, a `GameHistory` is designed to deal with just one `Game` and the communication with it is limited,
it is designed to deal with a `DataManager` which is an entity designed for keeping track of important data in the simulation.

`GameHistory` is an Entity which keep tracks of the game history, is composition is quite simple, is a list of `Gain` which
represent the Tuple composed by the ID of the customer who played the game and the gain in terms of money the game did.

#### Game Strategies
Every game has a `GameStrategy` which is the component where is stored the strategy part of a game, it is responsible for
simulate the real-world game behaviours through some predefined strategies and custom ones

The design follows the Strategy pattern combined with the Builder pattern to create a flexible and extensible architecture for different casino games.
The system supports three main game types: Slot machines, Roulette, and BlackJack, each with customizable betting strategies and conditions.

The system is built around a core trait `GameStrategy` that defines the contract for all gambling strategies.
Each game type implements this strategy through a two-phase construction process: a builder phase for configuration and an instance phase for execution.

A dedicated DSL module provides a more natural and readable way to construct strategies, making the API more user-friendly and expressive.

```mermaid
classDiagram
    class GameStrategy {
        <<trait>>
        +use() BetResult
    }
    
    class SlotStrategyBuilder {
        -betAmount: Option[Double]
        -condition: Option[Function0[Boolean]]
        +bet(amount: Double) SlotStrategyBuilder
        +when(cond: Boolean) SlotStrategyInstance
    }
    
    class SlotStrategyInstance {
        -betAmount: Double
        -condition: Function0[Boolean]
        +use() BetResult
    }
    
    class RouletteStrategyBuilder {
        -betAmount: Option[Double]
        -targets: Option[List[Int]]
        +bet(amount: Double) RouletteStrategyBuilder
        +on(targets: List[Int]) RouletteStrategyBuilder
        +when(cond: Boolean) RouletteStrategyInstance
    }
    
    class RouletteStrategyInstance {
        -betAmount: Double
        -targets: List[Int]
        -condition: Function0[Boolean]
        +use() BetResult
    }
    
    class BlackJackStrategyBuilder {
        -betAmount: Option[Double]
        -minimumVal: Option[Int]
        -condition: Option[Function0[Boolean]]
        +bet(amount: Double) BlackJackStrategyBuilder
        +accept(minimum: Int) BlackJackStrategyBuilder
        +when(cond: Boolean) BlackJackStrategyInstance
    }
    
    class BlackJackStrategyInstance {
        -betAmount: Double
        -minimumValue: Int
        -condition: Function0[Boolean]
        -dealCard(cardsValue: Int, stopValue: Int) Int
        +use() BetResult
    }
    
    GameStrategy <|.. SlotStrategyInstance
    GameStrategy <|.. RouletteStrategyInstance
    GameStrategy <|.. BlackJackStrategyInstance
    
    SlotStrategyBuilder --> SlotStrategyInstance : creates
    RouletteStrategyBuilder --> RouletteStrategyInstance : creates
    BlackJackStrategyBuilder --> BlackJackStrategyInstance : creates
```

The flow from the creation of the strategies to their use is modelled with the following flowchart:

```mermaid
flowchart TD
    A[DSL Entry Point] --> B{Strategy Type Selection}
    
    B -->|Slot| C[SlotStrategyBuilder]
    B -->|Roulette| D[RouletteStrategyBuilder]
    B -->|BlackJack| E[BlackJackStrategyBuilder]
    
    C --> F[Configure Bet Amount]
    D --> G[Configure Bet Amount & Targets]
    E --> H[Configure Bet Amount & Minimum Value]
    
    F --> I[Set Condition]
    G --> J[Set Condition]
    H --> K[Set Condition]
    
    I --> L[SlotStrategyInstance]
    J --> M[RouletteStrategyInstance]
    K --> N[BlackJackStrategyInstance]
    
    L --> O[Execute Strategy]
    M --> O
    N --> O
    
    O --> P{Condition Met?}
    P -->|Yes| Q[Run Game Logic]
    P -->|No| R[Return Failure]
    
    Q --> S{Win?}
    S -->|Yes| T[Return Success with Winnings]
    S -->|No| U[Return Failure with Loss]
```

#### Game resolver
The GameResolver system serves as the central orchestrator for managing interactions between customers and games during each simulation tick. 
Implemented as a singleton object using functional programming principles, it processes all active gaming sessions simultaneously, 
handling bet placement, game execution, and result processing. The system maintains complete separation between game logic 
and customer management while ensuring consistent state updates across all gaming interactions.

The GameResolver follows a functional processing pipeline architecture, where each simulation tick triggers a complete evaluation of all customer-game interactions.

GameResolver acts as a mediator between customers and games, preventing direct coupling and centralizing interaction logic, according to the **Mediator pattern**. 
This allows for complex interaction rules without modifying individual customer or game implementations.

The system integrates with the GameStrategy pattern by processing the results of strategy executions and translating them into game state updates.

```mermaid
graph TD
    subgraph "GameResolver Object"
        A[update method]
        B[games.map]
        C[playGame function]
    end

    subgraph "Customer Filtering"
        D[customers.filter]
        G{CustState.Playing?}
        H[Include Customer]
        I[Skip Customer]
    end

    subgraph "Processing Pipeline"
        E[playingCustomers.foldRight]
    end

    subgraph "Result Handling"
        F[Result Pattern Matching]
        J{Result.Success?}
        K{Inner Result?}
        L[updateHistory 0.0]
        M[updateHistory -lostValue]
        N[updateHistory +winValue]
    end

    A --> B
    B --> C
    C --> D

    D --> G
    G -->|Yes| H
    G -->|No| I

    D --> E
    E --> F

    F --> J
    J -->|Yes| K
    J -->|No| L

    K -->|Success| M
    K -->|Failure| N

    M --> E
    N --> E
    L --> E
```

```mermaid
sequenceDiagram
    participant Sim as Simulation Engine
    participant GR as GameResolver
    participant Game as Game Instance
    participant Cust as Customer
    participant Hist as Game History

    Note over Sim, Hist: Single Simulation Tick Processing

    Sim->>GR: update(customers, games)

    loop For Each Game
        GR->>GR: playGame(game, customers)

        Note over GR: Filter customers playing this game
        GR->>GR: filter customers by game.id

        loop For Each Playing Customer (foldRight)
            GR->>Cust: placeBet()
            Cust-->>GR: bet amount

            GR->>Game: play(bet)
            Game-->>GR: Result[BetResult]

            alt Game Success
                alt Customer Lost (Success)
                    GR->>Hist: updateHistory(customerId, -lostValue)
                else Customer Won (Failure)
                    GR->>Hist: updateHistory(customerId, +winValue)
                end
            else Game Failure
                GR->>Hist: updateHistory(customerId, 0.0)
            end

            Note over GR: Customer processed, continue fold
        end

        Note over GR: Game processing complete
    end

    GR-->>Sim: List[Updated Games]
```

#### ScalaJs view
The view system is built around a central reactive architecture where state changes flow through observable signals, triggering automatic UI updates. 
The design separates concerns between canvas-based visualization, HTML-based controls, and event coordination. 
The system uses immutable state management with reactive variables (Vars) that automatically propagate changes throughout the UI hierarchy

Laminar's reactive system provides automatic observer pattern implementation through Signals and EventBus, eliminating manual event listener management.

Each UI element is encapsulated as a self-contained component with its own state, rendering logic, and event handling.

All user interactions and state changes flow through a central EventBus, providing decoupled communication between components.

```mermaid
graph TD
    subgraph "Main Entry Point"
        A[Main.scala]
        B[DOMContentLoaded Event]
        C[EventBus Creation]
    end
    
    subgraph "Core State Management"
        D[SimulationState Var]
        E[DataManager Var]
        F[Update Var]
        G[Event Processing Loop]
    end
    
    subgraph "UI Components"
        H[CanvasManager]
        I[ButtonBar]
        J[ConfigForm]
        K[Modal]
        L[Sidebar]
        P[Drag & Drop]
    end
    
    subgraph "Reactive Forms"
        Q[Movement Parameters]
        R[Spawning Strategies]
        S[Strategy Selectors]
    end
    
    A --> B
    B --> C
    C --> G
    
    A --> D
    A --> E
    A --> F
    
    D --> H
    D --> I
    D --> J
    D --> K
    
    L --> P
    P --> H
    
    J --> Q
    J --> R
    J --> S
    
    G --> D
    I --> G
```
The component interaction is designed as follows:

```mermaid
flowchart TD
    A[🌐 Browser DOM Ready] --> B[📋 Initialize State Variables]
    
    B --> C[🔄 Create EventBus]
    C --> D[⚡ Setup Event Processing Loop]
    
    D --> E["🔄 SCAN LOOP<br/>eventBus.events.scanLeft(state, updateFunc)"]
    
    E --> F[🎯 Initialize UI Components]
    
    F --> G[🖼️ CanvasManager]
    F --> H[🎛️ ButtonBar]
    F --> I[📝 ConfigForm]
    F --> J[📊 Modal]
    F --> K[📋 Sidebar]
    
    G --> L["🖱️ Mouse Events<br/>(mousedown, mousemove, mouseup)"]
    G --> M["🎨 Canvas Rendering<br/>(customers, walls, games)"]
    
    H --> N["🔘 Button Clicks<br/>(Add, Run, Reset, Save, Load, Data)"]
    
    I --> O["📊 Parameter Changes<br/>(movement config, spawning config)"]
    
    K --> P["🎲 Drag & Drop<br/>(Wall, Slot, Roulette, BlackJack)"]
    
    L --> Q{🎯 Event Type?}
    N --> Q
    O --> Q
    P --> Q
    
    Q -->|Mouse Events| R[🎮 Canvas State Update]
    Q -->|Button Events| S[🎯 Simulation Control]
    Q -->|Config Events| T[⚙️ Parameter Update]
    Q -->|Drag Events| U[🏗️ Entity Creation]
    
    R --> V["🔄 REACTIVE UPDATE<br/>Var.set triggers signal propagation"]
    S --> V
    T --> V
    U --> V
    
    V --> W[🎨 UI Re-render]
    W --> X[♻️ Wait for Next Event]
    X --> E
    
    classDef initialization fill:#e1f5fe,stroke:#0277bd,stroke-width:2px,color:#000
    classDef component fill:#fff3e0,stroke:#f57c00,stroke-width:2px,color:#000
    classDef event fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px,color:#000
    classDef reactive fill:#ffebee,stroke:#c62828,stroke-width:3px,color:#000
    
    class A,B,C,D,F initialization
    class G,H,I,J,K component
    class L,N,O,P,Q,R,S,T,U event
    class E,V,W reactive
```
This kind of design takes advantage from the Laminar reactive system which eliminates the need for manual DOM updates. 
When simulation state changes, the UI automatically reflects those changes through signal propagation, preventing the common 
problem of UI-state desynchronization that plagues many web applications.

## Implementation
### Student contributions
### Nicolò Ghignatti
#### Result
Having to deal with data which can have two states (win or loss for a bet, for example) can be quite annoying so, I've
decided to implement a monad which can do it for us. Basically it is a enum which can have 2 states: a
`Success` or a `Failure`:
```scala
package utils

enum Result[+T, +E]:
  case Success(value: T)
  case Failure(error: E)

  def map[U](f: T => U): Result[U, E] = this match
    case Success(value) => Success(f(value))
    case Failure(error) => Failure(error)

  def flatMap[U, F](f: T => Result[U, F]): Result[U, E | F] = this match
    case Success(value) => f(value)
    case Failure(error) => Failure(error)

  def getOrElse[U >: T](default: U): U = this match
    case Success(value) => value
    case Failure(_)     => default

  def isSuccess: Boolean = this match
    case Success(_) => true
    case Failure(_) => false

  def isFailure: Boolean = !isSuccess
```
This kind of implementation help also in the error handling

#### Games
I've dealt with the game implementation in their totality. The crucial point was dealing with the customers, so useful APIs
have been implemented, allowing the players to join games and play them.
```scala
class Game extends Entity:
  def gameType: GameType
  def lock: Result
  def unlock: Result
  def play: Result
```
An important task was to manage the customers joining a game, allowing the to join if possible, otherwise block them.
This mechanism has been implemented in a `GameState` which manage the join/leave mechanism:
Instead the logic of the games was implemented using an internal DSL, which expose useful stuff to implement strategies
in an easy way:
```scala
// this is an example of how the game strategy DSL was implemented
trait GameStrategy:
  def use(): Result[Double, Double]

object SlotStrategy:
  def apply: SlotStrategyBuilder = SlotStrategyBuilder()

case class SlotStrategyBuilder( betAmount, condition):
    def bet(amount: Double): SlotStrategyBuilder =
      require(amount > 0.0, "Bet amount must be positive")
      this.copy(betAmount = Some(amount))

    def when(cond: => Boolean): SlotStrategyInstance =
      SlotStrategyInstance(betAmount.getOrElse(0.0), () => cond)

case class SlotStrategyInstance(betAmount, condition) extends GameStrategy:
  override def use(): Result[Double, Double] =
    val values =
      for _ <- 1 to 5 yield Random.nextInt(5) + 1
    if condition() && values.distinct.size == 1 then
      Result.Success(betAmount * 10)
    else Result.Failure(betAmount)
```
Allowing an easy creation like the following:
```scala 3
val bankroll = 10.0
use(SlotStrategy) bet 5.0 when (bankRoll > 0.0)
```
In order to keep track of the customer playing a game I decided to subordinate the functions to a state, which is called `GameState`

```scala 3
case class GameState(
    currentPlayers: Int,
    maxAllowedPlayers: Int,
    playersId: List[String]
):
  def isFull: Boolean = currentPlayers == maxAllowedPlayers

  def addPlayer(id: String): Result[GameState, GameState] =
    if (currentPlayers < maxAllowedPlayers)
      Result.Success(
        GameState(currentPlayers + 1, maxAllowedPlayers, playersId :+ id)
      )
    else
      Result.Failure(this)

  def removePlayer(id: String): Result[GameState, GameState] =
    if (currentPlayers > 0)
      Result.Success(
        GameState(
          currentPlayers - 1,
          maxAllowedPlayers,
          playersId.filterNot(s => s != id)
        )
      )
    else
      Result.Failure(this)
```
#### Game Resolver

To deal with the interactions between customers and games I decided to use the **mediator pattern** which use a third entity
in order to manage the communication between games and customers

```scala 3
object GameResolver:
  private def playGame(game: Game, customers: List[Customer]): Game =
    val playingCustomers = customers.filter(c =>
      c.customerState match
        case CustState.Playing(customerGame) => game.id == customerGame.id
        case CustState.Idle                  => false
    )
    playingCustomers.foldRight(game)((c, g) =>
      g.play(c.placeBet()) match
        case Result.Success(value) =>
          value match
            case Result.Success(lostValue) => g.updateHistory(c.id, -lostValue)
            case Result.Failure(winValue) =>
              game.updateHistory(c.id, winValue)
        case Result.Failure(error) => game.updateHistory(c.id, 0.0)
    )

  def update(customers: List[Customer], games: List[Game]): List[Game] =
    games.map(g => playGame(g, customers))
```

Where updates the games according to the result of the last round of play by adding a new item in the history of the game

#### Spawner (Door)

The `Spawner` is the entity designed to spawn the customers according to a logic, every tick is called the spawn method which 
decide to spawn a certain number of customers, according to the spawn strategy:

```scala 3
def spawn(state: SimulationState): SimulationState =
  if currentTime % ticksToSpawn == 0 then
    state.copy(
      customers = state.customers ++ Seq.fill(
        strategy.customersAt(currentTime / ticksToSpawn)
      )(
        Customer(
          s"customer-${Random.nextInt()}",
          this.position.around(5.0),
          Vector2D(Random.between(0, 5), Random.between(0, 5)),
          bankroll = Random.between(30, 5000),
          favouriteGames = Seq(
            Random
              .shuffle(
                Seq(
                  model.entities.games.Roulette,
                  model.entities.games.Blackjack,
                  model.entities.games.SlotMachine
                )
              )
              .head
          )
        )
      ),
      spawner = Some(this.copy(currentTime = currentTime + 1))
    )
  else state.copy(spawner = Some(this.copy(currentTime = currentTime + 1)))
```
To avoid a non-realistic and chaotic spawn I decided to spawn customers every `ticksToSpawn` ticks which is an integer 
representing the number of ticks necessary for a spawn round

#### Spawning Strategy
The `SpawningStrategy` entity is quite simple, it takes as input the time passed in the simulation, and it outputs the number 
of customers that should be spawned according the selected strategy:
```scala 3
trait SpawningStrategy:
  def customersAt(time: Double): Int
```
An example is the famous Gaussian curve to model a bell spawning strategy:
```scala 3
case class GaussianStrategy(
    peak: Double,
    mean: Double,
    stdDev: Double,
    base: Int = 0
) extends SpawningStrategy:
  override def customersAt(time: Double): Int =
    if (stdDev <= 0) {
      if (math.abs(time - mean) < 1e-9) (base + peak).toInt else base
    } else {
      val exponent = -0.5 * math.pow((time - mean) / stdDev, 2)
      val value = base + peak * math.exp(exponent)
      math.round(value).toInt.max(0)
    }
```
In order to allow to other to define custom spawning strategy, I decided to design a DSL by allowing to create a custom strategy, possible to create through the builder:
```scala 3
class SpawningStrategyBuilder private (private val strategy: SpawningStrategy):
    def gaussian(
          peak: Double,
          mean: Double,
          stdDev: Double
    ): SpawningStrategyBuilder =
      new SpawningStrategyBuilder(GaussianStrategy(peak, mean, stdDev))
    
    def custom(f: Double => Int): SpawningStrategyBuilder =
      new SpawningStrategyBuilder((time: Double) => f(time))
```
Or customizing predefined/custom strategy while building the strategy:
```scala 3
class SpawningStrategyBuilder private (private val strategy: SpawningStrategy):
  // DSL operations
  def offset(amount: Int): SpawningStrategyBuilder =
    require(amount >= 0)
    val newStrategy = new SpawningStrategy:
      override def customersAt(time: Double): Int =
        strategy.customersAt(time) + amount
    new SpawningStrategyBuilder(newStrategy)

  def scale(factor: Double): SpawningStrategyBuilder =
    require(factor >= 0, "scale factor should be >= 0")
    val newStrategy = new SpawningStrategy:
      override def customersAt(time: Double): Int =
        math.round(strategy.customersAt(time) * factor).toInt
    new SpawningStrategyBuilder(newStrategy)

  def clamp(min: Int, max: Int): SpawningStrategyBuilder =
    require(min >= 0, "minimum value should be >= 0")
    require(min <= max, "maximum value should be greater than minimum")
    val newStrategy = new SpawningStrategy:
      override def customersAt(time: Double): Int =
        val value = strategy.customersAt(time)
        value.max(min).min(max)
    new SpawningStrategyBuilder(newStrategy)
```
In order to make easier strategies to be created I simplified the offset and scale operations through operators:
```scala 3
object SpawningStrategyBuilder:
  implicit class StrategyWrapper(strategy: SpawningStrategy):
    def +(offset: Int): SpawningStrategy =
      (time: Double) => strategy.customersAt(time) + offset

    def *(factor: Double): SpawningStrategy =
      (time: Double) => math.round(strategy.customersAt(time) * factor).toInt
```

#### Walls
To introduce this kind of entities, which has a position, a size and the possibility to be resized, I decided to structure it
like a mixin, first I decided to implement the following traits:

```scala 3
trait Positioned:
  val position: Vector2D

trait Sized:
  val width: Double
  val height: Double

trait Collidable extends Sized with Positioned:
  // several operations  

trait CollidableEntity extends Collidable with Entity

trait SizeChangingEntity extends Sized:
  def withWidth(newWidth: Double): this.type
  def withHeight(newHeight: Double): this.type
  def withSize(newWidth: Double, newHeight: Double): this.type
```

Once introduced these traits, which resulted useful for other entities which has common behaviours with the walls, like customers,
write the mixin is quite simple:
```scala 3
case class Wall(
    id: String,
    position: Vector2D,
    width: Double,
    height: Double
) extends Positioned,
      Sized,
      Collidable,
      SizeChangingEntity,
      Entity
```

#### Update
The update implementation try to simulate what a simulation loop looks like in my head. The tail recursive structure is 
ideated in order to separate all the different phases in order to separate all the various moments in the simulation.

```scala 3
@tailrec
final def update(state: SimulationState, event: Event): SimulationState =
  event match
    case SimulationTick => 
      update(newState, UpdateCustomersPosition)

    case UpdateCustomersPosition => 
      update(state | customerManager, UpdateGames)

    case UpdateGames =>
      update(state.copy(games = updatedGames), UpdateSimulationBankrolls)

    case UpdateSimulationBankrolls =>
      update(state.copy(customers = updatedBankroll), UpdateCustomersState)

    case UpdateCustomersState =>
      state.copy(customers = updatedCustomerState)
```

### Patrignani Luca
#### Customer movements
The customer movements are modeled according to the previously presented architecture: a trait `Movable` is defined as such
```scala 3
trait Movable[T <: Movable[T]]:
  val direction: Vector2D
  val position: Vector2D

  def updatedPosition(newPosition: Vector2D): T

  def updatedDirection(newDirection: Vector2D): T
```
and all movement managers depend only on this trait, not on the concrete implementation of the `Customer`.
Two movement managers have been implemented:
- **Boid-like movement**: this implements the boid-like movement described in the user requirements section. It is obtained by combining three other managers, each one implementing one of the three boid-like rules: **Separation**, **Alignment** and **Cohesion**.
Since the boids logic for a single customer need not only its position and velocity, but also information about the other customers, the `Boids.State` case class is defined, which contains all the information needed to compute the movement of the boid. Then an adapter manager is defined to adapt the `SimulationState` to the necessary `Boids.State` and vice versa. This allows to keep the movement logic independently from the simulation state. The following class diagram describes the dependencies taking as example the `AlignmentManager`, the `SeparationManager` and the `CohesionManager` are implemented in the same way:
```mermaid
classDiagram
class Movable {
  +direction: Vector2D
  +position: Vector2D
}
Customer --|> Movable
Boid.State --o Movable : other boids
Boid.State --o Movable : the single boid
AlignmentManager --> Boid.State
BoidAdapter --> SimulationState : adapts
BoidAdapter --> Boid.State : adapts
SimulationState --o Customer
```
- **Game attraction movement**: this manager implements the game attraction movement, which is the movement of the customer towards its favourite game. The customer's favourite game is needed, so a new trait `Player` is defined extending `Movable`. Similarly to the boids movement managers, the `GamesAttractivenessManager` depends on a `Context` case class, which contains all the information needed to compute the movement of the customer towards its favourite game. The following class diagram describes the dependencies:
```mermaid
classDiagram
class Player
Customer --|> Player
Context --o Game
Context --o Player
GamesAttractivenessManager --> Context
GamesAttractivenessAdapter --> SimulationState : adapts
GamesAttractivenessAdapter --> Context : adapts
SimulationState --o Customer
SimulationState --o Game
Game --> GameType
Player --> GameType : favourite game
```
#### Manager composition
All movement managers are implemented independently from one another, to combine them a simple bash-like DSL is created: to combine two managers the `|` operator is used, similarly to what the `andThen` method does in Scala between two functions. This operator is also used to apply a manager to update the current state. In order to weight the contribution of each manager the trait `WeightedManager` is defined which supports the `*` operator. The `*` operator multiplies the contribution of the manager by a given weight. The following example shows how to obtain a manager which combines various components to obtain a boids-like movement:
```scala 3
val boidManager : BaseManager[SimulationState] = BoidsAdapter(
  PerceptionLimiterManager(perceptionRadius)
          | alignmentWeight * AlignmentManager()
          | cohesionWeight * CohesionManager()
          | separationWeight * SeparationManager(avoidRadius)
          | VelocityLimiterManager(maxSpeed)
          | MoverManager()
)
```
So given an initial `SimulationState`, the `boidManager` can be applied to it to obtain an updated state, which contains the updated positions and velocities of the customers:
```scala 3
val state : SimulationState = ???
val updatedState = state | boidManager
```

### Important implementation aspects

## Testing
### Technologies used
For testing our code we used ScalaTest, probably, the most widely used and flexible testing framework for Scala.
The choice of this testing technology has different pros:
- **Rich Testing Styles**: With multiple built-in styles—such as FlatSpec, FunSuite, FunSpec, WordSpec, FreeSpec,
  PropSpec, and FeatureSpec—ScalaTest enables you to write tests in the style that best fits your needs: xUnit, BDD,
  nested specification, or property-based testing
- **Powerful Matchers & DSL**: ScalaTest includes expressive matchers that allow fluent assertions
- **Deep Ecosystem Integration**: ScalaTest integrates with build tools and frameworks including sbt, Maven, Gradle,
  IntelliJ, Eclipse, and testing tools like JUnit, TestNG, ScalaCheck, JMock, EasyMock, Mockito, and Selenium

### Coverage level

### Methodology used

### Relevant examples

### Other useful elements

## Retrospective
### Development progress, backlog, iterations

### Final comments
