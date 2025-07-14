import com.raquo.airstream.core.Signal
import com.raquo.laminar.api.L.Var
import model.SimulationState
import model.entities.customers.DefaultMovementManager
import model.managers.BaseManager
import org.scalajs.dom.document
import update.Update
import view.ButtonBar
import view.CanvasManager
import view.ConfigForm
import view.Sidebar

@main
def main(): Unit =
  val model = SimulationState(List.empty, List.empty, None)
  document.addEventListener(
    "DOMContentLoaded",
    { _ =>
      val modelVar = Var(model)
      val updateVar = Var(Update(DefaultMovementManager()))

      val canvasManager = CanvasManager(modelVar)
      val sidebar = Sidebar()
      val buttonBar = ButtonBar(modelVar, updateVar)

      canvasManager.init()
      sidebar.init(canvasManager)
      buttonBar.init()
      document.body.appendChild(
        ConfigForm(updateVar).init().ref
      )
    }
  )
