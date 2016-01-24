package clusterer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Clusterer {

	private static double INF = 1000*1000*1000;
	
	private List<TermVector> vectors;
	
	public Clusterer(List<TermVector> vectors) {
		this.vectors = vectors;
	}
	
	public void cluster() {
		
	}
	
	/**
	 * 
	 * @param k: indicates number of clusters
	 */
	public Map<Integer, Set<TermVector>> KMeans(int k) {
		Map<Integer, Set<TermVector>> clustering = new HashMap<Integer, Set<TermVector>>();
		// choose k random centroids
		Map<Integer, TermVector> centroids = initialCentroids(k);
		double prevRSS = 2*INF, currRSS = INF;	/** RSS abbreviates Residual Sum of Squares **/
		int steps = 0;
		while(clusteringContinues(prevRSS, currRSS, steps++)) {
			/** assigning to clusters */
			Map<Integer, Set<TermVector>> temp = new HashMap<Integer, Set<TermVector>>();
			for(int i=0; i<vectors.size(); i++) {
				TermVector v = vectors.get(i);
				int cluster = v.mostSimilar(centroids);
				if(temp.containsKey(cluster))
					temp.get(cluster).add(v);
				else {
					Set<TermVector> set = new HashSet<TermVector>();
					set.add(v);
					temp.put(cluster, set);
				}
			}
			clustering = temp;
			
			/** compute centroids */
			for(Map.Entry<Integer, Set<TermVector>> entry : clustering.entrySet()) {
				TermVector v = TermVector.centroid(entry.getValue());
				centroids.put(entry.getKey(), v);
			}
			prevRSS = currRSS;
			currRSS = computeRSS(clustering, centroids);
		}
		
		return clustering;
	}
	
	private Map<Integer, TermVector> initialCentroids(int k) {
		Map<Integer, TermVector> centroids = new HashMap<Integer, TermVector>();
		List<Integer> initCents = new ArrayList<Integer>();
		Random rand = new Random();
		for(int i=0; i<k; i++) {
			int next;
			do {
				next = rand.nextInt(vectors.size());
			} while (!initCents.contains(next));
			initCents.add(next);
		}
		for(int i=0; i<k; i++) {
			centroids.put(i, vectors.get(initCents.get(i)));
		}
		return centroids;
	}
	
	private boolean clusteringContinues(double prevRSS, double curRSS, int steps) {
		if(steps < 10)
			return true;
		return false;
	}
	
	private double computeRSS(Map<Integer, Set<TermVector>> clustering, Map<Integer, TermVector> centroids) {
		return 0;
	}
}
