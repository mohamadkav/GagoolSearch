import indexer.Indexer;
import newcrawler.Core;

/**
 * Created by saeed on 12/29/2015.
 */
public class Main {
/*    public static void main(String[] args) throws FileNotFoundException {
     //   new HomePageFrame();
        new Core();
    }*/
    public static void main(String[] args) throws Exception{
        new Core().execute();
        new Indexer().indexify();
        Thread.sleep(2000);
        Indexer indexer=new Indexer();
        indexer.pageRank(0.5);
    //    System.out.println(indexer.pageRankedSearch("سعدی",null).toString());


        indexer.cluster(5);
        Thread.sleep(2000);

        System.out.println(indexer.filterResultsByCluster(indexer.pageRankedSearch("سعدی"),2));

    }
}
