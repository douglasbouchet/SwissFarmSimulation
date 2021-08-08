# The Main phases of the Simulation:
# Phase 1, basic simulation of food supply chain 

* What do we simulate:
    * Agricultural sector  
        $\bullet$ **grain farmer**   
        $\bullet$ **cereal farmer**  TODO see which cereals  
        $\bullet$ **cattle farmer**  
        $\Rightarrow$ next:
        - *pig farmer*, *poultry farmer*,
        based on most consumed type of animal in Switzerland  
        ["Stats"](#a) 
    * Food industry  
        $\bullet$ **wholesaler**  
        $\bullet$ **packaging compagny**  
        $\bullet$ Cereals: **agricultural cooperative**, **mills**, **bakery**  
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
    * Lands  TODO  
        $\bullet$ **arable land**: only represent the surface, and crops type  
        $\bullet$ **other lands**: ?  
        $\Rightarrow$ next: add soil quality (parameters TBD...)  
* Markets:
    * 2 types of market: **derivative**, **on spot**
        * **derivative**: *right (options)/obligation (futures)*
            * Exchange-traded derivative contract
            * Over-the-counter 
        * **on spot**: exchange directly (physical)  

* The food supply chain (bread wheat)
    1. Farmers buy *seeds* and *feedstuff* (only feed wheat + grass atm) to *wholesaler* and/or *agricultural cooperative*
    2. Cereals are sold to *cooperative* or *wholesaler*
    3. Cows are sent to *slaughetered houses*
    4. Cereals are *stored*, *transformed into flour* on the spot or inside *mills*
    5. Meat is sent to *butchery*
    6. Flour is sent to *bakery* to be transformed into *bread*
    7. Meat and bread are sold to *packaging compagny*
    8. They are ready to be distributed on the market.   

* What are the sources ? 
    * Find the start point where basic supplies are just "generated": 
        * ex: vaccin are mades by pharmaceutical laboratory, but this one does not need to import nitrate, alcool, etc.., it just create vaccin
    * Can be seen as factory that does not have any requires  
    - $\bullet$ seeds farmers     
    - $\bullet$ pesticides, fertilizer, vaccins + ?  
    * $\bullet$ agricultural equipment (tractor, cereal bins,...)  

* Add a basic(simple) production of CO2, water & soil pollution, for the food supply chain

* Data structure of lands:

* The relationship network, represented as a graph (undirected ?)
  * Each agent is represented as a node
  * Relation between agents are represented by edge
  * Edges are created by making exchange between the 2 nodes
  * Goal of the relationship network: keep track of interactions, to give choice to agent to pick someone from their network, or find someone on the market
  * $\Rightarrow$ next: edge have different level == importance of relationship
    * level1 = basic level, both nodes can directly talk to each other (stored in their local cache)
    * level2:
      * other node preferred vs market (can be selected randomly with proba)
      * access to other node's network ? 
      * contracts can be made easier TODO see contract part to see how this can influence the contracts ?
    * making exchange increase level, don't make decrease
  
# Phase 2, inclusion of types of agriculture

## Types of agriculture: ["definition"](http://www.riav.fr/quels-sont-les-differents-types-agriculture/)

TBD, possibles types + required and effects needs to be discussed with other people  
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

* This will probably need to adapte the supply needed by farmer
* Will influence productivity & economic

* Measurement of food supply chain on CO2 emission + other pollution (soil quality, water quality ? )
  * **True cost**
  * Need advise of Christian or experts to determine the major factor of pollution emission in food supply chain

# Phase 3: impact of types of agriculture

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
  * Economic impact 
  * Compare it to previous result 
  


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

# enhancement of Koch's code

1. Land should be a class instead of commodity, in order to add a localization, and other data on them like soil quality etc..
2. Factory is good, but how to model the fact that requires are not "required at the same time"
ex: seeds are required in october, but trucks are required in july during harvesting 
Add different type of production line in a farm: 
e.g 
  - production line requiring seeds and produce cereals (not harvested)
  - production line requiring not harveseted cereal, harvester and produce cereals
3. Production line should have a timer based on a global timer to start, i.e if you cannot afford seeds, you cannot start a production of wheat in May, you need to wait until October.
4. add an attribut to production line (optional) which are commodities that are not obligated to run the production line, but increase productivity (ex: fertilizer)

How the agents interacts 

# Contracts (Temp)

Input based contracts, output based contracts

contracts involved incompletness 

Souvent des contrats car monde agricole pas sur de: (possbilité de mauvaises récoltes, périssable donc 
doivent être vite stocké, cycle de production long (mois voir années)). concerne surtout la viande 

Diff: prix de marché vs prix de campagne ??????????????

Vente soit par la coopérative(prix de campagne askip), soit par futur contract (futures market for agriculture interdit en Suisse) (prix, location, quantité dédicé à l'avance) 
sur Euronext pour la Suisse ? obligatoire pour la france donc peut-être pareil.
sinon il existe : Chicago Board of Trade (CBOT),Kansas City Board of Trade (KCBT),Minneaolis Grain Exchange (MGEX) (au USA)


check spot market 


# Questions about implementation

How to reflect the fact that pollution of a farmer affects the land he crops on + water 

ADD water point to Land 