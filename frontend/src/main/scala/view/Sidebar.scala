package view

import org.scalajs.dom
import org.scalajs.dom.html

class Sidebar:
  private val sidebar = dom.document.getElementById("sidebar")
  private val components = List("Roulette", "Slot", "BlackJack", "Wall")

  def init(canvasManager: CanvasManager): Unit =
    components.foreach { comp =>
      val element = dom.document.createElement("div").asInstanceOf[html.Div]
      element.className = "draggable-component"
      element.textContent = comp
      element.dataset.update("type", comp)
      sidebar.appendChild(element)

      DragDrop.makeDraggable(element, canvasManager)
    }
