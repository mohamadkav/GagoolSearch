import com.google.gson.JsonObject;
import indexer.Indexer;
import newcrawler.Core;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.HttpClientBuilder;
import utils.HttpUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String FIRST_LINK = "https://fa.wikipedia.org/wiki/%D8%B3%D8%B9%D8%AF%DB%8C";
    private static final String INDEX_URL = "http://localhost:9200";
    private static final String INDEX_NAME = "gagool";

    public static void main(String[] args) throws Exception{

    	Indexer indexer = new Indexer();
		Scanner input = new Scanner(System.in);
		Core core=null;
        System.out.println("Please enter your initial doc links or enter 'd' if you just want to use sa'adi as default: (type q for finish)");
        List<String> inputLinks=new ArrayList<>();
        while (input.hasNext()){
            String rawInput=input.nextLine();
            if(rawInput.trim().equals("d")) {
                core = new Core(new ArrayList<String>() {{
                    add(FIRST_LINK);
                }});
                break;
            }
            else if (rawInput.trim().toLowerCase().equals("q"))
                break;
            else
                inputLinks.add(rawInput);
        }
        if(!inputLinks.isEmpty())
            core=new Core(inputLinks);
        System.out.println("INIT START-------------");
        core.execute();
        while (true) {
            try {
                System.out.println("---\nLegend: newIndex/deleteIndex/index/rank/cluster/simpleSearch/pageRankedSearch (n/d/i/r/c/s/p)");
                String command = input.nextLine().trim().toLowerCase();
                switch (command) {
                    case "n":
                        initIndex();
                        break;

                    case "d":
                        deleteIndex();
                        break;
                    case "i":
                        indexer.indexify();
                        System.out.println("Done");
                        break;
                    case "r":
                        System.out.println("Enter alpha");
                        Double alpha = Double.parseDouble(input.nextLine());
                        indexer.pageRank(alpha);
                        System.out.println("DONE");
                        break;
                    case "c":
                        System.out.println("Enter K for Kmeans");
                        Integer k = Integer.parseInt(input.nextLine());
                        indexer.cluster(k);
                        System.out.println("DONE");
                        break;
                    case "s": {
                        System.out.println("Enter Query!");
                        String query = input.nextLine();
                        System.out.println(indexer.basicSearch(query));
                        break;
                    }
                    case "p": {
                        System.out.println("Enter Query!");
                        String query = input.nextLine();
                        JsonObject result = indexer.pageRankedSearch(query);
                        System.out.println(result);
                        System.out.println("\n--Do you wanna filter these by cluster? y/n");
                        if (input.nextLine().toLowerCase().equals("y")) {
                            System.out.println("Enter cluster number: ");
                            System.out.println(indexer.filterResultsByCluster(result, Integer.parseInt(input.nextLine())));
                        }
                        break;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
		}
    }


    private static void initIndex() throws IOException {
        HttpPut httpput = new HttpPut(INDEX_URL + "/" + INDEX_NAME + "/");
        String s = "{\n" +
                "        \"mappings\": {\n" +
                "            \"tweet\": {\n" +
                "                \"properties\": {\n" +
                "                    \"title\": {\n" +
                "                                \"type\": \"text\",\n" +
                "                                \"term_vector\": \"with_positions_offsets_payloads\",\n" +
                "                                \"store\" : \"yes\",\n" +
                "                                \"analyzer\" : \"fulltext_analyzer\"\n" +
                "                         },\n" +
                "                     \"abstraction\": {\n" +
                "                                \"type\": \"text\",\n" +
                "                                \"term_vector\": \"with_positions_offsets_payloads\",\n" +
                "                                \"analyzer\" : \"fulltext_analyzer\"\n" +
                "                         }\n" +
                "                 }\n" +
                "            }\n" +
                "        },\n" +
                "        \"settings\" : {\n" +
                "            \"index\" : {\n" +
                "                \"number_of_shards\" : 1,\n" +
                "                \"number_of_replicas\" : 0\n" +
                "            },\n" +
                "            \"analysis\": {\n" +
                "                    \"analyzer\": {\n" +
                "                        \"fulltext_analyzer\": {\n" +
                "                            \"type\": \"custom\",\n" +
                "                            \"tokenizer\": \"whitespace\",\n" +
                "                            \"filter\": [\n" +
                "                                \"lowercase\",\n" +
                "                                \"type_as_payload\"\n" +
                "                            ]\n" +
                "                        }\n" +
                "                    }\n" +
                "            }\n" +
                "         }\n" +
                "    }";
        System.out.println(HttpUtils.requestWithEntity(httpput, s));
    }
    private static void deleteIndex() throws IOException{
        HttpClient client= HttpClientBuilder.create().build();
        HttpDelete httpDelete=new HttpDelete(INDEX_URL+"/_all");
        HttpResponse response = client.execute(httpDelete);
        System.out.println("Response: "
                + response.getStatusLine().getStatusCode());
    }

}
