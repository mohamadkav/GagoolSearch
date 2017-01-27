package newcrawler;

import com.google.gson.JsonArray;
import models.Article;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by mohammad on 1/23/17.
 */
public class Core {
    private JsonArray articlesJsonArray;
    public static final int REQUIRED_DOC_COUNT = 1000;
    private static final String DOCS_JSON_DIR = "docs";
    private static final String JSON_FORMAT = ".json";
    private static final String FIRST_LINK = "https://fa.wikipedia.org/wiki/%D8%B3%D8%B9%D8%AF%DB%8C";
    private static final ArrayList<String> FIRST_DOCS= new ArrayList<String>(){{add(FIRST_LINK);}};
    private static final Random random = new Random();

    private int nextDocId=0;
    private HashMap<String, Article> articles = new HashMap<>();
    public Core() {
        initializeJson();
    }

    public void execute(){
        for(String url:FIRST_DOCS)
            doURL(url);
        System.out.println("Finished parsing initial docs");
        while(articles.size()<=REQUIRED_DOC_COUNT){
            List<String> keysAsArray = new ArrayList<>(articles.keySet());
            Article article=articles.get(keysAsArray.get(random.nextInt(articles.size())));
            doURL(article.getReferredURLs().get(random.nextInt(article.getReferredURLs().size())));
            System.out.println((articles.size()*100)/REQUIRED_DOC_COUNT+"%");
        }
    }

    private void doURL(String url){
        try {
            if(articles.containsKey(url))
                return;
            Document document = Jsoup.connect(url).timeout(10000).get();
            String abs=Parser.getAbstractFromDoc(document);
            int docId=++nextDocId;
            String title=Parser.getTitleFromDoc(document);
            List<String> outLinks=Parser.extractLinksFromDoc(document);
            Article article=new Article(title,url,docId,abs,outLinks);
            makeJson(article);
            addToJson(article);
            articles.put(url,article);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void initializeJson() {
        articlesJsonArray = new JsonArray();
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


    private void makeJson(Article article) {
        try (FileWriter file = new FileWriter(DOCS_JSON_DIR + "/" + article.getId() + JSON_FORMAT)) {
            file.write(article.getJsonObject().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addToJson(Article article) {
        articlesJsonArray.add(article.getJsonObject());
    }

}
