package view

import com.raquo.laminar.api.L._
import model.SimulationState
import model.entities.spawner.SpawningStrategyBuilder
import org.nspl._
import org.nspl.canvasrenderer._
import org.scalajs.dom
import org.scalajs.dom.html
import update.Event
import update.Update

class ButtonBar(
    model: Var[SimulationState],
    update: Var[Update],
    configForm: ConfigForm,
    modal: Modal,
    eventBus: EventBus[Event]
):
  private val buttonBar = dom.document.getElementById("button-bar")
  private val buttons = List("Add", "Run", "Reset", "Save", "Load", "Data")

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
      case "Add" =>
        eventBus.writer.onNext(
          Event.AddCustomers(configForm.currentStrategyType match
            case "constant" =>
              SpawningStrategyBuilder()
                .constant(configForm.constantStrategyConfigInfo)
                .build()
            case "gaussian" =>
              val config = configForm.gaussianStrategyConfigInfo
              SpawningStrategyBuilder()
                .gaussian(config._1, config._2, config._3)
                .build()
            case "step" =>
              val config = configForm.stepStrategyConfigInfo
              SpawningStrategyBuilder()
                .step(config._1.toInt, config._2.toInt, config._3, config._4)
                .build()
          )
        )
      case "Run" =>
        dom.window.setInterval(
          () => eventBus.writer.onNext(Event.SimulationTick),
          50
        )
      case "Reset" => ???
      case "Save"  => ???
      case "Load"  => ???
      case "Data"  => modal.open()
      case _       => ???
