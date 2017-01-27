package indexer;

import java.io.FileNotFoundException;
import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import newcrawler.Core;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class PageRank {

	private static final int N = Core.REQUIRED_DOC_COUNT;
	private static double ALPHA = 0.2;
	private int[][] adjMat;
	private double[][] pMat;
	private double[] pageRank;

	public PageRank() {
		adjMat = new int[N][N];
		pMat = new double[N][N];
		pageRank = new double[N];
	}

	public double getPageRank(int i) {
		return pageRank[i];
	}

	public void computePageRanks(double alpha) throws FileNotFoundException {
		ALPHA = alpha;
        this.consAdjMatFromElastic();
		this.computeProbabilityMatrix();
		this.computePageRanksByHand();
	}

	private static final String ALL_DOCS_URL = "http://localhost:9200/gagool/_search?size="+N+"&pretty=true&q=*:*";
	private static final HttpClient client = HttpClientBuilder.create().build();
	private static final JsonParser jsonParser = new JsonParser();
	private void consAdjMatFromElastic() throws FileNotFoundException {
		try {
			HttpGet request = new HttpGet(ALL_DOCS_URL);
			request.addHeader("User-Agent", "Mozilla/5.0");
			HttpResponse response = client.execute(request);
			String json = EntityUtils.toString(response.getEntity(), "UTF-8");
			JsonArray jsonArray=jsonParser.parse(json).getAsJsonObject().getAsJsonObject("hits").getAsJsonArray("hits");
			HashMap<String,Integer> docToIdMapping=new HashMap<>();
			for(JsonElement docJsonElement:jsonArray){
				JsonObject docJsonObject=((JsonObject)docJsonElement).getAsJsonObject("_source");
				docToIdMapping.put(docJsonObject.get("url").getAsString(),docJsonObject.get("id").getAsInt());
			}
			if(docToIdMapping.size()!=jsonArray.size())
				throw new RuntimeException("Size is not equal!");
            for(JsonElement docJsonElement:jsonArray){
                int currDocId=((JsonObject)docJsonElement).getAsJsonObject("_source").get("id").getAsInt();
                JsonArray refURLArray=((JsonObject)docJsonElement).getAsJsonObject("_source").getAsJsonArray("referredURLs");
                for(JsonElement refURL:refURLArray){
                    String url=refURL.getAsString();
                    if(docToIdMapping.containsKey(url))
                        adjMat[currDocId-1][docToIdMapping.get(url)-1]=1;
                }
            }
            printMatrix(adjMat);
		}catch (Exception e){
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

    private static void printMatrix(int[][] grid) {
        for(int r=0; r<grid.length; r++) {
            for(int c=0; c<grid[r].length; c++)
                System.out.print(grid[r][c] + " ");
            System.out.println();
        }
    }

	public void computeProbabilityMatrix() {
		for(int i=0; i<N; i++) {
			int s = 0;
			for(int j=0; j<N; j++)
				if(adjMat[i][j] != 0)
					s++;
			if(s > 0)
				for(int j=0; j<N; j++)
					pMat[i][j] = ((double)adjMat[i][j] * (1.0 - ALPHA) / s) + (ALPHA / N);
			else
				for(int j=0; j<N; j++)
					pMat[i][j] = 1.0/N;
		}
	}


	private void computePageRanksByHand() {
		pageRank = new double[N];
		for(int i=0; i<N; i++) {
			pageRank[i] = 1.0 / N;
		}
		double[] prev = new double[N];
		int counter = 0;
		do {
			for(int i=0; i<N; i++)
				prev[i] = pageRank[i];
			for(int i=0; i<N; i++) {
				pageRank[i] = 0;
				for(int j=0; j<N; j++) {
					pageRank[i] += prev[j] * pMat[j][i];
				}
			}
			String s = "";
			for(int i=0; i<N; i++)
				s += pageRank[i] + " ";
			System.out.println("vector after " + counter++ + "th iteration: " + s);
		} while (iterationContinues(prev, pageRank));
	}
	
	private boolean iterationContinues(double[] prev, double[] curr) {
		double max = 0;
		for(int i=0; i<N; i++) {
			if(Math.abs(prev[i] - curr[i]) > max)
				max = Math.abs(prev[i] - curr[i]);
		}
		if(max < 0.001)
			return false;
		return true;
	}

}
