import indexer.Indexer;
import indexer.PageRank;
import newcrawler.Parser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ui.HomePageFrame;

import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by saeed on 12/29/2015.
 */
public class Main {
/*    public static void main(String[] args) throws FileNotFoundException {
     //   new HomePageFrame();
        new Core();
    }*/
    public static void main(String[] args) throws Exception{
        new newcrawler.Core().execute();
        new Indexer().indexify();
        Thread.sleep(2000);
        Indexer indexer=new Indexer();
        indexer.pageRank(0.5);
        indexer.cluster(5);
    }
}
