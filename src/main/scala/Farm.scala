package farmpackage

import landAdministrator.CadastralParcel
case class Farm(){

  var parcels: List[CadastralParcel] = List()
  var name = "ferme de douglas"

  def addParcels(newParcels: List[CadastralParcel]) {
    parcels :::= newParcels
  }
}