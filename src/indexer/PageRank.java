package indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class PageRank {

	private static final String FILES_PATH = System.getProperty("user.dir");
	private static final int N = 1000;
	private static double ALPHA = 0.2;
	private static final double PRECISION = 0.01;
	private int[][] adjMat;
	private double[][] pMat;
	private double[] pageRank;

	public static void main(String[] args) throws Exception {
		PageRank pr = new PageRank();
		pr.consAdjMatFromFile();
		String s = "";
		int c = 0;
		for(int i=0; i<N; i++) {
			for(int j=0; j<N; j++) {
				if(pr.adjMat[i][j] == 1)
					c++;
			}
		}
		System.err.println("Count of 1 entries: " + c);
		pr.computeProbabilityMatrix();
		pr.computePageRanksByHand();
		for(int i=0; i<N; i++)
			s += pr.pageRank[i] + " " ;
		System.err.println(s);
//		int[][] a = {{0, 1, 0}, {1, 0, 1}, {0, 1, 0}};
//		pr.setAdjacencyMatrix(a);
//		pr.computeProbabilityMatrix();
//		for(int i=0; i<N; i++) {
//			String s = "";
//			for(int j=0; j<N; j++)
//				s += pr.pMat[i][j] + " " ;
//			System.out.println(s);
//		}
//		pr.computePageRanks();
	}

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
//		this.setImaginaryPageRanks();
		this.consAdjMatFromFile();
		this.computeProbabilityMatrix();
		this.computePageRanksByHand();
	}
	
	public void setAdjacencyMatrix(int[][] m) {
		this.adjMat = m;
	}

	public void consAdjMatFromFile() throws FileNotFoundException {
		String s = "";
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
