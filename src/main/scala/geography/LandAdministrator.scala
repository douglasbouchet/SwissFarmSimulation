package geography

import Companies.{Mill, Supermarket}
import Owner.Owner
import Simulation.Simulation
import code.{Instruction, __do, __forever, __wait}
//import Simulation.SimLib.Mill
import Simulation.SimO
import farmpackage.Farmer
import generation.LandGenerator

import scala.collection.mutable

/** Used to perform operation on LandOverlay (split, merge, add/remove
 * parcelles) and get informations (find specific type of landOverlay)
 */

class LandAdministrator(s: Simulation, canton: String) extends SimO(s) {

  val landGenerator = new LandGenerator()

  var cadastralParcels: List[CadastralParcel] = landGenerator.generateParcels(canton)
  var landOverlays: List[LandOverlay] = List[LandOverlay]()

  val landIndexing: mutable.Map[CadastralParcel, Int] = landGenerator.parcelsToIndex

  //get all the agents of a given type, this will be used when looking for near agents of a specific type
  //i.e search a mill, a supermarket, or whatever. Care to update carefully this list
  var farmersList : List[Farmer] = List()
  var millsList : List[Mill] = List()
  var supermarketsList : List[Supermarket] = List()

  /** Split a land overlay in multiple lands overlays of same/different
   * purpose.
   *
   * @param current :
   *                LandOverlay, the land overlay to split
   * @param into    :
   *                List[LandOverlay], the final lands overlays into must only contain
   *                CadastralParcels belonging to current, and each CadastralParcel can
   *                appear in one and only one LandOverlay
   */
  def splitLandOverlay(
                        current: LandOverlay,
                        into: List[(LandOverlay, LandOverlayPurpose.Value)]
                      ): Boolean = {

    assert(landOverlays.contains(current))

    into.foreach(tup => assert(tup._1.getCadastralParcels.length > 0))
    //Check if each cadastral parcels of into is actually in current
    into.foreach(tup =>
      tup._1
        .getCadastralParcels
        .foreach(parcel =>
          assert(current.getCadastralParcels.contains(parcel))
        )
    )

    into.foreach(tup => changePurpose(tup._1, tup._2))

    /** Update the state of partOf inside every parcel involved */
    current.landsLot.foreach(_._1.partOf -= current)
    //into.foreach((land_overlay, purpose) =>
    into.foreach(tup =>
      tup._1.landsLot.foreach(tup2 => tup2._1.partOf += (tup._1 -> tup2._2))
    )

    landOverlays.filterNot(_ == current)

    landOverlays :::= into.map(_._1)

    true
  }

  /** Remove the land Overlay, and create a new One of different type */
  def changePurpose(
                     landOverlay: LandOverlay,
                     newPurpose: LandOverlayPurpose.Value
                   ): LandOverlay = {
    //remove the landOverlay, but keep its land in memory, as only the purpose changes
    val oldLands: List[(CadastralParcel, Double)] = landOverlay.landsLot
    removeLandOverlay(landOverlay)
    addLandOverlay(oldLands, newPurpose)
  }

  def removeLandOverlay(landOverlay: LandOverlay): Unit = {
    landOverlay.getCadastralParcels.foreach((parcelle: CadastralParcel) => landOverlay.removeCadastralParcel(parcelle))
    landOverlays = landOverlays.filterNot(_ == landOverlay)

  }

  def addLandOverlay(landsDistrib: List[(CadastralParcel, Double)], purpose: LandOverlayPurpose.Value): LandOverlay = {
    val landOverlay: LandOverlay = purpose match {
      case LandOverlayPurpose.wheatField => new Crop(landsDistrib)
      case LandOverlayPurpose.paddock => new Paddock(landsDistrib)
      case LandOverlayPurpose.meadow => new LandOverlay(landsDistrib) //TODO
      case LandOverlayPurpose.noPurpose => new LandOverlay(landsDistrib) //TODO
    }
    landOverlays ::= landOverlay
    landOverlay
  }

  def mergeLandOverlay(
                        toMerge: List[LandOverlay],
                        newPurpose: LandOverlayPurpose.Value
                      ): Boolean = {

    var mergedLandOverlay = new LandOverlay(toMerge.map(_.landsLot).flatten)

    toMerge.foreach(l_over => assert(landOverlays.contains(l_over)))

    changePurpose(mergedLandOverlay, newPurpose)

    toMerge.foreach(lOver => {
      lOver.purpose = newPurpose
    })

    /** remove old land overlay in partOf inside each parcel */
    toMerge.foreach(land_overlay =>
      land_overlay.landsLot.foreach(tup => tup._1.partOf -= land_overlay)
    )

    /** add new land overlay in partOf inside each parcel */
    mergedLandOverlay.landsLot.foreach(tup =>
      tup._1.partOf += (mergedLandOverlay -> tup._2)
    )

    toMerge.foreach(l_over => landOverlays.filterNot(_ == l_over))
    landOverlays ::= mergedLandOverlay

    true
  }

