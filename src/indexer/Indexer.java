package indexer;

import java.io.IOException;

import models.Article;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import crawler.Core;

public class Indexer {

	private static final String INDEX_URL = "http://localhost:9200";
	private String indexName;
	
	private Core core;
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		Indexer indexer = new Indexer("gagool");
//		indexer.createIndex();
		JsonObject sampleArticle = new JsonObject();
		sampleArticle.addProperty(Article.ID_KEY, "3");
		sampleArticle.addProperty(Article.TITLE_KEY, "article title");
		sampleArticle.addProperty(Article.URL_KEY, "article.url");
		sampleArticle.addProperty(Article.ABSTRACTION_KEY, "abstract, not abstraction!");
//		indexer.addArticle(sampleArticle);
		String result = indexer.basicSearch("*");
		System.out.println(result);
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
	
	public void addAllArticles() throws ClientProtocolException, IOException {
		JsonArray articles = core.getArticleJsons().get("articles").getAsJsonArray();
		for(int i=0; i<articles.size(); i++) {
			addArticle(articles.get(i).getAsJsonObject());
		}
	}
	
	public void addArticle(JsonObject articleJson) throws ClientProtocolException, IOException {
		int articleId = articleJson.get(Article.ID_KEY).getAsInt();
		String articleString = articleJson.toString();
		
        HttpPut httpput = new HttpPut(INDEX_URL + "/" + this.indexName + "/" + "article" + "/" + articleId);
		StringEntity body = new StringEntity(articleString);
		httpput.setEntity(body);
		CloseableHttpClient httpclient = HttpClients.createDefault();
//        CloseableHttpResponse response = httpclient.execute(httpput);
        httpclient.close();
	}
	
	public String basicSearch(String query) throws ClientProtocolException, IOException {
        HttpGet httpget = new HttpGet(INDEX_URL + "/" + this.indexName + "/" + "_search?q=" + query);
		CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = httpclient.execute(httpget);
        String responseString = new BasicResponseHandler().handleResponse(response);
        httpclient.close();
        return responseString;
	}
}
