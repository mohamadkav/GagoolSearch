package clusterer;

import java.util.HashMap;
import java.util.Map;

public class TermVector {

	private Map<String, Integer> terms = new HashMap<String, Integer>(); // number of occurrences of each term
	private Map<String, Double> normalizedVector;
	private double length;
	private double normalizedLength;
	private int id;
	
	public TermVector() {
		normalizedVector = new HashMap<String, Double>();		
	}
	
	public TermVector(int id) {
		this.id = id;
	}
	
	public int getId() {
		return this.id;
	}
	
//	public static TermVector centroid(Set<TermVector> vectors) {
//		return null;
//	}

	public Map<String, Integer> getTerms() {
		return this.terms;
	}
	
	public void addTerm(String term) {
		if(terms.containsKey(term))
			terms.put(term, terms.get(term)+1);
		else
			terms.put(term, 1);
	}
	
	public void addTerm(String term, int count) {
		if(terms.containsKey(term))
			terms.put(term, terms.get(term) + count);
		else
			terms.put(term, count);
	}
	
	private void computeLength() {
		length = 0;
		for(Map.Entry<String, Integer> entry : terms.entrySet())
			length += entry.getValue() * entry.getValue();
		length = Math.sqrt(length); 
	}
	
	public void computeNormalizedLength() {
		normalizedLength = 0;
		for(Map.Entry<String, Double> entry : normalizedVector.entrySet())
			normalizedLength += entry.getValue() * entry.getValue();
		normalizedLength = Math.sqrt(normalizedLength); 		
	}
	
	public double getNormalizedLength() {
		return this.normalizedLength;
	}
	
	public void normalize() {
		this.computeLength();
		normalizedVector = new HashMap<String, Double>();
		for(Map.Entry<String, Integer> entry : terms.entrySet()) {
			normalizedVector.put(entry.getKey(), (entry.getValue()/length));
		}
		this.computeNormalizedLength();
	}
	
	public Map<String, Double> getVector() {
		return normalizedVector;
	}
	
	public double getLength() {
		this.computeLength();
		return this.length;
	}
	
	public int getTermOcc(String term) {
		if(terms.containsKey(term))
			return terms.get(term);
		return 0;
	}
	
	/**
	 * return 0 if TF is 0
	 */
	public double getLWeightedTF(String term) {
		if(terms.containsKey(term))
			return (1 + Math.log10(terms.get(term)));
		return 0;
	}	
	
	public double distance(TermVector v) {
		double s = 0;
		Map<String, Double> v1 = this.normalizedVector; 
		Map<String, Double> v2 = v.getVector();
		for(Map.Entry<String, Double> entry : v1.entrySet()) {
			if(v2.containsKey(entry.getKey()))
				s += (entry.getValue() - v2.get(entry.getKey())) * (entry.getValue() - v2.get(entry.getKey()));
			else
				s += entry.getValue() * entry.getValue();
		}
		for(Map.Entry<String, Double> entry : v2.entrySet()) {
			if(!v1.containsKey(entry.getKey()))
				s += entry.getValue() * entry.getValue();
		}
//		return Math.sqrt(s);
//		System.err.println(id + ", " + v.getId() + ": " + s);
		return s;
	}
	
	/**
	 * When using this method the terms' map doesn't matter to us
	 * @param v
	 */
	public void addTo(TermVector v) {
		Map<String, Double> normalizedV = v.getVector();
		Map<String, Double> newVector = new HashMap<String, Double>();
		for(Map.Entry<String, Double> entry : normalizedVector.entrySet()) {
			if(normalizedV.containsKey(entry.getKey()))
				newVector.put(entry.getKey(), entry.getValue() + normalizedV.get(entry.getKey()));
			else
				newVector.put(entry.getKey(), entry.getValue());
		}
		for(Map.Entry<String, Double> entry : normalizedV.entrySet()) {
			if(!normalizedVector.containsKey(entry.getKey()))
				newVector.put(entry.getKey(), entry.getValue());
		}
		normalizedVector = newVector;
		this.computeNormalizedLength();
	}
	
	public void divideLength(double d) {
//		System.err.println(d);
		this.computeNormalizedLength();
//		System.err.println("before: " + this.getNormalizedLength());
		Map<String, Double> newVector = new HashMap<String, Double>();
		for(Map.Entry<String, Double> entry : normalizedVector.entrySet()) {
			newVector.put(entry.getKey(), entry.getValue() / d);
		}
		normalizedVector = newVector;
		this.computeNormalizedLength();
	}
	
}
