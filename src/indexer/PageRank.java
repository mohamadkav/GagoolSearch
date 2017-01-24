package indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import newcrawler.Core;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class PageRank {

	private static final String FILES_PATH = System.getProperty("user.dir");
	private static final int N = Core.REQUIRED_DOC_COUNT;
	private static double ALPHA = 0.2;
	private static final double PRECISION = 0.01;
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
	//this.consAdjMatFromFile();
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
				throw new RuntimeException("Haven't I fucked myself enough??!");
            for(JsonElement docJsonElement:jsonArray){
                int currDocId=((JsonObject)docJsonElement).getAsJsonObject("_source").get("id").getAsInt();
                JsonArray refURLArray=((JsonObject)docJsonElement).getAsJsonObject("_source").getAsJsonArray("referredURLs");
                for(JsonElement refURL:refURLArray){
                    String url=refURL.getAsString();
                    if(docToIdMapping.containsKey(url))
                        adjMat[currDocId][docToIdMapping.get(url)]=1;
                }
            }
            printMatrix(adjMat);
		}catch (Exception e){
			throw new RuntimeException(e);
		}
/*		String s = "";
		Scanner scanner = new Scanner(new File(FILES_PATH + "\\links.matrix"));
		while(scanner.hasNextLine()) {
			s += scanner.nextLine();
		}
		scanner.close();
		s = s.replaceAll("\\[", "");
		s = s.replaceAll("\\]", "");
		String[] entries = s.split(",");
		for(int i=0; i<N; i++) {
			for(int j=0; j<N; j++) {
				int ind = i*N + j;
				if(entries[ind].equals("true"))
					adjMat[i][j] = 1;
				else
					adjMat[i][j] = 0;
			}
		}*/
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
			for(int j=0; j<N; j++) {
				if(adjMat[i][j] != 0)
					s++;
			}
			if(s > 0) {
				for(int j=0; j<N; j++) {
					pMat[i][j] = ((double)adjMat[i][j] * (1.0 - ALPHA) / s) + (ALPHA / N);
				}
			}
			else {
				for(int j=0; j<N; j++) {
					pMat[i][j] = 1.0/N;
				}
			}
		}
	}

	public void computePageRanksEigenvector() throws Exception {
		RealMatrix m = MatrixUtils.createRealMatrix(pMat);
		EigenDecomposition ed = new EigenDecomposition(m.transpose());
//		EigenDecomposition ed = new EigenDecomposition(m);
		double[] eVals = ed.getRealEigenvalues();

//		for(int i=0; i<eVals.length; i++)
//			System.out.println(eVals[i]);
//		System.out.println("-----");

		int ind = getEValIndex(eVals, 1);
		if(ind == -1) {
			for(int i=0; i<eVals.length; i++)
				System.err.println(eVals[i]);
			throw new Exception("What?! Eigenvalue 1 doesn't exist!");
		}
		RealVector ev = ed.getEigenvector(ind);

		RealVector nev = normalizeEigenvector(ev);
		for(int i=0; i<N; i++)
			pageRank[i] = nev.getEntry(i);

		for(int i=0; i<nev.getDimension(); i++)
			System.out.println(nev.getEntry(i));
		System.out.println("-----");
	}

	public void computePageRanksByHand() {
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
	
	public void setImaginaryPageRanks() {
		Random rand = new Random();
		for(int i=0; i<N; i++)
			pageRank[i] = rand.nextDouble();
	}

	/**
	 * normalize the vector such that its components add to 1
	 */
	private RealVector normalizeEigenvector(RealVector v) throws Exception {
		double s = v.getEntry(0);
		for(int i=1; i<v.getDimension(); i++) {
			if(v.getEntry(i) * v.getEntry(i-1) < 0)
				throw new Exception("All elements of the eigenvector don't have the same sign");
			s += v.getEntry(i);
		}
		RealVector nv = v.mapDivideToSelf(s);
		return nv;
	}

	private int getEValIndex(double[] eVals, double v) {
		for(int i=0; i<eVals.length; i++)
			if(Math.abs(eVals[i] - v) < PRECISION)
				return i;
		return -1;
	}
}
