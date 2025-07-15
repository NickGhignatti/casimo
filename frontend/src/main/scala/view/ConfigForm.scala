package view

import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.L.unsafeWindowOwner
import com.raquo.laminar.nodes.ReactiveHtmlElement
import model.entities.customers.DefaultMovementManager
import org.scalajs.dom.HTMLDivElement
import update.Update

case class ConfigForm(update: Var[Update]):

  given Owner = unsafeWindowOwner
  private val maxSpeedVar = Var("1000.0")
  private val perceptionRadiusVar = Var("200000.0")
  private val avoidRadiusVar = Var("50.0")

//  Signal.combineWithFn(maxSpeedVar, perceptionRadiusVar, avoidRadiusVar)(DefaultMovementManager(_, _, _))
//    .map(Update(_))
//    .addObserver(update.toObserver)

//  update.signal
//    .map(_.customerManager)
//    .map(_.asInstanceOf[DefaultMovementManager])
//    .foreach { data =>
//      println(
//        s"Current values:\nMax Speed: ${data.maxSpeed}\n" +
//          s"Perception Radius: ${data.perceptionRadius}\n" +
//          s"Avoid Radius: ${data.avoidRadius}"
//      )
//    }
  def init(): ReactiveHtmlElement[HTMLDivElement] =
    // === Laminar reactive example ===
    val textVar = Var("")

    val app = div(
      h2("Reactive Input Example"),
      input(
        typ := "text",
        placeholder := "Type something...",
        onInput.mapToValue --> textVar
      ),
      p(
        child.text <-- textVar.signal
      )
    )
    app

//    val inputTextVar = Var("")
//    val checkedVar = Var(false)
//    div(
//      p(
//        label("Name: "),
//        input(
//          onInput.mapToValue --> inputTextVar
//        )
//      ),
//      p(
//        "You typed: ",
//        text <-- inputTextVar
//      ),
//      p(
//        label("I like to check boxes: "),
//        input(
//          typ("checkbox"),
//          onInput.mapToChecked --> checkedVar
//        )
//      ),
//      p(
//        "You checked the box: ",
//        text <-- checkedVar
//      )
//    )

//    div(
//      h3("Movement Manager Form"),
//      parameter(
//        "Max Speed",
//        maxSpeedVar,
//      ),
//      parameter(
//        "Perception Radius",
//        perceptionRadiusVar,
//      ),
//      parameter(
//        "Avoid Radius",
//        avoidRadiusVar,
//      ),
//      hr(),
//
//      // Live preview of the form data
//
//    )

  private def parameter(
      labelText: String,
      variable: Var[String]
  ): HtmlElement =
    val variable = Var("")
    div(
      label(labelText),
      input(
//        value <-- variable.signal.map(_.toString),
        onInput.mapToValue --> variable
      ),
      p(
        s"Current $labelText: ",
        text <-- variable
      )
    )
