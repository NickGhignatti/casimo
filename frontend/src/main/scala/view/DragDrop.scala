package view

import org.scalajs.dom
import org.scalajs.dom.{DataTransferDropEffectKind, DragEvent, HTMLElement}

object DragDrop:
  private var canvasManager: Option[CanvasManager] = None

  def registerCanvasManager(manager: CanvasManager): Unit =
    canvasManager = Some(manager)

  def makeDraggable(element: HTMLElement): Unit =
    element.draggable = true

    element.addEventListener(
      "dragstart",
      { (e: DragEvent) =>
        e.dataTransfer.setData("text/plain", element.dataset("type"))
        e.dataTransfer.dropEffect = DataTransferDropEffectKind.copy
      }
    )

    val canvas = dom.document.getElementById("main-canvas")
    canvas.addEventListener(
      "dragover",
      { (e: DragEvent) =>
        e.preventDefault() // Allow drop
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

        canvasManager.foreach(_.drawComponent(x, y, componentType))
      }
    )
