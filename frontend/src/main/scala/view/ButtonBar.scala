package view

import com.raquo.laminar.api.L._
import model.SimulationState
import model.entities.spawner.SpawningStrategyBuilder
import org.scalajs.dom
import org.scalajs.dom.html
import update.Event
import update.Update

class ButtonBar(
    model: Var[SimulationState],
    update: Var[Update],
    configForm: ConfigForm
):
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
          500
        )
      case "Reset" => ???
      case "Save"  => ???
      case "Load"  => ???
      case _       => ???
