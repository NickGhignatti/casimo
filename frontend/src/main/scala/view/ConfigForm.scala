package view

import com.raquo.laminar.api.L._
import model.entities.customers.DefaultMovementManager

object ConfigForm:

  def init(): HtmlElement =

    val maxSpeedVar = Var(1000.0)
    val perceptionRadiusVar = Var(200000.0)
    val avoidRadiusVar = Var(50.0)

    val formDataSignal: Signal[DefaultMovementManager] =
      Signal.combineWithFn(
        maxSpeedVar.signal,
        perceptionRadiusVar.signal,
        avoidRadiusVar.signal
      )(DefaultMovementManager(_, _, _))

    div(
      h3("Movement Manager Form"),
      div(
        label("Max Speed: "),
        input(
          typ := "number",
          controlled(
            value <-- maxSpeedVar.signal.map(_.toString),
            onInput.mapToValue.map(
              _.toDoubleOption.getOrElse(0.0)
            ) --> maxSpeedVar
          )
        )
      ),
      div(
        label("Perception Radius: "),
        input(
          typ := "number",
          controlled(
            value <-- perceptionRadiusVar.signal.map(_.toString),
            onInput.mapToValue.map(
              _.toDoubleOption.getOrElse(0.0)
            ) --> perceptionRadiusVar
          )
        )
      ),
      div(
        label("Avoid Radius: "),
        input(
          typ := "number",
          controlled(
            value <-- avoidRadiusVar.signal.map(_.toString),
            onInput.mapToValue.map(
              _.toDoubleOption.getOrElse(0.0)
            ) --> avoidRadiusVar
          )
        )
      ),
      hr(),

      // Live preview of the form data
      child <-- formDataSignal.map { data =>
        pre(
          s"Current values:\nMax Speed: ${data.maxSpeed}\n" +
            s"Perception Radius: ${data.perceptionRadius}\n" +
            s"Avoid Radius: ${data.avoidRadius}"
        )
      }
    )
