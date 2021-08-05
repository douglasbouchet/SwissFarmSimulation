# The Main phases of the Simulation:
# Phase 1 

* What do we simulate:
    * Agricultural sector  
        $\bullet$ **grain farmer, cereal farmer, cattle farmer** TODO see which cereals 
        $\Rightarrow$ next: *pig farmer*, *poultry farmer*,  
        based on most consumed type of animal in Switzerland  
        ["Stats"](#a)
    * Food industry  
        $\bullet$ **wholesaler, packaging compagny**  
        $\bullet$ Cereals: **agricultural cooperative, mills, bakery**  
        $\bullet$ Beef Meat: **slaughtered house, butchery + ?** TODO  
        ["Assumptions"](#b)
    * The food supply chain
        1. Farmers buy *seeds* and *feedstuff* to *wholesaler* and/or *agricultural cooperative*
        2. Cereals are sold to *cooperative* or *wholesaler*
        2. Cows are sent to *slaughetered houses*
        3. Cereals are *stored*, *transformed into flour* on the spot or inside mills
        3. Meat is sent to *butchery*
        4. Cereals are sent to *bakery* to be transformed into *bread*
        5. Meat and cereals are sold to *packaging compagny*
        6. They are ready to be distributed on the market. 
    * **People**  
        $\bullet$ Workers and consumers at the moment, constant and guaranteed salary 
        $\bullet$ Only buy one type of food (bread, steak)
        $\Rightarrow$ next: 
            - variable pay, based on current swiss situation if data available
            - add the origin of food (bio farm, permaculture, ...) to the product sold on market 
            - if data concerning behavior of people regarding food consumption based on their salary + product's origin + meat consumption implement it
    * Lands 
- Mesuring effect of different agriculture types
  - Quality of lands due to type of cereals used, intensity and methods of farming must be taken into account
  - Path cost only depends on road cost 
  - Each action generate a co2 cost, hardcoded as a simple function for the moment
- Decision making of agents:
  - They make the decision they want (random &/or most profitable)
  - Governments make really basis/ no decision on taxes -> change in next phases
- Supplies needed by people
  - People eat cow's meat and bread
  - People either work for an entity, or other, but earn some money -> upgrade in phase 2 by making different salary -> influence choice of meat in supermarket
- Supplies needed for production of commodities
  - Animals only require feedstuff -> change by adding vaccin + maybe other things after
  - Cereals only need seeds
- Need to define a start point where basic supplies are just "generated"
  - ex: vaccin are mades by pharmaceutical laboratory, but this one does not need to import nitrate, alcool, etc.., it just create vaccin
- Basic suppliers are seen as sources -> change this in phase 2 
- Data structure of the world (TODO):
  - Land and roads represented as a network
  - Lands only grows Cereals (superclass of all cereals (wheat, barley, mais etc))
  - Lands are only connected to a road -> phase +, add where the land in connected on the road, to have more efficient travel computation
- How exchange are made between each agents (TODO)
  - Market is a unique place, where we can find all buyers and sellers, but could become separate things (e.g some local markets, big international markets, etc... in phase 2+). BADDDDD
  - Different types of contract ? TODO 


Key word : basics needs for all entity, basic production

# Phase 2 

More complex types of food / animals 
More complex Health system for people (risk of cancers, obesity etc) -> Maybe phase 3 
More complex aliments are proposed. Thoses have a more accurate influence on Person's health 
    Different quality of meats/cereals
More complex lands, influence of quality of ground on production (dry, weet,...)
Where does the ressources of the source come from, model it, make it evolve
More complicated market, prices influence by quantity, amount of work needed to produce the good, possibility to negotiate the price 
Make a more complex productivity for factory, based on nb of avaible ressources


Voir définition basique du market

seeds are produced by seed farmerss 

# References:

# a
(pork(47%)(4% imported), beef(24%)(20% imported), poulty(20%)(44% imported)) [Source](https://2019.agrarbericht.ch/fr/marche/produits-animaux/viande-et-ufs#:~:text=En%202018%2C%20la%20population%20suisse,mais%20%C3%A0%20l'expansion%20d%C3%A9mographique.)

# b
For the moment, we assume that packaging (primary, secondary, tertiary) is made by 1 type of agent