  /** Changing owner of a Parcelle, will update this Parcelle's owner inside
   * LandOverlay and LandAdministrator Just need to recompute
   * ownershipDistrib inside each land overlays, the Parcelle is part of
   */
  def changeCadastralParcelOwner(
                                  cadastralParcel: CadastralParcel,
                                  new_owner: Owner
                                ) = {
    cadastralParcel.owner = new_owner
    cadastralParcel.partOf.keys.foreach(key =>
      key.cmptOwnershipDistrib(key.landsLot)
    )
  }

  //Return the parcels currently owned by no one, (so they can be assigned to some agents)
  def getFreeParcels: List[CadastralParcel] = {
    cadastralParcels.filter(_.owner == null)
  }

  //TODO
  def findParcelAccess(parcel: CadastralParcel) {}



  /**
   * Find the n closest farmers, given a current parcel.
   * Distance compute using L2-norm
   * @param from parcel from which we want to search
   * @param n
   * @return an option of such a list
   * TODO, see if we replace n by a distance
   */
  def findNClosestFarmers(from: CadastralParcel, n: Int): Option[List[(Farmer, Double)]] = {
    val x = landIndexing.contains(from)
    val y = landIndexing.contains(cadastralParcels(0))
    val fromIndex = landIndexing.getOrElse(from, -1)
    require(fromIndex != -1)
    val sideLength = landGenerator.sideLength
    val fromIndexLine = fromIndex / sideLength
    val fromIndexCol = fromIndex % sideLength
    var agentsByDistance = List[(Farmer, Double)]()
    farmersList.foreach(farmer =>{
      farmer.parcels.headOption match {
        case Some(parcel) => {
          val parcelIndex = landIndexing.getOrElse(parcel, -1)
          require(parcelIndex != -1)
          //as we pass the first parcel in from arg, sufficient to check that head != to not put the asking agent
          if(parcelIndex != fromIndex){
            agentsByDistance ::= (farmer, math.sqrt(math.pow(fromIndexLine - parcelIndex / sideLength,2) + math.pow(fromIndexCol - parcelIndex % sideLength,2)))
          }

        }
        case None => println("A farmer doesn't got any parcel (findNClosestFarmers)")
      }
    })
    if (agentsByDistance.isEmpty) None
    else Some(agentsByDistance.sortBy(_._2).takeRight(n))
  }

  def findNClosestMills(from: CadastralParcel, n: Int): Option[List[(Farmer, Double)]] = {
    //TODO
    None
  }

  def findNClosestBakery(from: CadastralParcel, n: Int): Option[List[(Farmer, Double)]] = {
    //TODO
    None
  }




  /*def findNClosestMills(from: CadastralParcel, n: Int): Option[List[(Mill, Double)]] = {
    val fromIndex = landIndexing.getOrElse(from, -1)
    val sideLength = landGenerator.sideLength
    val fromIndexLine = fromIndex / sideLength
    val fromIndexCol = fromIndex % sideLength
    require(fromIndex != -1)
    var agentsByDistance = List[(Mill, Double)]()
    millsList.foreach(mill =>{
      mill.parcels.headOption match {
        case Some(parcel) => {
          val parcelIndex = landIndexing.getOrElse(parcel, -1)
          require(parcelIndex != -1)
          agentsByDistance ::= (mill, math.sqrt(math.pow(fromIndexLine - parcelIndex / sideLength,2) + math.pow(fromIndexCol - parcelIndex % sideLength,2)))
        }
        case None => println("A mill doesn't got any parcel (findNClosestMills)")
      }
    })
    if (agentsByDistance.isEmpty) None
    else Some(agentsByDistance.sortBy(_._2).takeRight(n))
  }
  */


  /** Only update quantity of grass on each Paddock at the moment, can be more complex afterwards, like changing caracteristics of lands after a rainfall, etc... */
  def update(): Unit = {
    getPaddocks().foreach(_.grassGrowth())
  }

  /**
   * @return The list of Paddocks (inheriting from landOverlay)
   */
  def getPaddocks(): List[Paddock] = {
    landOverlays.filter(_.isInstanceOf[Paddock]).map(_.asInstanceOf[Paddock])
  }

  //add the agent in function of its type in the good list
  def addAgent(agent: SimO): Unit = {
    agent match {
      case agent@(_ : Farmer) =>
        farmersList ::= agent

      case agent@(_ : Supermarket) =>
        supermarketsList ::= agent

      case agent@(_: Mill) =>
        millsList ::= agent

      case _ => println("Unknown type")
    }
  }

  override def mycopy(_shared: Simulation, _substitution: mutable.Map[SimO, SimO]): SimO = ???

  override protected def algo: Instruction = __forever(
    __do(
      update()
    ),
    __wait(1*CONSTANTS.TICKS_TIMER_PER_DAY)
  )
}

