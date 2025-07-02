import model.SimulationState
import view.View

@main
def main(): Unit =
  val model = SimulationState(List.empty, List.empty)
  View(model).init()
