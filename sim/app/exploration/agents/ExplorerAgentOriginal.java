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
	
	// protected static final long serialVersionUID = 1L;
	//protected float INTEREST_THRESHOLD = 75;
	// protected final double STEP = Math.sqrt(2);
	protected final int viewRange = 30;
	
	// protected int identifyClock;

	// protected Int2D loc;
	// protected Int2D target;
	// protected double orientation;

	// public SimEnvironment env;
	// public BrokerAgent broker;
	// public MapperAgent mapper;
	// protected Vector<Prototype> knownObjects;

	// protected boolean GLOBAL_KNOWLEDGE = true;
	// protected int IDENTIFY_TIME = 30;
	
	public ExplorerAgentOriginal(Int2D loc) {
		this.loc = loc;
		this.orientation = 0;
		this.target = null;
		this.knownObjects = new Vector<Prototype>();
		this.identifyClock = 0;
	}



}
