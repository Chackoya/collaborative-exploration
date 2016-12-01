package sim.app.exploration.objects;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import sim.app.exploration.utils.Utils;
import sim.util.Int2D;

/**
 * This is not an object.
 * Thank you.
 * @author Miguel
 *
 */
public class Prototype {

	public Class thisClass;
	public double size;
	public Color color;
	public int nOccurrs;
	private List<SimObject> allObjects; //used for knn
	private ArrayList<Double> allSizes; //used for correlation
	private ArrayList<Integer> allColorsRed;//used for correlation
	private ArrayList<Integer> allColorsGreen;
	private ArrayList<Integer> allColorsBlue;
	
	public Prototype(Class cls, double s, Color c){
		this.allObjects = new ArrayList();
		this.allSizes = new ArrayList();
		this.allColorsRed = new ArrayList();
		this.allColorsGreen = new ArrayList();
		this.allColorsBlue = new ArrayList();
		
		this.thisClass = cls;
		this.size = s;
		this.color = c;
		this.nOccurrs = 1;
		SimObject obj = new SimObject(new Int2D(0,0),c,s);
		this.allObjects.add(obj);		
		this.allSizes.add(s);
		this.allColorsRed.add(c.getRed());
		this.allColorsGreen.add(c.getGreen());
		this.allColorsBlue.add(c.getBlue());
		
	}
	
	public void addOccurrence(double s, Color c){
		this.size = (this.size*nOccurrs + s)/(nOccurrs+1);
		this.color = Utils.avgColor(this.color, c, nOccurrs);
		this.nOccurrs += 1;
		SimObject obj = new SimObject(new Int2D(0,0),c,s);
		allObjects.add(obj);
		allSizes.add(s);
		allColorsRed.add(c.getRed());
		allColorsGreen.add(c.getGreen());
		allColorsBlue.add(c.getBlue());		

		Collections.sort(allSizes);
		Collections.sort(allColorsRed);
		Collections.sort(allColorsBlue);
		Collections.sort(allColorsGreen);
	}
	
	public double getAverageSize(){
		return size;
	}
	public Color getAverageColor(){
		return color;
	}
	public double getSizeQuantile(float quant){
		int index = Math.max(0, Math.round(allSizes.size()*quant)-1);
		double q = allSizes.get(index);
		
		return q;
	}
	public int getRedQuantile(float quant){
		int index = Math.max(0, Math.round(allColorsRed.size()*quant)-1);
		int q = allColorsRed.get(index);
		
		return q;
	}
	public int getGreenQuantile(float quant){
		int index = Math.max(0, Math.round(allColorsGreen.size()*quant)-1);
		int q = allColorsGreen.get(index);
		
		return q;
	}
	public int getBlueQuantile(float quant){
		int index = Math.max(0, Math.round(allColorsBlue.size()*quant)-1);
		int q = allColorsBlue.get(index);
		
		return q;
	}
	
}