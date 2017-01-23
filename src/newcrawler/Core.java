package newcrawler;

import com.google.gson.JsonArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by mohammad on 1/23/17.
 */
public class Core {
    private JsonArray articlesJsonArray;
    private boolean[][] linkGraph;
    private static final int REQUIRED_DOC_COUNT = 1000;
    private static final String DOCS_JSON_DIR = "docs";
    private static final String JSON_FORMAT = ".json";
    private static final String LINKS_MATRIX_FILE = "links.matrix";
    private static final int REQUIRED_FRIEND_COUNT = 100;
    private static String BASE_URL = "https://fa.wikipedia.org/";
    private static final String FIRST_LINK = "https://fa.wikipedia.org/wiki/%D8%B3%D8%B9%D8%AF%DB%8C";
    private static int nextDocId=0;

    private static final ArrayList<String> FIRST_DOCS= new ArrayList<String>(){{add(FIRST_LINK);}};

    public Core() {
        for(String url:FIRST_DOCS){
            try {
                Document document = Jsoup.connect(url).get();
                String abs=Parser.getAbstractFromDoc(document);
                int docId=++nextDocId;
                String title=Parser.getTitleFromDoc(document);


                System.out.println("ABS: "+abs);
                System.out.println("ID: "+docId);
                System.out.println("TITLE: "+title);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
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
}
