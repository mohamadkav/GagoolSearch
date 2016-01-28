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
	public Set<Cluster> clusters;

	private Map<String, Integer> allClustersTerms;
	private Set<String> allTerms;
	
	public Clusterer(List<TermVector> vectors) {
		this.vectors = vectors;
	}
	
	public void cluster(int k) {
		System.err.println("attempting to run KMeans.");
		clusters = KMeans(k);
		for(Cluster c : clusters) {
			System.err.println(c.getSize());
		}
		this.aggregateAllTerms();
		System.err.println("Number of all terms: " + allTerms.size());
		for(Cluster c : clusters) {
			c.generateClusterTitle(allTerms, clusters);
		}
	}
	
	/**
	 * 
	 * @param k: indicates number of clusters
	 */
	public Set<Cluster> KMeans(int k) {
		System.err.println("KMeans started.");
		Set<Cluster> clusters = new HashSet<Cluster>();
		// choose k random centroids
		initializeClusters(clusters, k);
		System.err.println("clusters initialized.");
		double prevRSS = 2*INF, currRSS = INF;	/** RSS abbreviates Residual Sum of Squares **/
		int steps = 0;
		while(clusteringContinues(prevRSS, currRSS, steps++)) {
			System.err.println(steps + "th step of KMeans.");
			for(Cluster c : clusters) {
				c.renew();
			}
			for(int i=0; i<vectors.size(); i++) {
				TermVector v = vectors.get(i);
				Cluster bestCluster = null;
				for(Cluster c : clusters) {
//					if(c.getCentroid().getVector() == null)
//						System.err.println("null");
					double dist = v.distance(c.getCentroid());
					if(bestCluster == null || dist < v.distance(bestCluster.getCentroid()))
						bestCluster = c;
				}
				bestCluster.addArticle(v.getId(), v);
			}
			for(Cluster c : clusters) {
//				System.err.println(c.getSize());
//				if(c.getCentroid().getVector() == null)
//					System.err.println("null");
//				System.err.println(c.getSize());
//				int counter = 0;
//				for(int i : c.getArticleIds())
//					if(c.getArticleVector(i) == null)
//						counter++;
//				System.err.println("counter (number of null vectors) = " + counter);
				c.computeCentroid();
			}			
			prevRSS = currRSS;
			currRSS = computeRSS(clusters);
		}
		
		return clusters;
	}
	
	private void initializeClusters(Set<Cluster> clusters, int k) {
		List<Integer> initCents = new ArrayList<Integer>();
		Random rand = new Random();
		for(int i=0; i<k; i++) {
			int next;
			do {
				next = rand.nextInt(vectors.size());
			} while (initCents.contains(next));
			initCents.add(next);
		}
		for(int i=0; i<k; i++) {
			clusters.add(new Cluster(vectors.get(initCents.get(i)), i));
		}
	}
	
	private boolean clusteringContinues(double prevRSS, double curRSS, int steps) {
		if(steps < 1)
			return true;
		return false;
	}
	
	private double computeRSS(Set<Cluster> clusters) {
		double rss = 0;
		for(Cluster c : clusters)
			rss += c.getRSS();
		return rss;
	}

	public void aggregateAllTerms() {
		allTerms = new HashSet<String>();
		for(TermVector v : vectors) {
			for(String term : v.getTerms().keySet())
				allTerms.add(term);
		}
	}
	
//	public void aggregateAllClustersTerms() {
//		allClustersTerms = new HashMap<String, Integer>();
//		for(Cluster c : clusters) {
//			Map<String, Integer> terms = c.getAllTerms();
//			for(Map.Entry<String, Integer> termEntry : terms.entrySet()) {
//				if(allClustersTerms.containsKey(termEntry.getKey())) {
//					int h = allClustersTerms.get(termEntry.getKey());
//					allClustersTerms.put(termEntry.getKey(), termEntry.getValue() + h);
//				}
//				else {
//					allClustersTerms.put(termEntry.getKey(), termEntry.getValue());
//				}				
//			}
//		}
//	}
}
