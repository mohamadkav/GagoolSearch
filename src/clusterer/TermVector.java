package clusterer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TermVector {

	private Map<String, Integer> terms = new HashMap<String, Integer>(); // number of occurrences of each term
	private Map<String, Double> normalizedVector;
	private double length;
	private int id;
	
	public TermVector() {}
	
	public TermVector(int id) {
		this.id = id;
	}
	
	public int getId() {
		return this.id;
	}
	
	public static TermVector centroid(Set<TermVector> vectors) {
		return null;
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
	
	public void normalize() {
		this.computeLength();
		normalizedVector = new HashMap<String, Double>();
		for(Map.Entry<String, Integer> entry : terms.entrySet()) {
			normalizedVector.put(entry.getKey(), (entry.getValue()/this.length));
		}
	}
	
	public double getLength() {
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
		return 0;
	}
	
	public int mostSimilar(Map<Integer, TermVector> vectors) {
		return 0;
	}
}
