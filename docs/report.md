# Casimo Documentation

## Adopted Development Process
### Task division methods

### Planned meetings/interactions

### Ongoing task review methods

### Choice of test/build/continuous integration tools

## Requirement Specification
### Business requirements

### Domain model

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
  if size(nearby_boids) > 0:
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
  if size(nearby_boids) > 0:
    for each other_boid in nearby_boids
      center_of_mass += other_boid.position
    center_of_mass /= size(nearby_boids)
    return normalize(center_of_mass - boid.position) 
  } else {
	return (0, 0)
  }
}
```
Each customer updates its position and velocity according to the following algorithm:
```
nearby_boids = collect_nearby_boids(b, boids)
separation = calculate_separation(b, nearby_boids)
alignment = calculate_alignment(b, nearby_boids)
cohesion = calculate_cohesion(b, nearby_boids)

/* Combine forces and update velocity */
b.velocity += SEPARATION_WEIGHT * separation
b.velocity += ALIGNMENT_WEIGHT * alignment
b.velocity += COHESION_WEIGHT * cohesion

/* Limit speed to MAX_SPEED */
if magnitude(b.velocity) > MAX_SPEED
b.velocity = normalize(b.velocity) * MAX_SPEED      

/* Update position */
b.position += b.velocity
```
Other parameters that influence the boids' behavior are:
- `MAX_SPEED`: maximum speed limit for boids
- `PERCEPTION_RADIUS`: distance within which a boid perceives others
- `AVOID_RADIUS`: minimum distance to avoid collisions
- `SEPARATION_WEIGHT`: weight for separation force
- `ALIGNMENT_WEIGHT`: weight for alignment force
- `COHESION_WEIGHT`: weight for cohesion force

#### System requirements

### Non-functional requirements

### Implementation requirements

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

### Diagrams

## Detailed Design
### Relevant design choices
An important design choice in our application is the use of a MVU architecture, which dictates how the application is structured and how data flows through it.
Cornerstone component is the update function, which has been designed in a way to simulate a loop in order to allow a better management of the simulation state.
![MVU Update Function Diagram](resources/update_loop.png)
### Design patterns

### Code organization

### Diagrams

## Implementation
### Student contributions
For each student: description of what was done/co-done and with whom

### Important implementation aspects

## Testing
### Technologies used

### Coverage level

### Methodology used

### Relevant examples

### Other useful elements

## Retrospective
### Development progress, backlog, iterations

### Final comments
