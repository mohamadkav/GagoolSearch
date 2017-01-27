package newcrawler;

import models.Article;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by mohammad on 1/23/17.
 */
public class Core {
    public static final int REQUIRED_DOC_COUNT = 1000;
    private static final int OUT_DEGREE=5;



    private static final String DOCS_JSON_DIR = "docs";
    private static final String JSON_FORMAT = ".json";
    private List<String> FIRST_DOCS;
    private static final Random random = new Random();

    private int nextDocId=0;
    private HashMap<String, Article> articles = new HashMap<>();
    private HashMap<String,Integer> outDegree=new HashMap<>();
    public Core(List<String>firstDocs) {
        initializeJson();
        this.FIRST_DOCS=firstDocs;
    }

    public void execute(){
        for(String url:FIRST_DOCS)
            doURL(url);
        System.out.println("Finished parsing initial docs");
        while(articles.size()<REQUIRED_DOC_COUNT){
            List<String> keysAsArray = new ArrayList<>(articles.keySet());
            String url=keysAsArray.get(random.nextInt(articles.size()));
            Article article=articles.get(url);
            if(outDegree.get(url)!=null&&outDegree.get(url)>=OUT_DEGREE)
                continue;
            if(doURL(article.getReferredURLs().get(random.nextInt(article.getReferredURLs().size())))) {
                if (outDegree.containsKey(url))
                    outDegree.put(url, outDegree.get(url)+1);
                else
                    outDegree.put(url,1);
            }
            double percentage=((double)(articles.size())/REQUIRED_DOC_COUNT)*100;
            System.out.println(round(percentage,2)+"%");
        }
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    private boolean doURL(String url){
        try {
            if(articles.containsKey(url))
                return false;
            Document document = Jsoup.connect(url).timeout(10000).get();
            String abs=Parser.getAbstractFromDoc(document);
            String allText=Parser.getAllTextFromDoc(document);
            int docId=++nextDocId;
            String title=Parser.getTitleFromDoc(document);
            List<String> outLinks=Parser.extractLinksFromDoc(document);
            Article article=new Article(title,url,docId,abs,allText,outLinks);
            makeJson(article);
            articles.put(url,article);
            return true;
        }catch (NullPointerException e) {
            System.err.println("Failed for doc! skipping...");
            return false;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    private void initializeJson() {
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

}
