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

### Description of architectural patterns used

### Any distributed system components

### Crucial technological choices for architecture

### Diagrams

## Detailed Design
### Relevant design choices

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
