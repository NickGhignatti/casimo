package view

import com.raquo.airstream.core.Signal
import com.raquo.laminar.api.L.Var
import model.SimulationState
import model.entities.customers.{Customer, CustomerList}

final class ViewModel:
  val dataVar: Var[CustomerList] = Var(List.empty)
  val dataSignal = dataVar.signal

  def addCustomer(item: Customer): Unit =
    dataVar.update(data => data :+ item)

  def removeDataItem(id: Customer): Unit =
    dataVar.update(data => data.filter(_.id != id))
