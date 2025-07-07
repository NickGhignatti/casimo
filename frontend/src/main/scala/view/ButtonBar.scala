package view

import com.raquo.laminar.api.L.*
import model.SimulationState
import org.scalajs.dom
import org.scalajs.dom.html
import update.{Event, Update}

class ButtonBar(state: SimulationState, model: Var[SimulationState]):
  private val buttonBar = dom.document.getElementById("button-bar")
  private val buttons = List("Add", "Run", "Reset", "Save", "Load")

  private val eventBus = new EventBus[Event]

  eventBus.events
    .scanLeft(model.now())((m, e) => Update.update(m, e))
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
    val event: Event = action match
      case "Add"   => Event.AddCustomers(50)
      case "Run"   => Event.SimulationTick
      case "Reset" => ???
      case "Save"  => ???
      case "Load"  => ???
      case _       => ???
    eventBus.writer.onNext(event)
