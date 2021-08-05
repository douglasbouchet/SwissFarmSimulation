# The Main phases of the Simulation:
# Phase 1, basic simulation of food supply chain 

* What do we simulate:
    * Agricultural sector  
        $\bullet$ **grain farmer, cereal farmer, cattle farmer** TODO see which cereals  
        $\Rightarrow$ next:
        - *pig farmer*, *poultry farmer*,
        based on most consumed type of animal in Switzerland  
        ["Stats"](#a) 
    * Food industry  
        $\bullet$ **wholesaler, packaging compagny**  
        $\bullet$ Cereals: **agricultural cooperative, mills, bakery**  
        $\bullet$ Beef Meat: **slaughtered house, butchery + ?** TODO  
        ["Assumptions"](#b)
        $\Rightarrow$ next: TBD
    * **People**  
        $\bullet$ Workers and consumers at the moment, constant and guaranteed salary  
        $\bullet$ Only buy one type of food (bread, steak)  
        $\Rightarrow$ next:  
          - variable pay, based on current swiss situation if data available  
          - add the origin of food (bio farm, permaculture, ...) to the product sold on market  
          - if data concerning behavior of people regarding food consumption based on their salary + product's origin + meat consumption implement it
    * Lands  
        $\bullet$ **arable land**: only represent the surface, and crops type
        $\bullet$ **other lands**: ?  
        $\Rightarrow$ next: add soil quality (parameters TBD...)  
    * Market
      * Types of contract: TODO
* The food supply chain  
    1. Farmers buy *seeds* and *feedstuff* to *wholesaler* and/or *agricultural cooperative*
    2. Cereals are sold to *cooperative* or *wholesaler*
    3. Cows are sent to *slaughetered houses*
    4. Cereals are *stored*, *transformed into flour* on the spot or inside mills
    5. Meat is sent to *butchery*
    6. Cereals are sent to *bakery* to be transformed into *bread*
    7. Meat and cereals are sold to *packaging compagny*
    8. They are ready to be distributed on the market. 

Find the start point where basic supplies are just "generated": 
- ex: vaccin are mades by pharmaceutical laboratory, but this one does not need to import nitrate, alcool, etc.., it just create vaccin

$\bullet$ seeds farmers     
$\bullet$ pesticides, fertilizer, vaccins + ?  
$\bullet$ agricultural equipment (tractor, cereal bins,...)  

* Add a basic production of the CO2 for the food supply chain

# Phase 2, inclusion of types of agriculture

## Types of agriculture: ["definition"](http://www.riav.fr/quels-sont-les-differents-types-agriculture/)
- Conventional agriculture
- Organic farming
- Sustainable agriculture
- Integrated agriculture
- And the use of farming's product as: fertilizer, pesticide, vaccin, nutrients, ...

* Each farmer get assigned a type of agriculture:
  * based on real data if available
  * or based on percentage of each type of production if available, randomly assign to farmers
  * or randomly assign 
  * Goal is to have the current situation of swiss agriculture, in order to compare it with phase 3

* Measurement of food supply chain on CO2 emission + other pollution (soil quality, water quality ? )
  * **True cost**
  * Need advise of Christian or experts to determine the major factor of pollution emission in food supply chain

# Phase 3, impacts of types of agriculture

* Farmers can change method of production:
  *  can buy fertilizer, pesticide, vaccin, nutrients
  *  change the type of agriculture 
  *  consequences: buy new land, hire people, higher cost, less productivity

* Farmers can change what they produced: 
  * based on global recomendation of supplies needed (if data available)
  * randomly
  * Can get help from the government, or associations
  
Goal is to play with differents scenarios and see the total CO2 + other pollution emitted by thoses, and compare it to scenario of **phase 2**

* Mesuring effect of different agriculture types
  * Effects on soil quality, pollution due to type of cereals used, intensity and methods of farming
  * Compare it to previous result 
  
  
- Data structure of the world (TODO):
  - Land and roads represented as a network
  - Lands only grows Cereals (superclass of all cereals (wheat, barley, mais etc))
  - More complex lands, influence of quality of ground on production (dry, weet,...)
  - Lands are only connected to a road -> phase +, add where the land in connected on the road, to have more efficient travel computation
- How exchange are made between each agents (TODO)
  - Market is a unique place, where we can find all buyers and sellers, but could become separate things (e.g some local markets, big international markets, etc... in phase 2+). BADDDDD
  - Different types of contract ? TODO 


More complicated market, prices influence by quantity, amount of work needed to produce the good, possibility to negotiate the price 


Voir définition basique du market

seeds are produced by seed farmerss 

# References:

# a
(pork(47%)(4% imported), beef(24%)(20% imported), poulty(20%)(44% imported)) [Source](https://2019.agrarbericht.ch/fr/marche/produits-animaux/viande-et-ufs#:~:text=En%202018%2C%20la%20population%20suisse,mais%20%C3%A0%20l'expansion%20d%C3%A9mographique.)

# b
For the moment, we assume that packaging (primary, secondary, tertiary) is made by 1 type of agent


# Questions 

1. What are the different types of agriculture that we can use to cultivate wheat ?

2. On which aspect do we need to focus on to measure the main impact of pollution (i.e CO2 emission, soil quality, water pollution, ...) to see the impact of type of agriculture used ?
 
3. What are the main factors that must be taken into account when measuring soil quality, and how they are influenced by the agriculture (effect of intensive agriculture, monocrop, fertilizer, pesticide,..)

4. What are the main products used in agriculture and animal breeding to increase the production (with or without respect of the environment) i.e fertilizer, pesticide, vaccins, nutrients, ... ? 

5. For the products give in previous question: what are their impact on aspect of **question 3** ?

6. What are the changes in term of requires(more work, more products,...) compared to the production(ton per ha) induced by a change of type of agriculture.
I.e what is the cost of changing agriculture's type (only financial cost could be taken into account but if possible we can use the True cost method proposed by Christian in order to see the real difference by taking into account durability of thoses changes). Easier if you want to convince the governement or people that changing your agriculture type can be worth, even if your productivity has decreased.