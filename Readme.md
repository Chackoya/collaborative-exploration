# Collaborative Exploration of Unknown Environments with a Team of Agents

This repository was used for the Artificial Intelligence course at the University of Coimbra. The contributors are
* Jani Hilliaho
* Sebastian Rehfeldt
* Miguel Tavares (author of the initial system)
* Pedro Gaspar (author of the initial system)

## Goal

During this project, a new approach for exploring unknown environments was studied. This approach is based on the idea that object classes could be predicted over range instead of walking there and using a (longer) full examination. This might be the case when objects are not interesting enough to go there.

Different strategies have been created and compared. The results can be seen in the paper.

## Installation (using Eclipse)

* Install Mason (http://cs.gmu.edu/~eclab/projects/mason/)
* Pull repository
* Create new Java project
* Link exploration folder as source folder
* Add external jars for Mason (Libraries tab)
* Run viewer
* Cross fingers and enjoy!

## Run options

* Change to old classification in the ExplorerAgentParent by changing the boolean value oldCorrelation
* Change agent types and amounts in SimEnvironment by adjusting the ExplorerAmounts list
* Change map type and starting point in SimEnvironment
* Change clustering in BrokerAgent by USE_CLUSTERING
* Change reclassification in ExplorerAgentParent by USE_RECLASSIFICATION
