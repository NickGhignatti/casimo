package view

import org.scalajs.dom
import org.scalajs.dom.html

class ButtonBar:
  private val buttonBar = dom.document.getElementById("button-bar")
  private val buttons = List("Run", "Reset", "Save", "Load")

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
      case "Run" => dom.console.log("Simulation started")
      case "Reset" =>
        dom.console.log("Simulation reset")
//        CanvasManager.clearCanvas()
      case "Save" => dom.console.log("State saved")
      case "Load" => dom.console.log("State loaded")
      case _      => // Ignore
