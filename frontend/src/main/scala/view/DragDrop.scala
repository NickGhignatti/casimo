package view

import org.scalajs.dom
import org.scalajs.dom.DataTransferDropEffectKind
import org.scalajs.dom.DataTransferEffectAllowedKind
import org.scalajs.dom.DragEvent
import org.scalajs.dom.HTMLElement
import org.scalajs.dom.console

object DragDrop:

  def makeDraggable(element: HTMLElement, canvasManager: CanvasManager): Unit =
    element.draggable = true

    element.addEventListener(
      "dragstart",
      { (e: DragEvent) =>
        e.dataTransfer.setData("text/plain", element.dataset("type"))
        e.dataTransfer.effectAllowed = DataTransferEffectAllowedKind.copy
      }
    )

    val canvas = dom.document.getElementById("main-canvas")
    canvas.addEventListener(
      "dragover",
      { (e: DragEvent) =>
        e.preventDefault()
        e.dataTransfer.dropEffect = DataTransferDropEffectKind.copy
      }
    )

    canvas.addEventListener(
      "drop",
      { (e: DragEvent) =>
        e.preventDefault()
        val componentType = e.dataTransfer.getData("text/plain")
        val rect = canvas.getBoundingClientRect()
        val x = e.clientX - rect.left
        val y = e.clientY - rect.top

        canvasManager.addComponent(x, y, componentType)
      }
    )
