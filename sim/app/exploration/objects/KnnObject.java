package sim.app.exploration.objects;

public class KnnObject {
	private double dist;
	private Class thisClass;
	
	public KnnObject(double d, Class c){
		this.dist = d;
		this.thisClass = c;
	}
	public double getDist(){
		return dist;
	}
	public Class getThisClass(){
		return thisClass;
	}
}
