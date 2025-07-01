import model.SimulationState
import model.customers.BoidCustomer
import view.View

@main
def main(): Unit =
  val model = SimulationState[BoidCustomer](List.empty, List.empty)
  View(model).init()
