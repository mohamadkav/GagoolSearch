package clusterer;

import java.util.*;

public class Clusterer {

	private static double INF = 1000*1000*1000;
	
	private List<TermVector> vectors;
	public Set<Cluster> clusters;
	private double RSS;
	private static final Random rand = new Random();

	//	private Map<String, Integer> allClustersTerms;
	private Set<String> allTerms;
	
	public Clusterer(List<TermVector> vectors) {
		this.vectors = vectors;
	}

	public double getRSS() {
		return this.RSS;
	}
	
	public void cluster(int k) {
		System.out.println("running KMeans started.");
		clusters = KMeans(k);
		for(Cluster c : clusters) {
			System.out.println("size of the " + c.getId() + "th cluster: " + c.getSize());
		}
		this.aggregateAllTerms();
		System.out.println("generating cluster labels started");
		for(Cluster c : clusters) {
			c.generateClusterTitle(allTerms, clusters);
		}
	}
	
	/**
	 * 
	 * @param k: indicates number of clusters
	 */
	public Set<Cluster> KMeans(int k) {
		System.out.println("KMeans started.");
		Set<Cluster> clusters = new HashSet<Cluster>();
		// choose k random centroids
		initializeClusters(clusters, k);
//		System.err.println("clusters initialized.");
		double prevRSS = 2*INF, currRSS = INF;	/** RSS abbreviates Residual Sum of Squares **/
		int steps = 0;
		while(clusteringContinues(prevRSS, currRSS, steps++)) {
			System.out.println(steps + "th step of KMeans.");
			for(Cluster c : clusters) {
				c.renew();
			}
			for(int i=0; i<vectors.size(); i++) {
				TermVector v = vectors.get(i);
				Cluster bestCluster = null;
				for(Cluster c : clusters) {
					double dist = v.distance(c.getCentroid());
					if(bestCluster == null)
						bestCluster = c;
					else if (dist < v.distance(bestCluster.getCentroid()))
						bestCluster = c;
				}
				bestCluster.addArticle(v.getId(), v);
			}
//			System.err.println("centroid lengths: ");
			for(Cluster c : clusters) {
				c.computeCentroid();
			}			
			prevRSS = currRSS;
			currRSS = computeRSS(clusters);
			System.out.println("RSS: " + currRSS);
		}
		this.RSS = currRSS;
		return clusters;
	}
	
	private void initializeClusters(Set<Cluster> clusters, int k) {
		List<Integer> initCents = new ArrayList<Integer>();
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
		if(Math.abs(prevRSS - curRSS) < 1)
			return false;
		if(steps < 10)
			return true;
		return false;
	}
	
	private double computeRSS(Set<Cluster> clusters) {
		double rss = 0;
		for(Cluster c : clusters)
			rss += c.getRSS();
		return rss;
	}

	private void aggregateAllTerms() {
		allTerms = new HashSet<String>();
		for(TermVector v : vectors) {
			for(String term : v.getTerms().keySet())
				allTerms.add(term);
		}
	}
}
