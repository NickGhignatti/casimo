import com.raquo.laminar.api.L.Var
import model.SimulationState
import org.scalajs.dom.document
import view.ButtonBar
import view.CanvasManager
import view.Sidebar

@main
def main(): Unit =
  val model = SimulationState(List.empty, List.empty, None)
  document.addEventListener(
    "DOMContentLoaded",
    { _ =>
      val modelVar = Var(model)

      val canvasManager = new CanvasManager(modelVar)
      val sidebar = new Sidebar()
      val buttonBar = new ButtonBar(model, modelVar)

      canvasManager.init()
      sidebar.init(canvasManager)
      buttonBar.init()
    }
  )
