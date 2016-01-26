package clusterer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Cluster {

	private Map<Integer, TermVector> articleVectors;
	private TermVector centroid;

	public Cluster() {
		articleVectors = new HashMap<Integer, TermVector>();
		centroid = new TermVector();
	}

	public Cluster(TermVector centroid) {
		articleVectors = new HashMap<Integer, TermVector>();
		this.centroid = centroid;
	}

	public TermVector getCentroid() {
		return this.centroid;
	}

	public void setCentroid(TermVector v) {
		this.centroid = v;
	}

	public void renew() {
		articleVectors = new HashMap<Integer, TermVector>();
	}

	public void addArticle(int id, TermVector v) {
		articleVectors.put(id, v);
//		this.computeCentroid();
	}

	public void removeArticle(int id) {
		articleVectors.remove(id);
//		this.computeCentroid();
	}

	public Set<Integer> getArticleIds() {
		return articleVectors.keySet();
	}

	public TermVector getArticleVector(int id) {
		return articleVectors.get(id);
	}

	public TermVector computeCentroid() {
		Map<String, Double> vectorSum = new HashMap<String, Double>();

	}

	public double getRSS() {

	}
}
