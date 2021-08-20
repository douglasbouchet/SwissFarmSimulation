# Plan

# The Class 

- super class animals extend ? sim ? 
  - age: int
  - weight: int 
  - Organic Feedstuff: bool (start with something simple, but then extends to all kinds of cereals etc)
  - Quantity feedstuff : int (f(weight,age)) (maybe juste f(weight) atm) 
  - Owner: Farm
  - opt(Health: int -> usefull to determine quality of the meat in f. of feedstuff, environnement, vaccin)
  - opt(Farmer if decide to separate meat and cereals) 
- Supplier abstract
  - list (commodities, quantity)
  - list farm (clients)
  - Opt(list supplier, but assume source atm)
  - Opt(location)

## What we need to model

- cows extends animal 
  - pregant: bool
  - state counter -> influence attributs of the cows (age inc. etc..) Interactions dans le md

- Farm (both meat + cereals)
  - Bio: bool
  - n employee: int <- f(Bio, wheatland)
  - surface agricole utile of wheat = wheatland: int (ha) (maybe a double (percentage) in f. of surface agricole totale) (wheat atm but extend to all cereals after)
  - surface agricole totale ? = land : int (ha)
  - herd: list Cows
  - Seed Supplier: Type évolué, where buy wheatseed in f. of 
  - Feedstuff Supplier: type ev. where buy feedstuff + opt(vaccin)
  - Quantity of CO2 emitted <- f(herd,wheatland*RF where RF <- f(Bio)): double (kg) -> plus tard: kg/quantity commodities produced
  - state counter -> influence attributs of the cows (when buy feedstuff, seeds, when sell cereals, etc) Interactions dans le md
  - Opt(location)


- case class Seed Supplier extends Supplier
  - state counter Interactions dans le md

- case class Feedstuff Supplier extends Supplier
  - state counter Interactions dans le md


- Market 
  - List stuff to buy: List Commodities
  - List person (clients)
  - state counter 



- Person TODO

Dans l'algo:
  each step, check le state counter de chaque agent et voir si des actions sont requises




# The global Agent (The map):

## What it needs to do: 

- Put agents on location 
- Manage actions of agents between them (maybe some locals action can be done by the agent themself but anyway they will have to tell their new state to the global agent, so maybe only the global agent manage the actions)? 
- Manage the market (all exchange) 
  - Need a search algo to put agents in relation
- Manage the weather
- May play the role of the government ?

## What do we represent:
- The roads form the edges of the network, they needs to be updated, so the network change
  - Flux (vehicule per min)
  - Type ? (dirt, tar etc)
  - Restriction (no more than 20T, prohibited for tractor (e.g highway), private)
  - Respect the shape of the real world (plot as plotted on google map) or just edges with caracteristic ?? The edge could be plot between each intersection -> hard to correctly represent the true land without overlapping-> prefer a real representation, but use edges that can have a slot shape between each intersection 
- Lands 
  - Type (field, pasture, vines, urban, other)
  - Possessor (Agents ?), the one that interact on it (+ some other agents may interact on it also)
  - Surface 
  - Shape (as a polygone, with a list of sides)
  - Connected to which road (where can be implemented latter, just say which side of the polygon, concerning koch on outgoing points)
  - Some stats (dry/wet, quality of the sol) -> This can come after, and just add some attributs (EZ to add)
  - + maybe we want to model rare events (boar, too much rain, freeze,...)
- All other agents could need a graphical representation (e.g person by cities, farms next to their land etc) 


## How the agents are managed
- Each agent chose individually what he wants to do
- The global agent needs to keep state consistent of all agents -> each time an agent make a modification of its state -> report to the global agent (but only modifications that other agents may care about)
- Some local actions don't need a report to the global agent, but they might have changes (e.g number of cows available to eat) that needs to be report.










est ce que si on achète une parcelle, qu'elle est membre d'un overlay, peut on changer par exemple la partie ocmmune d'un paddoc a un field ? 

Create object Crops Manager which keep track of all land overlay, and their purpose ? 


TOMORROW focus on road network 
-> keep roads as parcelle atm, maybe easier when we get the data
But most of the work will be done in the RoadAdministrator (find a good name) which
will be in charge of creating a network and computing path, length etc....
this is the object that will be called, as the land administrator

also, see if define a function inside landAdministrator to find for example saleable parcelles, but should probably be in an market instead

+ method add and remove landOverlays

TODO See if adding caracterstics about the crops etc inside the LandOverlay 

Parcelle -> CadastralParcel, parcelles -> cadastral_parcels, parcelle -> cadastral_parcel

ASK CHRISTOPH !!!!!!!!!! Change Parcelle by LandLot, parcelles = landlots

# Caching:
Currently, path for example are requires from one parcel to another. Unlikely that this path will be ask by other simulated entities. -> should be cache locally inside the entity that ask it. (WeakHashMap or HashMap)
But some info could be asked by more people: 
This info could be cache in concurrent Map, at the ...Administrator level

change cadastral_parcels_aggregation to landLot