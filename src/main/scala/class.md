# Agriculture sector

## Grain farmer
produce seeds 
- extends source(Sim0(Seller(Owner), Sim)) 
No other things to add

## Cattle farmer
produce beef's meat 
- extends Factory, Sim0(Seller(extends Owner) and Sim) 


## cereal farmer 
Factory

# Food Industry

## Wholesaler
trader

## Packaging compagny 
factory

## Agriculure cooperativ
factory (required mill for example) otw Trader (stocks cereals and sell them to mill, or buy seeds)

## Mills
factory

## Bakery
factory

## Slaughtered House
factory

# Butchery
factory

# People

# Markets

# Lands 


## Derivative Market

## On Spot Market

# Lands 

# The market:

market = List(seller) per commodity 
-> This can be used for the future/options and on spot market (implement 2 different markets)
We also need to add the relationship network, in order to exchange directly with usual seller/buyer(like cooperative or wholesalers) or pass by market 