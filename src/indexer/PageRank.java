package indexer;

import java.lang.Math;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class PageRank {

	private static final int N = 3;
	private static final double ALPHA = 0.5;
	private static final double PRECISION = 0.01;
	private int[][] adjMat;
	private double[][] pMat;
	private double[] pageRank;
	
	public static void main(String[] args) throws Exception {
		PageRank pr = new PageRank();
		int[][] a = {{0, 1, 0}, {1, 0, 1}, {0, 1, 0}};
		pr.setAdjacencyMatrix(a);
		pr.computeProbabilityMatrix();
		for(int i=0; i<N; i++) {
			String s = "";
			for(int j=0; j<N; j++)
				s += pr.pMat[i][j] + " " ;
			System.out.println(s);
		}
		pr.computePageRanks();
	}

	public PageRank() {
		pMat = new double[N][N];
		pageRank = new double[N];
	}

	public void setAdjacencyMatrix(int[][] m) {
		this.adjMat = m;
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
					pMat[i][j] = (adjMat[i][j] * (1.0 - ALPHA) / s) + (ALPHA / N);
				}
			}
			else {
				for(int j=0; j<N; j++) {
					pMat[i][j] = 1.0/N;
				}
			}
		}
	}

	public void computePageRanks() throws Exception {
		RealMatrix m = MatrixUtils.createRealMatrix(pMat);
		EigenDecomposition ed = new EigenDecomposition(m.transpose());
//		EigenDecomposition ed = new EigenDecomposition(m);
		double[] eVals = ed.getRealEigenvalues();

//		for(int i=0; i<eVals.length; i++)
//			System.out.println(eVals[i]);
//		System.out.println("-----");

		int ind = getEValIndex(eVals, 1);
		if(ind == -1)
			throw new Exception("What?! Eigenvalue 1 doesn't exist!");
		RealVector ev = ed.getEigenvector(ind);

		RealVector nev = normalizeEigenvector(ev);
		for(int i=0; i<N; i++)
			pageRank[i] = nev.getEntry(i);

		for(int i=0; i<nev.getDimension(); i++)
			System.out.println(nev.getEntry(i));
		System.out.println("-----");
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
