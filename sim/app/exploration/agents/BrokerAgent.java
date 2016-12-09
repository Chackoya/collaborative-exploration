package sim.app.exploration.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.text.html.HTMLDocument.Iterator;

import sim.app.exploration.core.Simulator;
import sim.app.exploration.objects.SimObject;
import sim.util.Bag;
import sim.util.Int2D;

public class BrokerAgent {
	
	private ArrayList<PointOfInterest> pointsOfInterest;
	private ArrayList<PointOfInterest> removedPoIs;
	private ExploringAreas ExplorableAreas;
	private HashMap<Integer, Int2D> agentLocations = new HashMap<Integer, Int2D>();
	
	public BrokerAgent() {
		this.pointsOfInterest = new ArrayList<PointOfInterest>();
		this.removedPoIs = new ArrayList<PointOfInterest>();
		// Change this to change the clustering method
		this.ExplorableAreas = new ExploringRectangles();
	}
	
	public Int2D requestTarget(Int2D agentPos, int agentId) {
		if (agentLocations.get(agentId) == null) {
			System.out.println("Agent " + agentId + " asking for first target");
		}
		agentLocations.put(agentId, agentPos);
		ExplorableAreas.createAreasFromPoIs(pointsOfInterest, agentLocations);
		
		Int2D target = null;
		PointOfInterest target_PoI = null;
		
		// If we have no points of interest, return a random point
		if (pointsOfInterest.size() == 0)
			return getRandomTarget();
			//return getLimitedRandomTarget(agentPos);
		
		// Else, find the best point of Interest
		else {
			
			double bestScore = Double.NEGATIVE_INFINITY;
			double score;
			
			for (PointOfInterest PoI : pointsOfInterest) {
				score = PoI.interestMeasure - ( (agentPos.distance(PoI.loc) * 100) / Simulator.limitRadius);
				
				//System.out.println("[Broker] Score for " + PoI + ": " + score);
				
				boolean insideStripe = false;
				if (ExplorableAreas != null && ExplorableAreas.getAreaCount() > 0 && ExplorableAreas.getArea(agentId) != null) {
					insideStripe = ExplorableAreas.getArea(agentId).locInsideArea(PoI.loc);					
				}
				
				if (score > bestScore && insideStripe) {
					bestScore = score;
					target = PoI.loc;
					target_PoI = PoI;
					//System.out.println("Found poi " + PoI.loc.x + "," + PoI.loc.y + " for agent " + agentId + " at " + agentPos.x + "," + agentPos.y + ". Stripe range: " + ExplorableAreas.agentAreas.get(agentId).leftBorder + "-" + ExplorableAreas.agentAreas.get(agentId).rightBorder);
				}
			}
			
			// If the target is too far, send a random target
			if (bestScore < 0) {
				System.out.println("Found random target for agent " + agentId + " at " + agentPos.x + "," + agentPos.y);
				return getLimitedRandomTarget(agentPos, agentId);				
			}
			
			// Remove the target from the list of Points of Interest and add it to the removed list (this should be done when you arrive at the point if you're constantly calculating new targets)
			if (target_PoI != null) {
				pointsOfInterest.remove(target_PoI);
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
	}
	
	public void removePointOfInterest(Int2D loc) {
			PointOfInterest tmp = new PointOfInterest(loc, 1);
			
			if (pointsOfInterest.contains(tmp)) {
				//System.out.println("[Broker] Removing " + loc + " ("+ pointsOfInterest.size() + ")");
				pointsOfInterest.remove(tmp);
				//System.out.println("[Broker] Now with " + pointsOfInterest.size());
				
				removedPoIs.add(tmp);
			}
		}
	
	public Int2D getLimitedRandomTarget(Int2D agentPos, int agentId) {
		Int2D target = null;
		
		while (true) {
			target = getRandomTarget();
			System.out.println("Trying random target " + target.x + "," + target.y + " for agent " + agentId + " in " + agentPos.x + "," + agentPos.y);

			boolean insideStripe = false;
			boolean stripesAvailable = false;
			if (ExplorableAreas != null && ExplorableAreas.getAreaCount() > 0 && ExplorableAreas.getArea(agentId) != null) {
				insideStripe = ExplorableAreas.getArea(agentId).locInsideArea(target);
				stripesAvailable = true;
			}
			
			if ((agentPos.distance(target) <= Simulator.limitRadius && !stripesAvailable) || insideStripe)
				break;
		}
		
		return target; 
	}
	
	public Int2D getRandomTarget() {
		return new Int2D((int)(Math.random()*Simulator.WIDTH), (int)(Math.random()*Simulator.HEIGHT)); 
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


// HELPING CLASSES IMPLEMENTED DURING THE AI ASSIGNMENT

// Interfaces for clustering
interface ExploringArea {
	public boolean locInsideArea(Int2D loc);
};

interface ExploringAreas {
	void createAreasFromPoIs(ArrayList<PointOfInterest> pointsOfInterest, HashMap<Integer, Int2D> agentLocations);
	ExploringArea getArea(int id);
	int getAreaCount();
};


class ExploringRectangle implements ExploringArea{
	private Int2D upperLeft;
	private Int2D bottomRight;
	
	ExploringRectangle(Int2D upleft, Int2D botright) {
		this.upperLeft = upleft;
		this.bottomRight = botright;
	}
	
	public boolean locInsideArea(Int2D loc) {
		if (loc.x <= this.bottomRight.x && loc.x >= this.upperLeft.x && loc.y <= this.bottomRight.y && loc.y >= this.upperLeft.y) {
			return true;
		} else {
			return false;
		}
	}
}

class ExploringRectangles implements ExploringAreas{
	public Map<Integer, ExploringRectangle> agentAreas;
	private int creationCounter = -1;
	
	private int minimumX = 9999;
	private int maximumX = 0;
	private int minimumY = 9999;
	private int maximumY = 0;
	
	private int lastXRange = 0;
	private int lastYRange = 0;
	
	ExploringRectangles() {
		agentAreas = new HashMap<Integer, ExploringRectangle>();
	}
	
	public ExploringArea getArea(int id) {
		return agentAreas.get(id);
	}
	
	public int getAreaCount() {
		return agentAreas.size();
	}
	
	public void createAreasFromPoIs(ArrayList<PointOfInterest> pointsOfInterest, HashMap<Integer, Int2D> agentLocations) {
		if (agentLocations.size() < 2) {return;}
		if (creationCounter >= 0 && creationCounter < 10) {creationCounter++; return;}
		creationCounter = 0;
		
		int maxX = 0;
		int minX = 9999;

		int maxY = 0;
		int minY = 9999;
		
		for (int i = 0; i < pointsOfInterest.size(); ++i) {
			int x = pointsOfInterest.get(i).loc.x;
			if (x < minX) {minX = x;} else if (x > maxX) {maxX = x;}
			int y = pointsOfInterest.get(i).loc.y;
			if (y < minY) {minY = y;} else if (y > maxY) {maxY = y;}
		}
		
		if (minX < minimumX) minimumX = minX;
		if (maxX > maximumX) maximumX = maxX;
		if (minY < minimumY) minimumY = minY;
		if (maxY > maximumY) maximumY = maxY;
		
		int xRange = maximumX - minimumX;
		int yRange = maximumY - minimumY;

		if (lastXRange == xRange && lastYRange == yRange) {
			return;
		}
	
		lastXRange = xRange;
		lastYRange = yRange;
		
		int agentCount = agentLocations.size();
		System.out.println("Creating rectangle cluster with " + agentCount + " agents");
		double rows;
		double cols;
		
		// 2,3,4,6,8,9
		if (agentCount >= 12) {
			rows = 3;
			cols = 4;
		} else if (agentCount >= 9) {
			rows = 3;
			cols = 3;
		} else if (agentCount >= 8) {
			rows = 2;
			cols = 4;
		} else if (agentCount >= 6) {
			rows = 2;
			cols = 3;
		} else if (agentCount >= 4) {
			rows = 2;
			cols = 2;
		} else if (agentCount >= 3) {
			rows = 1;
			cols = 3;
		} else {
			rows = 1;
			cols = 2;
		}
		
		double areaWidth = (xRange / cols) + 1;
		double areaHeight = (yRange / rows) + 1;

		double counter = 0;
		
		for (Map.Entry<Integer, Int2D> entry : agentLocations.entrySet()) {
			double row = Math.floor(counter / cols);
			double col = counter%cols;
			
			if (row < rows) {
				int ulx = (int) Math.round(minimumX + col * areaWidth);
				int uly = (int) Math.round(minimumY + row * areaHeight);

				int brx = (int) Math.round(minimumX + (col + 1) * areaWidth);
				int bry = (int) Math.round(minimumY + (row + 1) * areaHeight);
				
				Int2D ulc = new Int2D(ulx, uly);
				Int2D brc = new Int2D(brx, bry);

				System.out.println("Rows: " + rows + " Cols: " + cols + " Counter: " + counter + " Row: " + row + " Col: " + col);

				agentAreas.put(entry.getKey(), new ExploringRectangle(ulc,brc));
			} else {
				int ulx = (int) Math.round(0);
				int uly = (int) Math.round(0);

				int brx = (int) Math.round(Simulator.WIDTH);
				int bry = (int) Math.round(Simulator.HEIGHT);
				
				Int2D ulc = new Int2D(ulx, uly);
				Int2D brc = new Int2D(brx, bry);

				System.out.println("Rows: " + rows + " Cols: " + cols + " Counter: " + counter + " Row: " + row + " Col: " + col + " having a full map to explore");
				agentAreas.put(entry.getKey(), new ExploringRectangle(ulc,brc));
			}
			counter++;
		}	
	}
}




