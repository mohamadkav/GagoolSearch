package crawler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import models.Article;

import java.util.ArrayList;
import java.util.HashMap;

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

    private HashMap<String, Article> referencesTemp = new HashMap<>();
    private HashMap<String, Article> citationsTemp = new HashMap<>();
    private HashMap<String, Article> articles = new HashMap<>();

    public Core() {
        initializeJson();
        downloader = Downloader.getInstance(this);
        itemPipeline = ItemPipeline.getInstance(this);
        parser = Parser.getInstance(this);
        scheduler = Scheduler.getInstance();
//        Document doc = downloader.getPage(FIRST_LINK);
//        parser.parseFirstPage(doc);
        scheduler.addUrl("https://www.researchgate.net/publication/221620547_Latent_Dirichlet_Allocation");
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

    public void addArticle(String url, String title, String abstraction, ArrayList<String> references, ArrayList<String> citations) {
        url = url.split("\\?")[0];
        Article article = new Article(title, url, nextDocId, abstraction);
        nextDocId++;
        addToJson(article);
        addCitationsAndReferencesToTemp(article, references, citations);
        setReferencesAndCitations(article, url);
        articles.put(url, article);
//        System.out.println("---->" + url);
        System.out.println(article);
//        addArticleToGraph(article);
    }

    public void setReferencesAndCitations(String baseUrl, String url) {
        url = url.split("\\?")[0];
        Article article = articles.get(baseUrl);
        if (article != null) { //if it is null it will come up later!
            System.out.println("==========>BaseUrl-->" + baseUrl);
            url = url.split("\\?")[0];
            Article tempArticle = referencesTemp.get(url);
            if (tempArticle != null)
                article.addReference(tempArticle);
            tempArticle = citationsTemp.get(url);
            if (tempArticle != null)
                article.addCitation(tempArticle);
        }
    }

    private void setReferencesAndCitations(Article article, String url) {
        url = url.split("\\?")[0];
        Article tempArticle = referencesTemp.get(url);
        if (tempArticle != null)
            article.addReference(tempArticle);
        tempArticle = citationsTemp.get(url);
        if (tempArticle != null)
            article.addCitation(tempArticle);
    }

    private void addCitationsAndReferencesToTemp(Article article, ArrayList<String> references, ArrayList<String> citations) {
        for (String url : references) {
            referencesTemp.put(url, article);
            System.out.println("REFERENCE: " + url);
        }
        for (String url : citations) {
            referencesTemp.put(url, article);
            System.out.println("CITATION: " + url);
        }
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
