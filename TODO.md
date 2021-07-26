update the number of cows in cattle farm when cows are produced


Role of the trader for the simulation ? 




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






QUESTIONS: 
Is it a problem if the object are mutable ? eg cows 



TODO:


ADD CO2 production 

Rendre le nombre de cow tuable a chaque tour un paramètre du cattlefarm

- modifier un peu la structure pour ne pas avoir besoin de passser market en argument de updtate (passer market en global ??)


Améliorer le marché en créeant des owner et Buyer 
  Keep track of all ressources stored in the market as "goods"
  But add a list of owner wich have a Good and a quantity and a price