package crawler;

import models.Article;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by saeed on 1/4/2016.
 */
public class Core {
    public static final int REQUIRED_DOC_COUNT = 1;
    String BASE_URL = "https://www.researchgate.net/";
    int nextDocId = 1;
    Downloader downloader;
    ItemPipeline itemPipeline;
    Parser parser;
    Scheduler scheduler;
    public static final String FIRST_LINK = "https://www.researchgate.net/researcher/8159937_Zoubin_Ghahramani";

    public Core() throws IOException, InterruptedException {
        downloader = Downloader.getInstance(this);
        itemPipeline = ItemPipeline.getInstance(this);
        parser = Parser.getInstance(this);
        scheduler = Scheduler.getInstance();
        Document doc = downloader.getPage(FIRST_LINK);
        parser.parseFirstPage(doc);
        while (!isDone())
            downloader.run();
    }

    public boolean isDone() {
        return nextDocId < REQUIRED_DOC_COUNT;
    }

    String getAbsoluteUrl(String url) {
        return BASE_URL + url;
    }

    public void addArticle(String url, String title, String abstraction) {
        Article article = new Article(title, url, nextDocId, abstraction);
        nextDocId++;
    }
}
