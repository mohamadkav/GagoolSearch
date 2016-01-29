package clusterer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Cluster {

	private Map<Integer, TermVector> articleVectors;
	private TermVector centroid;
	private int id;
	private String clusterTitle;
	
	private Map<String, Integer> allTerms;
	
	public Cluster(int id) {
		articleVectors = new HashMap<Integer, TermVector>();
		centroid = new TermVector();
		this.id = id;
	}
	
	public Cluster(TermVector centroid, int id) {
		articleVectors = new HashMap<Integer, TermVector>();
		this.centroid = centroid;
		this.id = id;
	}
	
	public int getId() {
		return this.id;
	}

	public String getTitle() {
		return this.clusterTitle;
	}
	
	public int getSize() {
		return articleVectors.size();
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

	public Map<String, Integer> getAllTerms() {
		return this.allTerms;
	}
	
	public TermVector computeCentroid() {
//		Map<String, Double> vectorSum = new HashMap<String, Double>();
		centroid = new TermVector();
		for(Map.Entry<Integer, TermVector> entry : articleVectors.entrySet()) {
//			System.err.println("size of articleVectors: " + articleVectors.size() + " ---- ");
			centroid.addTo(entry.getValue());
		}
		centroid.divideLength(this.getSize());
		return centroid;
	}
	
	public double getRSS() {
		double rss = 0;
		for(Map.Entry<Integer, TermVector> entry : articleVectors.entrySet()) {
			rss += centroid.distance(entry.getValue());
		}		
		return rss;
	}
	
	public int numOfDocsContainingTerm(String term) {
		int count = 0;
		for(Map.Entry<Integer, TermVector> entry : articleVectors.entrySet()) {
			TermVector v = entry.getValue();
			if(v.getTermOcc(term) > 0)
				count++;
		}
		return count;
	}
	
	public void generateClusterTitle(Set<String> allTerms, Set<Cluster> clusters) {
		clusterTitle = "title_" + id;
		Map<String, Double> topTerms = new HashMap<String, Double>();
//		double infoSum = 0;
		for(String term : allTerms) {
			int n11 = this.numOfDocsContainingTerm(term);
			int n01 = this.getSize() - n11;
			int n = 0, n1x = 0;
			for(Cluster c : clusters) {
				n += c.getSize();
				n1x += c.numOfDocsContainingTerm(term);
			}
			int n10 = n1x - n11;
			int n00 = (n - this.getSize()) - n10;
			
			double info = 0;
			info += ((double)n11 / n) * Math.log((double)n*n11/(n10+n11)/(n01+n11));
			info += ((double)n01 / n) * Math.log((double)n*n01/(n00+n01)/(n01+n11));
			info += ((double)n10 / n) * Math.log((double)n*n10/(n10+n11)/(n00+n10));
			info += ((double)n00 / n) * Math.log((double)n*n00/(n00+n01)/(n00+n10));
			if(Double.isNaN(info))
				continue;
//			infoSum += info;
//			topTerms.put(term, info);
			if(topTerms.size() < 5) {
				topTerms.put(term, info);
			}
			else {
				String minString = null;
				double minVal = 0;
				for(Map.Entry<String, Double> entry : topTerms.entrySet()) {
					if(minString == null) {
						minString = entry.getKey();
						minVal = entry.getValue();
					}
					else if(entry.getValue() < minVal) {
						minString = entry.getKey();
						minVal = entry.getValue();
					}
				}
				if(info > minVal) {
					topTerms.remove(minString);
					topTerms.put(term, info);
				}
			}
		}
//		System.err.println(infoSum);
		String title = "";
		for(String s : topTerms.keySet())
			title += s + " " ;
		this.clusterTitle = title;
		System.out.println("label for cluster " + this.id + " : " + title);
	}
	
}
