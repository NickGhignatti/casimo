package view

import com.raquo.laminar.api.L._
import model.SimulationState
import org.scalajs.dom
import org.scalajs.dom.html
import update.Event
import update.Update

class ButtonBar(model: Var[SimulationState], update: Var[Update]):
  private val buttonBar = dom.document.getElementById("button-bar")
  private val buttons = List("Add", "Run", "Reset", "Save", "Load")

  val eventBus = new EventBus[Event]

  eventBus.events
    .scanLeft(model.now())((m, e) => update.now().update(m, e))
    .foreach(model.set)(using unsafeWindowOwner)

  def init(): Unit =
    buttons.foreach { text =>
      val button =
        dom.document.createElement("button").asInstanceOf[html.Button]
      button.className = "button"
      button.textContent = text
      button.onclick = _ => handleButtonClick(text)
      buttonBar.appendChild(button)
    }

  private def handleButtonClick(action: String): Unit =
    action match
      case "Add" => eventBus.writer.onNext(Event.AddCustomers(50))
      case "Run" =>
        dom.window.setInterval(
          () => eventBus.writer.onNext(Event.SimulationTick),
          500
        )
      case "Reset" => ???
      case "Save"  => ???
      case "Load"  => ???
      case _       => ???
