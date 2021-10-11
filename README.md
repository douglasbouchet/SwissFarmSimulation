# economic_simulations

## Packages explanation

### FarmRelated

This package contains classes for the farmers, and their way to produce ressources
For the moment, farmers produces commodities by using Crops instance and Herd instance

### Compagnies

This package represents all the agents that are between the farmers and the consumers. 
They are mainly factories, which buy some commodities and use them to create/transform new commodities

In a first Milestone, this intermediate agents are:
- Mills
- Bakery
- Supermarkets

We assume that consumers buy commodities only from supermarkets, but this will evolve in further milestones

### generation

This package is in charge of generating some data (lands, farmers, companies, consumers)

### geography

Manage all the aspect related to the geography:
- The road network
- The lands (manages by the LandAdministrator)

### market 

This will probably change by only one agent that will compute some domestic prices and (further) some world prices