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
import sim.util.WordWrap;

public class ExplorerAgentParent implements sim.portrayal.Oriented2D {

	protected static final long serialVersionUID = 1L;
	protected final double STEP = Math.sqrt(2);
	protected final int viewRange = 40;
	
	protected int identifyClock;

	protected Int2D loc;
	protected Int2D target;
	protected double orientation;

	public SimEnvironment env;
	public BrokerAgent broker;
	public MapperAgent mapper;
	protected Vector<Prototype> knownObjects;
	protected int agentId;
	
	public final double INTEREST_THRESHOLD_MAX = 80;
	public double INTEREST_THRESHOLD_STEPS;
	public double KNN_START;
	public double INTEREST_THRESHOLD_INITIAL;
	public double INTEREST_THRESHOLD;

	protected boolean GLOBAL_KNOWLEDGE = true;
	protected int IDENTIFY_TIME = 15;
	protected boolean addedToBroker = false;
	protected boolean USE_RECLASSIFICATION = true;
	protected int RECLASSIFY_MAX_AMOUNT = 4;
	
	public void step(SimState state) {
		if (!addedToBroker) {
			addedToBroker = true;
			broker.addAgentInfo(new AgentInfo(agentId, this.getClass().getSimpleName(), loc));
		}
		int reclassifyCounter = 0;

		// The explorer sees the neighboring objects and sends them to the
		// mapper
		if (identifyClock == 0) {
			Bag visible = env.getVisibleObjects(loc.x, loc.y, viewRange);
			boolean agentPoIStatus = (this.broker.getAgentInfo(agentId).targetType == "poi");

			// -------------------------------------------------------------
			for (int i = 1; i < visible.size(); i++) {
				SimObject obj = (SimObject) visible.get(i);
				
				if (!mapper.isIdentified(obj.loc)) {
					Hashtable<Class, Double> probs = getProbabilityDist(obj);
					
					float interest = getObjectInterest(probs);

					// If not interesting enough, classify it to the highest prob
					if (interest < INTEREST_THRESHOLD) {
						Class highest = Utils.getHighestProb(probs);
						mapper.identify(obj, highest, false);
						Class real = env.identifyObject(obj.loc).getClass();
						//if (highest != real)
							//System.err.println(real.getSimpleName());
						
						broker.removePointOfInterest(obj.loc);

					} else {
						mapper.addObject(obj);
						broker.addPointOfInterest(obj.loc, interest);
					}
				}
				// Else if ... reclassify object if it isn't what it looks like
				else if (USE_RECLASSIFICATION && this.getClass().getSimpleName().equals("ExplorerAgentExplorer") && !agentPoIStatus && obj != null && reclassifyCounter < RECLASSIFY_MAX_AMOUNT && Math.random() > 0.9) {
					Hashtable<Class, Double> probs = getProbabilityDist(obj);
					Class highest = Utils.getHighestProb(probs);					
					if (!mapper.isObjectWhatItLooksLike(obj, highest)) {					
						mapper.deIdentify(obj);
						float interest = getObjectInterest(probs);
						broker.reAddPointOfInterest(obj.loc, interest);
						reclassifyCounter++;
						target = null;
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
						mapper.identify(obj, obj.getClass(), true);
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

	protected int getObjectInterest(Hashtable<Class, Double> probs) {
		double unknownInterest = 0;
		double entropyInterest;
		Vector<Double> prob = new Vector<Double>();

		for (Class c : probs.keySet()) {
			if (c == SimObject.class)
				unknownInterest = Utils.interestFunction(probs.get(c));

			prob.add(probs.get(c));
		}

		//entropyInterest = Utils.entropy(prob);
		entropyInterest = Utils.entropy2(prob,probs.size());

		//System.out.println("ENTROPY: " + entropyInterest + " | UNKNOWN: "
		//		+ unknownInterest);

		double interest = (entropyInterest > unknownInterest ? entropyInterest : unknownInterest) * 100;

		return (int) Math.round(interest);
	}

	protected void addPrototype(SimObject obj, Class class1) {
		// TODO Auto-generated method stub

		// Using the global team knowledge
		if (GLOBAL_KNOWLEDGE) {

			mapper.addPrototype(obj, class1);

			// Using only the agent's knowledge
		} else {
			for (Prototype p : this.knownObjects) {
				if (class1 == p.thisClass) {
					p.addOccurrence(obj.size, obj.color);
					return;
				}
			}

			this.knownObjects.add(new Prototype(class1, obj.size, obj.color));
		}

	}

	protected Hashtable<Class, Double> getProbabilityDist(SimObject obj) {

		Hashtable<Class, Double> probs = new Hashtable<Class, Double>();

		// TODO: Implement global knowledge

		Vector<Prototype> prototypes;
		if (GLOBAL_KNOWLEDGE) {
			prototypes = mapper.knownObjects;
		} else {
			prototypes = this.knownObjects;
		}
		int nClasses = prototypes.size();
		double unknownCorr = 0;
		double corrSum = 0;

		
		//change setup
		Boolean oldCorrelation = false;
		Boolean weighted = false;
		int outOfQuantile = 0;
		
		if(oldCorrelation){
			INTEREST_THRESHOLD = 65;
			for (Prototype prot : prototypes) {
				// TODO: Stuff here
				double corr;
				double colorDist = Utils.colorDistance(obj.color, prot.color);
				double sizeDist = Math.abs(obj.size - prot.size) / Utils.MAX_SIZE;

				// Correlation
				corr = 1 - (0.5 * colorDist + 0.5 * sizeDist);
				// Saturation
				corr = Utils.saturate(corr, prot.nOccurrs);

				probs.put(prot.thisClass, corr*corr*corr);
				corrSum += corr*corr*corr;

				unknownCorr += (1 - corr) / nClasses;
			}
		}
		else{
			for (Prototype prot : prototypes) {
				// TODO: Stuff here
				double corr1,corr2,corr3,corr;
				double colorDistMedian = Utils.colorDistance(obj.color, 
														prot.getRedQuantile(new Float(0.5)),
														prot.getGreenQuantile(new Float(0.5)),
														prot.getBlueQuantile(new Float(0.5)));
				double sizeDistMedian = Math.abs(obj.size - prot.getSizeQuantile(new Float(0.5))) / Utils.MAX_SIZE;
				
				double colorDistUpper = Utils.colorDistance(obj.color, 
						prot.getRedQuantile(new Float(0.75)),
						prot.getGreenQuantile(new Float(0.75)),
						prot.getBlueQuantile(new Float(0.75)));
				double sizeDistUpper = Math.abs(obj.size - prot.getSizeQuantile(new Float(0.75))) / Utils.MAX_SIZE;
				
				double colorDistLower = Utils.colorDistance(obj.color, 
						prot.getRedQuantile(new Float(0.25)),
						prot.getGreenQuantile(new Float(0.25)),
						prot.getBlueQuantile(new Float(0.25)));
				double sizeDistLower = Math.abs(obj.size - prot.getSizeQuantile(new Float(0.25))) / Utils.MAX_SIZE;
				
				// Correlation and saturation
				corr1 = 1 - (0.5 * colorDistMedian + 0.5 * sizeDistMedian);
				corr1 = Utils.saturate(corr1, prot.nOccurrs);

				corr2 = 1 - (0.5 * colorDistLower + 0.5 * sizeDistLower);
				corr2 = Utils.saturate(corr2, prot.nOccurrs);

				corr3 = 1 - (0.5 * colorDistUpper + 0.5 * sizeDistUpper);
				corr3 = Utils.saturate(corr3, prot.nOccurrs);
				
				corr = (corr1+corr2+corr3)/3;
				
				Class[][] identifiedObjects = mapper.identifiedObjects;
				int k=Math.min(prot.nOccurrs, 10);
				if(nClasses>=2 && prot.nOccurrs>KNN_START){
					double knnCor = Utils.getKNN(identifiedObjects,prot,mapper,env,k,obj);

					INTEREST_THRESHOLD = Math.min(INTEREST_THRESHOLD_MAX, 
							INTEREST_THRESHOLD_INITIAL+((INTEREST_THRESHOLD_MAX - INTEREST_THRESHOLD_INITIAL)/INTEREST_THRESHOLD_STEPS)*(prot.nOccurrs-KNN_START));
					
					if(weighted && prot.nOccurrs>= KNN_START && prot.nOccurrs<= (KNN_START+INTEREST_THRESHOLD_STEPS/2))
						corr = 0.9*knnCor + 0.1*corr;
					else
						corr = knnCor;
				}
				
				probs.put(prot.thisClass, corr);
				corrSum += corr;

				unknownCorr += (1 - corr) / nClasses;
				
				if((obj.size > prot.getSizeQuantile(new Float(0.75)) || obj.size < prot.getSizeQuantile(new Float(0.25))) || 
					((obj.color.getRed() > prot.getRedQuantile(new Float(0.75)) || obj.color.getRed() < prot.getRedQuantile(new Float(0.25))) &&
					(obj.color.getGreen() > prot.getGreenQuantile(new Float(0.75)) || obj.color.getGreen() < prot.getGreenQuantile(new Float(0.25))) &&
					(obj.color.getBlue() > prot.getBlueQuantile(new Float(0.75)) || obj.color.getBlue() < prot.getBlueQuantile(new Float(0.25))))  ){
					outOfQuantile++;
				}
			}
		}
		

		if (nClasses == 0)
			unknownCorr = 1.0;
		if(outOfQuantile == nClasses && outOfQuantile>0){
			unknownCorr = 1.0;
		}
			
		probs.put(SimObject.class, unknownCorr);
		corrSum += unknownCorr;
		//System.out.println(env.identifyObject(obj.getLoc()));

		for (Class c : probs.keySet()) {
			
			probs.put(c, probs.get(c) / corrSum);
			//System.out.println(c.getSimpleName() + " : " + probs.get(c));
		}

		//System.out.println("============");
		return probs;
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
