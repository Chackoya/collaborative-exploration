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
	protected final int viewRange = 40;

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
}
