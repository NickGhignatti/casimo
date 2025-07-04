import model.SimulationState
import org.scalajs.dom.document
import view.{ButtonBar, CanvasManager, Sidebar}

@main
def main(): Unit =
  val model = SimulationState(List.empty, List.empty)
  document.addEventListener(
    "DOMContentLoaded",
    { _ =>
      val canvasManager = new CanvasManager()
      val sidebar = new Sidebar()
      val buttonBar = new ButtonBar()

      canvasManager.init()
      sidebar.init()
      buttonBar.init()
    }
  )
