package crawler;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import models.Article;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;

/**
 * Created by saeed on 1/4/2016.
 */
public class Core {
    private static final int REQUIRED_DOC_COUNT = 1000;
    private static final String DOCS_JSON_DIR = "docs";
    private static final String JSON_FORMAT = ".json";
    private static final String LINKS_MATRIX_FILE = "links.matrix";
    private static final int REQUIRED_FRIEND_COUNT = 100;
    private static String BASE_URL = "https://fa.wikipedia.org/";
    private static final String FIRST_LINK = "https://fa.wikipedia.org/wiki/%D8%B3%D8%B9%D8%AF%DB%8C";
    private static Formatter log;
    private boolean[][] linkGraph;
    private int nextDocId = 1;
    private ItemPipeline itemPipeline;
    private Parser parser;
    private Scheduler scheduler;
    private JsonArray articlesJsonArray;

    private HashMap<String, ArrayList<Integer>> authorsDocs = new HashMap<>();
    private HashMap<String, Article> referencesTemp = new HashMap<>();
    private HashMap<String, Article> citationsTemp = new HashMap<>();
    private HashMap<String, Article> articles = new HashMap<>();

    public Core() throws FileNotFoundException {
        initializeLogging();
        initializeJson();
        Downloader downloader = Downloader.getInstance(this);
        itemPipeline = ItemPipeline.getInstance(this);
        parser = Parser.getInstance(this);
        scheduler = Scheduler.getInstance();
        Document doc = downloader.getPage(FIRST_LINK);
        parser.parseFirstPage(doc);
        System.out.println("0%");
        while (!isDone())
            downloader.run();
        cleanUpLogging();
        makeGraphFile();
        makeJson(getArticleJsons());

//        while (!isFriendDone()) {
//            System.out.println("==============================" + isFriendDone());
//            downloader.getWriterDocs();
//        }
//        makeGraphFile();
//        makeJson(getArticleJsons());
//        makeAuthorsFile();
    }

    private void makeAuthorsFile() {
        try (FileWriter file = new FileWriter("authors" + JSON_FORMAT)) {
            for (String key : authorsDocs.keySet()) {
                String str = key;
                for (int doc : authorsDocs.get(key)) {
                    str += ":" + doc;
                }
                file.write(str + "\n");
                file.flush();
            }
            file.write("\n\r");
            file.flush();
            file.close();
        } catch (Exception e) {
            makeAuthorsFile();
        }
    }

    private void makeGraphFile() {
        try (FileWriter file = new FileWriter(LINKS_MATRIX_FILE)) {
            file.write(new Gson().toJson(linkGraph));
        } catch (Exception e) {
            makeGraphFile();
        }
    }

//    public void log(String msg) {
//        long timeStamp = System.currentTimeMillis();
//        log.format(timeStamp + ":" + msg + "\n");
//        log.flush();
//    }

    private void initializeLogging() throws FileNotFoundException {
        log = new Formatter("docs.log");
    }

    private void cleanUpLogging() {
        log.format("\n\r");
        log.flush();
        log.close();
        log = null;
    }

    private void initializeJson() {
        articlesJsonArray = new JsonArray();
        linkGraph = new boolean[REQUIRED_DOC_COUNT + 1][REQUIRED_DOC_COUNT + 1];
        File dir = new File(DOCS_JSON_DIR);
        if (!dir.exists()) {
            boolean successful = dir.mkdir();
            if (successful) {
                System.out.println("docs directory was created successfully");
            } else {
                System.out.println("failed trying to create docs directory... retrying...");
                initializeJson();
            }
        }
    }

    public boolean isDone() {
        return nextDocId > REQUIRED_DOC_COUNT;
    }

    public boolean isFriendDone() {
        return authorsDocs.size() > REQUIRED_FRIEND_COUNT;
    }

    public static String getAbsoluteUrl(String url) {
        return BASE_URL + url;
    }

