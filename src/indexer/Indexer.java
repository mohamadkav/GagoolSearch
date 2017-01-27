package indexer;

import clusterer.Cluster;
import clusterer.Clusterer;
import clusterer.TermVector;
import com.google.gson.*;
import models.Article;
import newcrawler.Core;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import utils.HttpUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Indexer {

	private static final String INDEX_URL = "http://localhost:9200";
	private static final String FILES_PATH = System.getProperty("user.dir");
	private String indexName;
	private PageRank pageRank;

	public Indexer() {
		this.indexName = "gagool";
		pageRank = new PageRank();
	}

	/** UI Methods **/
	public void indexify() throws IOException {
		this.addAllArticles();
	}

	public void cluster(int k) throws IOException {
		List<TermVector> vectors = new ArrayList<TermVector>();
		for(int i=1; i<=Core.REQUIRED_DOC_COUNT; i++) {
			try {
				TermVector v = this.getTermVector(i);
				if(v == null)
					System.err.println(i);
				else {
					vectors.add(v);
				}
			} catch (Exception e) {
				e.printStackTrace();
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


		
	private void addAllArticles() throws IOException {
		for(int i = 1; i<= Core.REQUIRED_DOC_COUNT; i++) {
//			System.err.println(i);
			String s = "";
			Scanner scanner = new Scanner(new File(FILES_PATH + "/docs/" + i + ".json"));
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
	
	private String addArticle(JsonObject articleJson) throws IOException {
		int articleId = articleJson.get(Article.ID_KEY).getAsInt();
		String articleString = articleJson.toString();
        HttpPut httpput = new HttpPut(INDEX_URL + "/" + this.indexName + "/" + "article" + "/" + articleId);
        return HttpUtils.requestWithEntity(httpput, articleString);
	}
	
	private void assignPageRanks(double alpha) throws FileNotFoundException {
//		pageRank.consAdjMatFromFile();
		pageRank.computePageRanks(alpha);
		for(int i=0; i<Core.REQUIRED_DOC_COUNT; i++) {
			try {
				updateArticlePageRank(i+1, pageRank.getPageRank(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void updateArticlePageRank(int articleId, double pageRank) throws IOException {
        HttpPost httppost = new HttpPost(INDEX_URL + "/" + this.indexName + "/" + "article" + "/" + articleId + "/_update");
        String s = "{\"doc\" : {\"page_rank\" : " + pageRank + "}}";
        HttpUtils.requestWithEntity(httppost, s);
	}
	
	private String addClusterToIndex(Cluster c) throws IOException {
		JsonObject json = new JsonObject();
		json.addProperty("cluster_id", c.getId());
		json.addProperty("cluster_title", c.getTitle());
        HttpPut httpput = new HttpPut(INDEX_URL + "/" + this.indexName + "/" + "cluster" + "/" + c.getId());
        return HttpUtils.requestWithEntity(httpput, json.toString());		
	}

	private void updateArticleCluster(int articleId, Cluster c) throws IOException {
        HttpPost httppost = new HttpPost(INDEX_URL + "/" + this.indexName + "/" + "article" + "/" + articleId + "/_update");
        String s = "{\"doc\" : {\"cluster_id\" : " + c.getId() + "}}";
        HttpUtils.requestWithEntity(httppost, s);		
	}

	private TermVector getTermVector(int id) throws IOException {
        HttpPost httppost = new HttpPost(INDEX_URL + "/" + this.indexName + "/" + "article/" + id + "/_termvectors");	
        JsonArray fields = new JsonArray();
        fields.add(new JsonPrimitive("abstraction"));
		fields.add(new JsonPrimitive("title"));
        JsonObject body = new JsonObject();
        body.add("fields", fields);
        body.addProperty("term_statistics", true);
        String res = HttpUtils.requestWithEntity(httppost, body.toString());
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

    public JsonArray filterResultsByCluster(JsonObject initialResult, int clusterId) {
        JsonArray hitsArray = initialResult.get("hits").getAsJsonArray();
        JsonArray result = new JsonArray();
        for(int i=0; i<hitsArray.size(); i++) {
            JsonObject hit = hitsArray.get(i).getAsJsonObject();
            int id = Integer.parseInt(hit.get("_source").getAsJsonObject().get("cluster_id").toString());
            if(id == clusterId)
                result.add(hit);
        }
        return result;
    }

	public JsonObject pageRankedSearch(String query) throws IOException {
		HttpPost httppost = new HttpPost(INDEX_URL + "/" + this.indexName + "/article/" + "_search");
		JsonObject script = new JsonObject();
		script.addProperty("script", "_score +  doc['page_rank'].value");
		JsonObject scriptScore = new JsonObject();
		scriptScore.add("script_score", script);
		JsonArray functions = new JsonArray();
		functions.add(scriptScore);
		JsonObject field = new JsonObject();
		field.addProperty("query", query);
		field.add("fields",new JsonParser().parse("[\"title^5\",\"abstraction^3\",\"allText\"]"));
		JsonObject queryJson = new JsonObject();
		queryJson.add("multi_match", field);
		JsonObject functionScoreBody = new JsonObject();
//		functionScoreBody.addProperty("query", "\"bool\": {\"should\": [{\"match\": {\"title\":\"" + query + "\"}},{\"match\": {\"abstraction\":\"" + query + "\"}}]}");
		functionScoreBody.add("query", queryJson);
		functionScoreBody.add("functions", functions);
		JsonObject functionScore = new JsonObject();
		functionScore.add("function_score", functionScoreBody);
		JsonObject wholeQuery = new JsonObject();
		wholeQuery.add("query", functionScore);
		String res = HttpUtils.requestWithEntity(httppost, wholeQuery.toString());
		JsonObject json = new JsonParser().parse(res).getAsJsonObject();
		return json.get("hits").getAsJsonObject();
	}

    public String basicSearch(String query) throws IOException {
        HttpGet httpget = new HttpGet(INDEX_URL + "/" + this.indexName + "/article/" + "_search?q=" + query);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = httpclient.execute(httpget);
        String responseString = new BasicResponseHandler().handleResponse(response);
        httpclient.close();
        return responseString;
    }
}
