import com.raquo.laminar.api.L.*
import model.SimulationState
import model.data.DataManager
import model.entities.customers.DefaultMovementManager
import org.scalajs.dom
import org.scalajs.dom.{document, html}
import update.Event
import update.Update
import view.ButtonBar
import view.CanvasManager
import view.ConfigForm
import view.Modal
import view.Sidebar

@main
def main(): Unit =
  val model = SimulationState.empty()
  val dataManager = DataManager(model)

  document.addEventListener(
    "DOMContentLoaded",
    _ =>
      val modelVar = Var(model)
      val dataManagerVar = Var(dataManager)
      val updateVar = Var(Update(DefaultMovementManager()))

      val eventBus = new EventBus[Event]

      eventBus.events
        .scanLeft(modelVar.now())((m, e) => updateVar.now().update(m, e))
        .foreach(modelVar.set)(using unsafeWindowOwner)

      val sidebar = Sidebar()
      val modal = new Modal(modelVar, updateVar, dataManagerVar)
      val configForm = ConfigForm(updateVar, modelVar)
      val canvasManager = CanvasManager(modelVar, updateVar, eventBus)
      val buttonBar =
        ButtonBar(
          modelVar,
          updateVar,
          configForm,
          modal,
          eventBus,
          canvasManager
        )

      canvasManager.init()
      sidebar.init(canvasManager)
      buttonBar.init()

      val container = document.createElement("div")
      document.body.appendChild(container)
      render(container, div(configForm.init(), modal.init()))
  )
