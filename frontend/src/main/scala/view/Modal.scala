package view

import com.raquo.laminar.api.L._
import model.SimulationState
import model.data.DataManager
import org.nspl.par
import org.nspl.xyplot
import org.scalajs.dom
import org.scalajs.dom.html
import update.Update

class Modal(
    model: Var[SimulationState],
    update: Var[Update],
    dataManager: Var[DataManager]
):
  private val isVisible = Var(false)
  private val gamesBankroll: Var[List[Double]] = Var(List.empty)
  private val customersBankroll: Var[List[Double]] = Var(List.empty)

  // Import all necessary given instances and implicits
  import org.nspl.canvasrenderer.given
  import org.nspl.given

  def open(): Unit = isVisible.set(true)
  def close(): Unit = isVisible.set(false)

  private def updateBankrolls(): Unit =
    gamesBankroll.set(
      gamesBankroll.now() :+ dataManager.now().currentGamesBankroll
    )
    customersBankroll.set(
      customersBankroll.now() :+ dataManager.now().currentCustomersBankroll
    )

  private def createPlot(): html.Canvas =
    val gamesSeries = gamesBankroll.now().zipWithIndex.map {
      case (value, index) => (index.toDouble, value)
    }

    val customersSeries = customersBankroll.now().zipWithIndex.map {
      case (value, index) => (index.toDouble, value)
    }

    if (gamesSeries.nonEmpty && customersSeries.nonEmpty) {
      try
        import org.nspl.data.*

        // Create data using explicit Row conversion
        val gamesData = gamesSeries.map { case (x, y) => (x, y) }.toSeq
        val customersData = customersSeries.map { case (x, y) => (x, y) }.toSeq

        val plot = xyplot(customersData)(
          par(
            ylab = "Bankroll Value",
            xlab = "Time Step",
            main = "Games Bankroll Over Time"
          )
        )

        val (canvas: html.Canvas, updatePlot) =
          org.nspl.canvasrenderer.render(plot, width = 800, height = 500)

        canvas
      catch
        case e: Exception =>
          println(s"Error creating NSPL plot: ${e.getMessage}")
          // Fallback: create a simple canvas with basic drawing
          createFallbackCanvas()
    } else
      createFallbackCanvas()

  private def createFallbackCanvas(): html.Canvas =
    val canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
    canvas.width = 600
    canvas.height = 400

    val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    // Draw a simple fallback plot
    ctx.fillStyle = "#f0f0f0"
    ctx.fillRect(0, 0, 600, 400)

    ctx.strokeStyle = "#333"
    ctx.lineWidth = 2
    ctx.strokeRect(50, 50, 500, 300)

    ctx.fillStyle = "#333"
    ctx.font = "16px Arial"
    ctx.fillText("Bankroll Over Time", 250, 30)
    ctx.fillText("Time Step", 300, 380)

    // Rotate and draw y-label
    ctx.save()
    ctx.translate(20, 200)
    ctx.rotate(-Math.PI / 2)
    ctx.fillText("Bankroll Value", 0, 0)
    ctx.restore()

    // Draw simple line if we have data
    val gamesSeries = gamesBankroll.now()
    if (gamesSeries.nonEmpty) {
      ctx.strokeStyle = "#ff0000"
      ctx.lineWidth = 2
      ctx.beginPath()

      gamesSeries.zipWithIndex.foreach { case (value, index) =>
        val x =
          50 + (index.toDouble / math.max(1, gamesSeries.length - 1)) * 500
        val maxVal = gamesSeries.max
        val minVal = gamesSeries.min
        val range = math.max(1, maxVal - minVal)
        val y = 350 - ((value - minVal) / range) * 300

        if (index == 0) ctx.moveTo(x, y) else ctx.lineTo(x, y)
      }
      ctx.stroke()
    }

    canvas

  def init(): HtmlElement =
    val plotCanvas = Var[Option[html.Canvas]](None)

    dom.window.setInterval(
      () =>
        dataManager.set(
          update
            .now()
            .updateSimulationDataManager(dataManager.now(), model.now())
        )
        updateBankrolls()

        // Update the plot canvas
        try
          val newCanvas = createPlot()
          plotCanvas.set(Some(newCanvas))
        catch
          case e: Exception =>
            println(s"Error creating plot: ${e.getMessage}")
      ,
      500
    )

    div(
      cls := "modal-backdrop",
      display <-- isVisible.signal.map(if _ then "flex" else "none"),
      div(
        cls := "modal-content",
        button(
          "X",
          cls := "modal-close",
          onClick --> (_ => close())
        ),
        div(
          cls := "plot-container",
          child <-- plotCanvas.signal.map {
            case Some(canvas) =>
              // Create a div wrapper for the canvas
              val wrapper = div()
              wrapper.ref.appendChild(canvas)
              wrapper
            case None =>
              div("Loading plot...")
          }
        )
      )
    )
