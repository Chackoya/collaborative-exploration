package sim.app.exploration.agents;

import java.util.Hashtable;
import java.util.Vector;

import sim.app.exploration.env.SimEnvironment;
import sim.app.exploration.objects.Prototype;
import sim.app.exploration.objects.SimObject;
import sim.app.exploration.utils.Utils;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;

public class ExplorerAgentOriginal extends ExplorerAgentParent {
	
	protected float INTEREST_THRESHOLD = 65;
	protected final int viewRange = 30;

	// protected boolean GLOBAL_KNOWLEDGE = true;
	// protected int IDENTIFY_TIME = 15;
	
	public ExplorerAgentOriginal(Int2D loc, int agentId) {
		this.loc = loc;
		this.orientation = 0;
		this.target = null;
		this.knownObjects = new Vector<Prototype>();
		this.identifyClock = 0;
		this.agentId = agentId;
	}

	public void step(SimState state) {

		// The explorer sees the neighboring objects and sends them to the
		// mapper
		if (identifyClock == 0) {
			Bag visible = env.getVisibleObjects(loc.x, loc.y, viewRange);

			// -------------------------------------------------------------
			for (int i = 1; i < visible.size(); i++) {
				SimObject obj = (SimObject) visible.get(i);

				if (!mapper.isIdentified(obj.loc)) {
					Hashtable<Class, Double> probs = getProbabilityDist(obj);

					float interest = getObjectInterest(probs);
					//System.out.println("OBJECT AT: (" + obj.loc.x + ","
					//		+ obj.loc.y + "). INTEREST: " + interest);

					// If not interesting enough, classify it to the highest prob
					if (interest < INTEREST_THRESHOLD) {
						Class highest = Utils.getHighestProb(probs);

						mapper.identify(obj, highest);
						Class real = env.identifyObject(obj.loc).getClass();
						//if (highest != real)
						//	System.err.println(real.getSimpleName());
						
						broker.removePointOfInterest(obj.loc);

					} else {
						mapper.addObject(obj);
						broker.addPointOfInterest(obj.loc, interest);

					}
				}

			}
			// --------------------------------------------------------------

			// Check to see if the explorer has reached its target
			if (target != null) {
				if (loc.distance(target) == 0) {
					target = null;

					SimObject obj = env.identifyObject(loc);

					if (obj != null) {
						broker.removePointOfInterest(obj.loc);
						mapper.identify(obj, obj.getClass());
						addPrototype(obj, obj.getClass());

						identifyClock = IDENTIFY_TIME;
					}
				}
			}

			// If the explorer has no target, he has to request a new one from
			// the broker
			if (target == null) {
				target = broker.requestTarget(loc, agentId);
				//System.out.println("NEW TARGET: X: " + target.x + " Y: "
				//		+ target.y);
			}

			// Agent movement
			Double2D step = new Double2D(target.x - loc.x, target.y - loc.y);
			step.limit(STEP);

			loc.x += Math.round(step.x);
			loc.y += Math.round(step.y);

			env.updateLocation(this, loc);
			mapper.updateLocation(this, loc);

			orientation = Math.atan2(Math.round(step.y), Math.round(step.x));
		}
		
		if (identifyClock > 0)
			identifyClock--;
	}

	@Override
	public double orientation2D() {
		return orientation;
	}

	public Int2D getLoc() {
		return loc;
	}

	public double getOrientation() {
		return orientation;
	}

}
