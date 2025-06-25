package view

import com.raquo.laminar.api.L.{*, given}
import model.SimulationState
import org.scalajs.dom

import scala.scalajs
import scala.scalajs.js
import scala.scalajs.js.annotation.*

@js.native @JSImport("/javascript.svg", JSImport.Default)
val javascriptLogo: String = js.native

def InitView(): Unit =
  renderOnDomContentLoaded(
    dom.document.getElementById("app"),
    Main.appElement()
  )

  object Main:
    def appElement(): Element =
      div(
        cls := "main-application",
        height := "98vh",
        width := "99vw",
        // Canvas centered
        div(
          cls := "canvas-wrapper",
          canvasTag(
            cls := "simulation-canvas",
            onMountCallback { ctx =>
              val cnv = ctx.thisNode.ref
              // Kick off drawing loop or simulation here
              drawInitial(cnv)
            }
          )
        ),
        // Bottom controls
        div(
          cls := "bottom-controls",
          button(
            "Start",
            onClick --> { _ =>
              update.Update.update(
                SimulationState(List(), List()),
                update.Event.SimulationTick
              )
            }
          ),
          button("Pause", onClick --> { _ => println("Pause clicked") }),
          button("Resume", onClick --> { _ => println("Resume clicked") })
        )
      )

    private val signalBus = new EventBus[String] // your MVU dispatch
    def events: EventStream[String] = signalBus.events

    def drawInitial(canvas: dom.html.Canvas): Unit =
      val ctx =
        canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
      ctx.fillStyle = "#fff"
      ctx.fillRect(0, 0, canvas.width, canvas.height)
