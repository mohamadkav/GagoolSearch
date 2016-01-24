package indexer;

import java.io.File;
import java.io.IOException;
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

import clusterer.TermVector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class Indexer {

	private static final String INDEX_URL = "http://localhost:9200";
	private static final String FIlES_PATH = System.getProperty("user.dir");

	private String indexName;
	
//	private Core core;
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		Indexer indexer = new Indexer("gagool");
//		indexer.createIndex();
		JsonObject sampleArticle = new JsonObject();
		sampleArticle.addProperty(Article.ID_KEY, "7");
		sampleArticle.addProperty(Article.TITLE_KEY, "seventh article");
		sampleArticle.addProperty(Article.URL_KEY, "seventh.url");
		sampleArticle.addProperty(Article.ABSTRACTION_KEY, "TV sucks, thank to the internte!");
//		System.out.println(indexer.addArticle(sampleArticle));
//		indexer.updateArticlePageRank(7, 0.2);
//		String result = indexer.basicSearch("*");
//		System.out.println(result);
//		System.out.println(indexer.pageRankedSearch("abstraction"));
//		indexer.addAllArticles();
		indexer.getTermVector(27);
	}
	
	public Indexer(String name) {
		this.indexName = name;
	}
	
	public void createIndex() throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPut httpput = new HttpPut(INDEX_URL + "/" + this.indexName);
            CloseableHttpResponse response = httpclient.execute(httpput);
            System.out.println(response.getEntity());
        } finally {
            httpclient.close();
        }	
    }
	
	public void removeIndex() {
		
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
			String s = "";
			Scanner scanner = new Scanner(new File(FIlES_PATH + "\\docs\\" + i + ".json"));
			while(scanner.hasNextLine()) {
				s += scanner.nextLine();
			}
			scanner.close();			
			JsonParser parser = new JsonParser();
			JsonObject article = parser.parse(s).getAsJsonObject();
			try{
				addArticle(article);
			} catch (Exception e) {
				System.err.println(s);
				System.err.println(i);				
			}
		}
//		JsonParser parser = new JsonParser();
//		JsonReader reader = new JsonReader(new FileReader("articles." + Core.JSON_FORMAT));
		
//		JsonArray articles = parser.parse(s).getAsJsonObject().get("articles").getAsJsonArray();
//		System.err.println("articles size: " + articles.size());
//		for(int i=0; i<articles.size(); i++) {
//			addArticle(articles.get(i).getAsJsonObject());
//			System.err.println(i);
//		}
	}
	
	public String addArticle(JsonObject articleJson) throws ClientProtocolException, IOException {
		int articleId = articleJson.get(Article.ID_KEY).getAsInt();
		String articleString = articleJson.toString();		
        HttpPut httpput = new HttpPut(INDEX_URL + "/" + this.indexName + "/" + "article" + "/" + articleId);
        return this.requestWithEntity(httpput, articleString);
	}
	
	public void updateArticlePageRank(int articleId, double pageRank) throws IOException {
        HttpPost httppost = new HttpPost(INDEX_URL + "/" + this.indexName + "/" + "article" + "/" + articleId + "/_update");
        String s = "{\"doc\" : {\"page_rank\" : " + pageRank + "}}";
        this.requestWithEntity(httppost, s);
	}
	
	public String basicSearch(String query) throws ClientProtocolException, IOException {
        HttpGet httpget = new HttpGet(INDEX_URL + "/" + this.indexName + "/" + "_search?q=" + query);
		CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = httpclient.execute(httpget);
        String responseString = new BasicResponseHandler().handleResponse(response);
        httpclient.close();
        return responseString;
	}
	
	public String pageRankedSearch(String query) throws ClientProtocolException, IOException {
        HttpPost httppost = new HttpPost(INDEX_URL + "/" + this.indexName + "/" + "_search");

        /**
         * Trying to generate this:
         * {
			"query" : {
				"function_score": {
					"query" : {"match" : {"abstraction" : "article abstraction"}},
					"functions" : [
						{"script_score" : {"script" : "_score * Float.parseFloat(doc['page_rank'].value)"}}
					]
				}
				}
			}
         */
        JsonObject script = new JsonObject();
		script.addProperty("script", "_score * Float.parseFloat(doc['page_rank'].value)");
		JsonObject scriptScore = new JsonObject();
		scriptScore.add("script_score", script);
		JsonArray functions = new JsonArray();
		functions.add(scriptScore);
		JsonObject field = new JsonObject();
		field.addProperty("abstraction", query);
		JsonObject queryJson = new JsonObject();
		queryJson.add("match", field);
		JsonObject functionScoreBody = new JsonObject();
		functionScoreBody.add("query", queryJson);
		functionScoreBody.add("functions", functions);
		JsonObject functionScore = new JsonObject();
		functionScore.add("function_score", functionScoreBody);
		JsonObject wholeQuery = new JsonObject();
		wholeQuery.add("query", functionScore);
        
		System.err.println(wholeQuery.toString());
        return this.requestWithEntity(httppost, wholeQuery.toString());		
	}
	
	public String indexSearchScript() throws ClientProtocolException, IOException {
		String pageRankScriptId = "pageRank_script";
        HttpPost httppost = new HttpPost(INDEX_URL + "/_scripts/mustache/" + pageRankScriptId);
        String script = "{\"script\" : \"_score * doc['page_rank'].value \"}";
        return requestWithEntity(httppost, script);
	}
	
	public String requestWithEntity(HttpEntityEnclosingRequestBase httpRequest, String requestBody) throws ClientProtocolException, IOException {
		StringEntity entity = new StringEntity(requestBody);
		httpRequest.setEntity(entity);
		CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        String responseString = new BasicResponseHandler().handleResponse(response);
        httpclient.close();
        return responseString;
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
        System.out.println(res);
        
        TermVector v = new TermVector();
        JsonObject json = new JsonParser().parse(res).getAsJsonObject();
        JsonObject terms = 
        		json.get("term_vectors").getAsJsonObject().get("abstraction").getAsJsonObject().get("terms").getAsJsonObject();
        for(Map.Entry<String, JsonElement> entry : terms.entrySet()) {
        	int tf = entry.getValue().getAsJsonObject().get("term_freq").getAsInt();
        	v.addTerm(entry.getKey(), tf);
        	System.err.println(entry.getKey() + ": " + tf);
        }
        terms = json.get("term_vectors").getAsJsonObject().get("title").getAsJsonObject().get("terms").getAsJsonObject();
        for(Map.Entry<String, JsonElement> entry : terms.entrySet()) {
        	int tf = entry.getValue().getAsJsonObject().get("term_freq").getAsInt();
        	v.addTerm(entry.getKey(), tf);
        	System.err.println(entry.getKey() + ": " + tf);
        }
        return v;
	}
}
