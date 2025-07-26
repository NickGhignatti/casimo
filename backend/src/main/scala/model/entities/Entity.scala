package model.entities

/** Base trait for all entities in the simulation system.
  *
  * Defines the minimal contract that all simulation entities must fulfill.
  * Every entity in the system must have a unique identifier for tracking,
  * collision detection, state management, and entity relationships.
  *
  * This trait serves as the foundation for the entity hierarchy, allowing
  * different types of simulation objects (customers, games, spawners, etc.) to
  * be handled uniformly while maintaining their unique identities.
  */
trait Entity:
  val id: String
