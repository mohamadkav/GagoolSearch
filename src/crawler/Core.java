package crawler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import models.Article;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by saeed on 1/4/2016.
 */
public class Core {
    public static final int REQUIRED_DOC_COUNT = 1;
    public static String BASE_URL = "https://www.researchgate.net/";
    int nextDocId = 1;
    Downloader downloader;
    ItemPipeline itemPipeline;
    Parser parser;
    Scheduler scheduler;
    JsonArray articlesJsonArray;
    public static final String FIRST_LINK = "https://www.researchgate.net/researcher/8159937_Zoubin_Ghahramani";

    public Core() throws IOException, InterruptedException {
        initializeJson();
        downloader = Downloader.getInstance(this);
        itemPipeline = ItemPipeline.getInstance(this);
        parser = Parser.getInstance(this);
        scheduler = Scheduler.getInstance();
        Document doc = downloader.getPage(FIRST_LINK);
        parser.parseFirstPage(doc);
        while (!isDone())
            downloader.run();
    }

    private void initializeJson() {
        articlesJsonArray = new JsonArray();
    }

    public boolean isDone() {
        return nextDocId < REQUIRED_DOC_COUNT;
    }

    public static String getAbsoluteUrl(String url) {
        return BASE_URL + url;
    }

    public void addArticle(String url, String title, String abstraction) {
        Article article = new Article(title, url, nextDocId, abstraction);
        nextDocId++;
        addToJson(article);
        addArticleToGraph(article);
    }

    private void addToJson(Article article) {
        articlesJsonArray.add(article.getJsonObject());
    }

    public JsonObject getArticleJsons() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("articles", articlesJsonArray);
        return jsonObject;
    }
}
