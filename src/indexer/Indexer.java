package indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import models.Article;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import clusterer.Cluster;
import clusterer.Clusterer;
import clusterer.TermVector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class Indexer {

	private static final String INDEX_URL = "http://localhost:9200";
	private static final String FILES_PATH = System.getProperty("user.dir");
	private static final int N = 1000;

	private String indexName;
	private PageRank pageRank;

	public Indexer() {
		this.indexName = "gagool";
		pageRank = new PageRank();
	}

	/** UI Methods **/
	public void indexify() throws ClientProtocolException, IOException {
		this.addAllArticles();
	}

	public void cluster(int k) throws ClientProtocolException, IOException {
		List<TermVector> vectors = new ArrayList<TermVector>();
		for(int i=1; i<=1000; i++) {
			try {
				TermVector v = this.getTermVector(i);
				if(v == null)
					System.err.println(i);
				else {
					vectors.add(v);
				}
			} catch (Exception e) {
//				e.printStackTrace();
				System.err.println(i);
			}
		}
		Clusterer clusterer = new Clusterer(vectors);
		clusterer.cluster(k);
		for(Cluster c : clusterer.clusters) {
			this.addClusterToIndex(c);
			for(int id : c.getArticleIds()) {
				this.updateArticleCluster(id, c);
			}
		}
	}
	
	public void pageRank(double alpha) throws FileNotFoundException {
		this.assignPageRanks(alpha);
	}


		
	public void addAllArticles() throws ClientProtocolException, IOException {
//		JsonArray articles = core.getArticleJsons().get("articles").getAsJsonArray();
//		FileReader file = new FileReader("articles" + Core.JSON_FORMAT);
//		String s = "";
//		Scanner scanner = new Scanner(FIlES_PATH + "/articles." + Core.JSON_FORMAT);
//		while(scanner.hasNextLine()) {
//			s += scanner.nextLine();
//		}
//		scanner.close();
		for(int i=1; i<=1000; i++) {
//			System.err.println(i);
			String s = "";
			Scanner scanner = new Scanner(new File(FILES_PATH + "\\docs\\" + i + ".json"));
			while(scanner.hasNextLine()) {
				s += scanner.nextLine();
			}
			scanner.close();
//			System.err.println("scanner closed");
			JsonParser parser = new JsonParser();
			JsonObject article = parser.parse(s).getAsJsonObject();
			try{
				addArticle(article);
			} catch (Exception e) {
//				System.err.println(s);
				System.err.println(i);				
			}
			if(i % 50 == 0)
				System.out.println(i + "th article added to index.");
		}
	}
	
	public String addArticle(JsonObject articleJson) throws IOException {
		int articleId = articleJson.get(Article.ID_KEY).getAsInt();
		String articleString = articleJson.toString();
        HttpPut httpput = new HttpPut(INDEX_URL + "/" + this.indexName + "/" + "article" + "/" + articleId);
        return this.requestWithEntity(httpput, articleString);
	}
	
	public void assignPageRanks(double alpha) throws FileNotFoundException {
//		pageRank.consAdjMatFromFile();
		pageRank.computePageRanks(alpha);
		for(int i=0; i<N; i++) {
			try {
				updateArticlePageRank(i, pageRank.getPageRank(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void updateArticlePageRank(int articleId, double pageRank) throws IOException {
        HttpPost httppost = new HttpPost(INDEX_URL + "/" + this.indexName + "/" + "article" + "/" + articleId + "/_update");
        String s = "{\"doc\" : {\"page_rank\" : " + pageRank + "}}";
        this.requestWithEntity(httppost, s);
	}
	
	public String addClusterToIndex(Cluster c) throws ClientProtocolException, IOException {
		JsonObject json = new JsonObject();
		json.addProperty("cluster_id", c.getId());
		json.addProperty("cluster_title", c.getTitle());
        HttpPut httpput = new HttpPut(INDEX_URL + "/" + this.indexName + "/" + "cluster" + "/" + c.getId());
        return this.requestWithEntity(httpput, json.toString());		
	}

	public void updateArticleCluster(int articleId, Cluster c) throws ClientProtocolException, IOException {
        HttpPost httppost = new HttpPost(INDEX_URL + "/" + this.indexName + "/" + "article" + "/" + articleId + "/_update");
        String s = "{\"doc\" : {\"cluster_id\" : " + c.getId() + "}}";
        this.requestWithEntity(httppost, s);		
	}

	public String requestWithEntity(HttpEntityEnclosingRequestBase httpRequest, String requestBody) throws ClientProtocolException, IOException {
		StringEntity entity = new StringEntity(requestBody);
		httpRequest.setEntity(entity);
		CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        try {
        	String responseString = new BasicResponseHandler().handleResponse(response);
            httpclient.close();
            return responseString;
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return null;
	}
	
	public TermVector getTermVector(int id) throws ClientProtocolException, IOException {
        HttpPost httppost = new HttpPost(INDEX_URL + "/" + this.indexName + "/" + "article/" + id + "/_termvectors");	
        JsonArray fields = new JsonArray();
        fields.add(new JsonPrimitive("abstraction"));
        fields.add(new JsonPrimitive("title"));
        JsonObject body = new JsonObject();
        body.add("fields", fields);
        body.addProperty("term_statistics", true);
        String res = requestWithEntity(httppost, body.toString());
//        System.out.println(res);
        
        TermVector v = new TermVector(id);
        JsonObject json = new JsonParser().parse(res).getAsJsonObject();
        JsonObject terms = 
        		json.get("term_vectors").getAsJsonObject().get("abstraction").getAsJsonObject().get("terms").getAsJsonObject();
        for(Map.Entry<String, JsonElement> entry : terms.entrySet()) {
        	int tf = entry.getValue().getAsJsonObject().get("term_freq").getAsInt();
        	v.addTerm(entry.getKey(), tf);
//        	System.err.println(entry.getKey() + ": " + tf);
        }
        terms = json.get("term_vectors").getAsJsonObject().get("title").getAsJsonObject().get("terms").getAsJsonObject();
        for(Map.Entry<String, JsonElement> entry : terms.entrySet()) {
        	int tf = entry.getValue().getAsJsonObject().get("term_freq").getAsInt();
        	v.addTerm(entry.getKey(), tf);
//        	System.err.println(entry.getKey() + ": " + tf);
        }
        v.normalize();
        return v;
	}
}
