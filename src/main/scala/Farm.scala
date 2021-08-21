package farmpackage

import landAdministrator.CadastralParcel
class Farm(){

  var parcels: List[CadastralParcel] = List()

  def addParcels(newParcels: List[CadastralParcel]) {
    parcels :::= newParcels
  }
}