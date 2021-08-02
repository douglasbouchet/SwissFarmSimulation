# The Main phases of the Simulation:
# Phase 1  

Agents are not obligated, they make the decision they want (random & most profitable)
Path cost only depends on road cost 
Each action generate a co2 cost, hardcoded as a simple function for the moment 
People eat Meat and cereals, and if they don't they die -> upgraded in phase 2
People either work for an entity, or other, but earn some money -> upgrade in phase 2 by making different salary -> influence choice of meat in supermarket
Basic suppliers are seen as sources -> change this in phase 2 
Governments make really basis/ no decision on taxes -> change in next phases
Animals only require feedstuff -> change by adding vaccin + maybe other things after
Cereals only need seeds + people to work on
On/Off productivity, if all supplies to available, prod = 0, else prod = 100%
Land and roads represented as a network
Lands only grows Cereals (superclass of all cereals (wheat, barley, mais etc))
Only cows. 
Market is a unique place, where we can find all buyers and sellers, but could become separate things 
(e.g some local markets, big international markets, etc... in phase 2+). 
Lands are only connected to a road -> phase +, add where the land in connected on the road, to have more efficient travel computation


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


//TODO see if we don't merge the trader and the owner into a single trait 

-> Yes, we merge it into a single entity, we will add later the opportunity for some people to trade goods, without possesing ones. -> Trader should rely on a stock, that will be override by thoses types of trader (by using all the market as a stock instead of a personnal stock)


This afternoon/demain: Voir comment intégrer la factory dans l'ensemble,
Voir définition basique du market