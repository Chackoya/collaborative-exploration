package sim.app.exploration.agents;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.util.Vector;

import sim.app.exploration.objects.Prototype;
import sim.app.exploration.objects.SimObject;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.Int2D;

public class MapperAgent {

	public SparseGrid2D knownWorld;
	public Class[][] identifiedObjects;
	public Class[][] stronglyIdentifiedObjects;
	public Vector<Prototype> knownObjects;
	public Vector<Int2D> identifiedLocations;
	
	public MapperAgent(int width, int height){
		knownWorld = new SparseGrid2D(width, height);
		identifiedObjects = new Class[width][height];
		stronglyIdentifiedObjects = new Class[width][height];
		identifiedLocations = new Vector<Int2D>();
		this.knownObjects = new Vector<Prototype>();
	}
	
	/**
	 * Adds a series of objects to the known world, checking if they are already 
	 * mapped or not
	 * @param visible
	 */
	public void addVisibleObjects(Bag visible) {
		System.out.println("AddVisibleObjects");

		for(Object o : visible){
			// If the object is not known to the world
			if(knownWorld.getObjectLocation(o) == null){
				
				SimObject s = (SimObject) o;
				knownWorld.setObjectLocation(s, s.getLoc().x, s.getLoc().y);
				
			}
		}
	}

	public void updateLocation(ExplorerAgentParent agent, Int2D loc) {
		
		knownWorld.setObjectLocation(agent,loc);
	}

	public boolean isIdentified(Int2D loc) {
		
		return identifiedObjects[loc.x][loc.y] != null;
	}

	public void identify(SimObject obj, Class highest, boolean stronglyIdentified) {
		
		//System.out.println("IDENTIFYING OBJ AT (" + obj.loc.x + "," + obj.loc.y + ") AS " + highest.getName());
		
		Int2D loc = obj.loc;
		
		identifiedObjects[loc.x][loc.y] = highest;
		identifiedLocations.add(loc);
		
		if (stronglyIdentified) {
			stronglyIdentifiedObjects[loc.x][loc.y] = highest;
		}
	
		Class [] params = {Int2D.class, Color.class, double.class};
		Object[] args = {obj.loc, obj.color, obj.size};
		
		if(highest.isInstance(obj)){
			this.addObject(obj);
			
		}else{
			try{
				Constructor c = highest.getConstructor(params);
				SimObject newObj = (SimObject) c.newInstance(args);
				this.addObject(newObj);
				
			}catch (Exception e){
				System.err.println("No such constructor, please give up on life.");
			}
		}

	}

	public void addObject(SimObject obj) {
		Int2D loc = obj.loc;
		
		Bag temp = knownWorld.getObjectsAtLocation(loc.x, loc.y);
		
		if(temp != null){
			Bag here = new Bag(temp);
			
			for(Object o : here){
				if(! (o instanceof ExplorerAgentParent) ){
					knownWorld.remove(o);
				}
			}
		}
		
		knownWorld.setObjectLocation(obj, loc);
		
	}

	public void deIdentify(SimObject obj) {
		identifiedObjects[obj.loc.x][obj.loc.y] = null;
		identifiedLocations.remove(obj.loc);
	}
	
	public boolean isObjectWhatItLooksLike(SimObject object, Class highest) {
		
		Class obj = identifiedObjects[object.loc.x][object.loc.y];
		Class strongObj = stronglyIdentifiedObjects[object.loc.x][object.loc.y];
		
		String original = obj.getSimpleName();
		String predict = highest.getSimpleName();

		if (!original.equals(predict) && strongObj == null) {
			return false;
		}
				
		return true;
	}
	
	public void addPrototype(SimObject obj, Class class1) {
		for(Prototype p : this.knownObjects){
			if(class1 == p.thisClass){
				p.addOccurrence(obj.size, obj.color);
				return;
			}
		}
		
		this.knownObjects.add(new Prototype(class1, obj.size, obj.color));
		
	}
}
