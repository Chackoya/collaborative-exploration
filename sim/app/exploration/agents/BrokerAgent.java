package sim.app.exploration.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import sim.app.exploration.core.Simulator;
import sim.app.exploration.objects.SimObject;
import sim.util.Bag;
import sim.util.Int2D;

public class BrokerAgent {
	
	private ArrayList<PointOfInterest> pointsOfInterest;
	private ArrayList<PointOfInterest> removedPoIs;
	private Map<Integer, Int2D> agentLocations;
	private Map<Integer, ArrayList<PointOfInterest> > agentPoIs = new HashMap<Integer, ArrayList<PointOfInterest> >();
	
	public BrokerAgent() {
		this.pointsOfInterest = new ArrayList<PointOfInterest>();
		this.removedPoIs = new ArrayList<PointOfInterest>();
		this.agentLocations = new HashMap<Integer, Int2D>();
	    agentPoIs = new HashMap<Integer, ArrayList<PointOfInterest> >();
	}
	
	public Int2D requestTarget(Int2D agentPos, int agentId) {
		agentLocations.put(agentId, agentPos);	
		
		Int2D target = null;
		PointOfInterest target_PoI = null;
		
		// If we have no points of interest, return a random point
		if (agentPoIs.get(agentId) == null || agentPoIs.get(agentId).size() == 0)
			return getRandomTarget();
			//return getLimitedRandomTarget(agentPos);
		
		// Else, find the best point of Interest
		else {
			
			double bestScore = Double.NEGATIVE_INFINITY;
			double score;
			
			for (PointOfInterest PoI : agentPoIs.get(agentId)) {
				score = PoI.interestMeasure - ( (agentPos.distance(PoI.loc) * 100) / Simulator.limitRadius);
				
				//System.out.println("[Broker] Score for " + PoI + ": " + score);
				
				if (score > bestScore) {
					bestScore = score;
					target = PoI.loc;
					target_PoI = PoI;
				}
			}
			
			// If the target is too far, send a random target
			if (bestScore < 0)
				return getLimitedRandomTarget(agentPos);
			
			// Remove the target from the list of Points of Interest and add it to the removed list (this should be done when you arrive at the point if you're constantly calculating new targets)
			if (target_PoI != null) {
				agentPoIs.get(agentId).remove(target_PoI);
				removedPoIs.add(target_PoI);
			}
			
			//System.out.println("[Broker] Best score: " + bestScore);
			//System.out.println("[Broker] Target: " + target);
		}
		
		return target;
	}
	
	public void addPointOfInterest(Int2D point, double interestMeasure) {
		PointOfInterest PoI = new PointOfInterest(point, interestMeasure);
		if (!pointsOfInterest.contains(PoI) && !removedPoIs.contains(PoI)) {
			pointsOfInterest.add(PoI);
			//System.out.println("[Broker] PoI added: " + PoI.loc);
		}
		divideInterestPointLocationsToAgents();
	}
	
	public void removePointOfInterest(Int2D loc) {
		PointOfInterest tmp = new PointOfInterest(loc, 1);
		for (int k = 0; k < this.agentPoIs.size(); ++k) {
			if (agentPoIs.get(k).contains(tmp)) {
				//System.out.println("[Broker] Removing " + loc + " ("+ pointsOfInterest.size() + ")");
				agentPoIs.get(k).remove(tmp);
				//System.out.println("[Broker] Now with " + pointsOfInterest.size());
			}
		}
	}
	
	public Int2D getLimitedRandomTarget(Int2D agentPos) {
		Int2D target = null;
		
		while (true) {
			target = getRandomTarget();
			if (agentPos.distance(target) <= Simulator.limitRadius)
				break;
		}
		
		return target; 
	}
	
	public Int2D getRandomTarget() {
		return new Int2D((int)(Math.random()*Simulator.WIDTH), (int)(Math.random()*Simulator.HEIGHT)); 
	}

	public void divideInterestPointLocationsToAgents() {
		//System.out.println("divideInterestPointLocationsToAgents");
		//System.out.println(this.pointsOfInterest.size() + " PoIs");
		//System.out.println(this.agentLocations.size() + " agents");
		
		if (this.agentLocations.size() > 0 && this.pointsOfInterest.size() > 0) {
			for (int i = 0; i < this.pointsOfInterest.size(); ++i) {
				Int2D poiLoc = this.pointsOfInterest.get(i).loc;
				int nearestAgent = 0;
				double nearestDistance = 9999;
				for (int k = 0; k < this.agentLocations.size(); ++k) {
					if (agentPoIs.get(k) == null) {
						agentPoIs.put(k, new ArrayList<PointOfInterest>());
					}
					int agentId = k;
					Int2D agentLoc = this.agentLocations.get(k);
					double agentDistance = Math.sqrt( (agentLoc.x - poiLoc.x)^2 + (agentLoc.y - poiLoc.y)^2 );
					if (agentDistance < nearestDistance) {
						nearestDistance = agentDistance;
						nearestAgent = k;
					}
				}
				agentPoIs.get(nearestAgent).add(this.pointsOfInterest.get(i));
				removedPoIs.add(this.pointsOfInterest.get(i));
				this.pointsOfInterest.remove(i);
			}
			for (int i = 0; i < agentPoIs.size(); ++i) {
				System.out.println("Agent " + i + " pois amount: " + agentPoIs.get(i).size());
			}
		}
	}
}

class PointOfInterest {
	public Int2D loc;
	public double interestMeasure;	// I expect this to be in [0, 100]
	
	PointOfInterest(Int2D loc, double interestMeasure) {
		this.loc = loc;
		this.interestMeasure = interestMeasure;
	}
	
	@Override
	public boolean equals(Object o_PoI) {
		PointOfInterest PoI = (PointOfInterest) o_PoI;
		return this.loc.equals(PoI.loc);
	}
	
	public String toString() {
		return "[" + loc.x + ", " + loc.y + " - " + interestMeasure + "]";
	}
}
