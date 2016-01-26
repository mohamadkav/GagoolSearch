package clusterer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
	public Set<Cluster> KMeans(int k) {
		Set<Cluster> clusters = new HashSet<Cluster>();
		// choose k random centroids
		initializeClusters(clusters, k);
		double prevRSS = 2*INF, currRSS = INF;	/** RSS abbreviates Residual Sum of Squares **/
		int steps = 0;
		while(clusteringContinues(prevRSS, currRSS, steps++)) {
			for(Cluster c : clusters) {
				c.renew();
			}
			for(int i=0; i<vectors.size(); i++) {
				TermVector v = vectors.get(i);
				Cluster bestCluster = null;
				for(Cluster c : clusters) {
					double dist = v.distance(c.getCentroid());
					if(bestCluster == null || dist < v.distance(bestCluster.getCentroid()))
						bestCluster = c;
				}
				bestCluster.addArticle(v.getId(), v);
			}
			for(Cluster c : clusters) {
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
			} while (!initCents.contains(next));
			initCents.add(next);
		}
		for(int i=0; i<k; i++) {
			clusters.add(new Cluster(vectors.get(initCents.get(i))));
		}
	}

	private boolean clusteringContinues(double prevRSS, double curRSS, int steps) {
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
}