    public void addArticle(String url, String title, String abstraction, ArrayList<String> references, ArrayList<String> citations, ArrayList<String> authors) {
        url = url.split("\\?")[0];
        Article article = new Article(title, url, nextDocId, abstraction);
        addAuthors(authors, article);
        updateProgressBar();
        nextDocId++;
        makeJson(article);
        addToJson(article);
        addCitationsAndReferencesToTemp(article, references, citations);
        setReferencesAndCitations(article, url);
        articles.put(url, article);
//        System.out.println("---->" + url);
//        System.out.println(article);
//        addArticleToGraph(article);
    }

    private void addAuthors(ArrayList<String> authors, Article article) {
        for (String author : authors) {
            ArrayList<Integer> docIds = authorsDocs.get(author);
            if (docIds == null) {
                docIds = new ArrayList<>();
            }
            docIds.add(article.getId());
            authorsDocs.put(author, docIds);
        }
    }

    public void addArticle(String url, String title, String abstraction, ArrayList<String> references, ArrayList<String> citations) {
        url = url.split("\\?")[0];
        Article article = new Article(title, url, nextDocId, abstraction);
        updateProgressBar();
        nextDocId++;
        makeJson(article);
        addToJson(article);
        addCitationsAndReferencesToTemp(article, references, citations);
        setReferencesAndCitations(article, url);
        articles.put(url, article);
//        System.out.println("---->" + url);
//        System.out.println(article);
//        addArticleToGraph(article);
    }

    private void updateProgressBar() {
        System.out.println("\r" + (nextDocId * 100.0) / REQUIRED_DOC_COUNT + "%");
    }

    private void makeJson(Article article) {
        try (FileWriter file = new FileWriter(DOCS_JSON_DIR + "/" + article.getId() + JSON_FORMAT)) {
            file.write(article.getJsonObject().toString());
        } catch (Exception e) {
            makeJson(article);
        }
    }

    private void makeJson(JsonObject articles) {
        try (FileWriter file = new FileWriter("articles" + JSON_FORMAT)) {
            file.write(articles.toString());
        } catch (Exception e) {
            e.printStackTrace();
        //    makeJson(articles);
        }
    }


    public void setReferencesAndCitations(String baseUrl, String url) {
        url = url.split("\\?")[0];
        Article article = articles.get(baseUrl);
        if (article != null) { //if it is null it will come up later!
//            System.out.println("==========>BaseUrl-->" + baseUrl);
            url = url.split("\\?")[0];
            Article tempArticle = referencesTemp.get(url);
            if (tempArticle != null) {
                article.addReference(tempArticle);
                linkGraph[article.getId()][tempArticle.getId()] = true;
            }
            tempArticle = citationsTemp.get(url);
            if (tempArticle != null) {
                article.addCitation(tempArticle);
                linkGraph[tempArticle.getId()][article.getId()] = true;
            }
        }
    }

    private void setReferencesAndCitations(Article article, String url) {
        url = url.split("\\?")[0];
        Article tempArticle = referencesTemp.get(url);
        if (tempArticle != null) {
            article.addReference(tempArticle);
            linkGraph[article.getId()][tempArticle.getId()] = true;
        }
        tempArticle = citationsTemp.get(url);
        if (tempArticle != null) {
            article.addCitation(tempArticle);
            linkGraph[tempArticle.getId()][article.getId()] = true;
        }
    }

    private void addCitationsAndReferencesToTemp(Article article, ArrayList<String> references, ArrayList<String> citations) {
        for (String url : references) {
            referencesTemp.put(url, article);
//            System.out.println("REFERENCE: " + url);
        }
        for (String url : citations) {
            citationsTemp.put(url, article);
//            System.out.println("CITATION: " + url);
        }
    }

    private void addToJson(Article article) {
        articlesJsonArray.add(article.getJsonObject());
    }

    //
    public JsonObject getArticleJsons() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("articles", articlesJsonArray);
        return jsonObject;
    }

    public ItemPipeline getItemPipeline() {
        return itemPipeline;
    }

    public void setItemPipeline(ItemPipeline itemPipeline) {
        this.itemPipeline = itemPipeline;
    }

    public Parser getParser() {
        return parser;
    }

    public void setParser(Parser parser) {
        this.parser = parser;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
