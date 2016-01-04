package crawler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.Article;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by saeed on 1/4/2016.
 */
public class Parser {
    private static Parser mInstance;
    private Core mCore;

    private Parser(Core core) {
        mCore = core;
    }

    public static Parser getInstance(Core core) {
        if (mInstance == null) {
            mInstance = new Parser(core);
        }
        return mInstance;
    }

    public void parseFirstPage(Document doc) throws IOException {
        Elements listOfFirstDocsHeaders = getListOfFirstDocsHeaders(doc);
        for (int i = 0; i < ((listOfFirstDocsHeaders.size() > 10) ? 10 : listOfFirstDocsHeaders.size()); i++) {
            Element header = listOfFirstDocsHeaders.get(i);
            Article article = processHeaders(header);
            mCore.itemPipeline.addArticle(article);
        }
    }

    private Elements getListOfFirstDocsHeaders(Document doc) {
        Element firstDocsLi = doc.select(".journal-publications ul").first();
        return firstDocsLi.select("li div h5");
    }


    private Article processHeaders(Element header) {
        Element headerLink = header.getElementsByTag("a").first();
        String title = headerLink.getElementsByTag("span").first().html();
        String url = headerLink.attr("href");
        return new Article(title, url);

    }

    public void parseCiteRefResponse(HttpResponse httpResponse) {
        try {
            String response = buildResponseFromEntity(httpResponse.getEntity());
            ArrayList<String[]> citations = parseCitationsJson(response);
            for (String[] citation : citations) {
                mCore.itemPipeline.addArticle(new Article(citation[0], citation[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String buildResponseFromEntity(HttpEntity entity)
            throws IllegalStateException, IOException {

        BufferedReader r = new BufferedReader(new InputStreamReader(
                entity.getContent()));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        return total.toString();
    }

    private ArrayList<String[]> parseCitationsJson(String responseStr) {
        JsonArray citationArray = new JsonParser()
                .parse(responseStr)
                .getAsJsonObject()
                .getAsJsonObject("result")
                .getAsJsonObject("data")
                .getAsJsonArray("citationItems");
        ArrayList<String[]> citations = new ArrayList<>();
        for (int i = 0; i < ((citationArray.size() >= 10) ? 10 : citationArray.size()); i++) {
            JsonObject citation = citationArray.get(i).getAsJsonObject().getAsJsonObject("data");
            citations.add(new String[]{citation.get("title").getAsString()
                    , citation.get("url").getAsString()});
        }
        return citations;
    }
}
