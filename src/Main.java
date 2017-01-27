import java.util.Scanner;

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
//        new Core().execute();
//        new Indexer().indexify();
//        Thread.sleep(2000);
//        Indexer indexer=new Indexer();
//        indexer.pageRank(0.5);
//    //    System.out.println(indexer.pageRankedSearch("سعدی",null).toString());
//
//
//        indexer.cluster(5);
//        Thread.sleep(2000);
//
//        System.out.println(indexer.filterResultsByCluster(indexer.pageRankedSearch("سعدی"),2));
        
        

    	Indexer indexer = new Indexer();
		Scanner input = new Scanner(System.in);
		PostMan postMan = new PostMan();
		while (input.hasNext()) {
			switch (input.nextLine()) {

			case "put": {
				postMan.sendPut("http://localhost:9200/gagool/");
			}
			
			case "delete": {
				
			}

			case "core": {
				new Core().execute();
				System.out.println("Core created");
			}

			case "index": {
				indexer.indexify();
				System.out.println("index created");
			}

			case "rank": {
				System.out.println("enter alpha value");
				double alpha = input.nextDouble();
				indexer.pageRank(alpha);
			}

			case "filter": {
				System.out.println("please enter cluster number");
				int clusterId = input.nextInt();

				indexer.filterResultsByCluster(indexer.pageRankedSearch("سعدی"), clusterId);
			}

			}
		}
    }
}
