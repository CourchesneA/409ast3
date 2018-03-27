package graphcolouring;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.*;

public class GraphSolver {

	volatile static boolean[][] graphEdges;	//[i][j] is true if there is an edge, only bottom left part is used (where i > j)
	volatile static int[] graphColors;
	volatile static boolean[] conflicting;
	static int nbEntries; //Number of usable entry in the array (i.e. number of possible edges)
	static int edgeCount=0;
	static Random rnd = new Random();
	static int n;
	static int e;
	static int t;

	public static void main(String args[]) {
		if (args.length<3) {
			new Exception("Missing arguments, only "+args.length+" were specified!").printStackTrace();
		}
		// arg 0 is n -> nb of nodes in the graph
		int n = Integer.parseInt(args[0]);
		// arg 1 is e -> nb of undirected edges in the graph
		int e = Integer.parseInt(args[1]);
		// arg 2 is t -> number of threads to use 
		int t = Integer.parseInt(args[2]);

		assert(n > 3);
		assert(e > 0);
		assert(t > 0);
		n = 10000;
		e = 8000;
		t = 6;

		graphEdges = new boolean[n][n];
		graphColors = new int[n];
		nbEntries = triangularSerie(n-1);

		//Sequentially create the graph, edges between random pairs of nodes
		constructRandomGraph();
		//printGraph();
		//Concurrently solve graph coloring without blocking (i.e. use atomics primitives (CAS,TS) and volatile)
		//conflicting = new AtomicBoolean[n];	//Init the conflicting set
		long time1 = System.currentTimeMillis();
		conflicting = new boolean[n];
		for(int i=0; i<conflicting.length; i++) {
			conflicting[i] = true;
		}
		while(!arrayIsEmpty(conflicting)) {
			assign();
			DetectConflicts();
		}
		//printGraph();
		//printColors();
		//Print time to solve the graph coloring
		System.out.println("Time taken: "+(System.currentTimeMillis()-time1));
		//Verify correct coloring
		System.out.println("Maximum node degree: "+getMaximumDegree());
		System.out.println("Maximum color: "+getMaximumColor());
	}
	

	public static void assign() {
		Thread[] threads = new Thread[t];
		for(int i=0; i<threads.length; i++) {
			threads[i] = new Thread(new NodeColorer(),""+i);
		}
		for(int i=0; i<threads.length;i++) {
			threads[i].start();
		}
		for(int i=0; i< threads.length;i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {}
		}
	}
	
	public static void DetectConflicts() {
		Thread[] threads = new Thread[t];
		for(int i=0; i<threads.length; i++) {
			threads[i] = new Thread(new ConflictDetector(),""+i);
		}
		for(int i=0; i<threads.length;i++) {
			threads[i].start();
		}
		for(int i=0; i< threads.length;i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {}
		}
	}

	/**
	 * Algorithm that tries to generate all the random edges in O(e*n) for average.
	 * @Description: We keep track of the graph edges as a triangle matrix in a bool[][], using the lower part.
	 * A value of true at [i][j] means that there is an undirected edge between node i and node j.
	 * To get the random edges, we loop through each entry of the triangle matrix and if there is no edge, add one with the
	 * probability (number of edge remaining to add)/(number of edge possible)
	 * @param e
	 */
	private static void constructRandomGraph() {
		int i=0, j=0;
		while(edgeCount < e) {
			//Cycle through triangle of 2d array
			if(j>=i) {
				j=0;i++;
			}
			if(i==n) {	//Now we are out of bound, loop again
				i=0;j=0;
				continue;
			}
			if(!graphEdges[i][j]) {
				//chance of choosing this edge is nb of edge to add remaining / number of possible edge remaining
				double p1=rnd.nextDouble();
				double p2=(e-edgeCount)/(double)(nbEntries-edgeCount);
				//if(rnd.nexttrueDouble() <= (e-edgeCount)/(nbEntries-edgeCount)) {
				if(p1 <= p2) {
					graphEdges[i][j] = true;
					graphEdges[j][i] = true;
					edgeCount++;
				}
			}
			j++;
		}
	}

	private static boolean arrayIsEmpty(boolean[] conflicting2) {
		boolean ans = true;
		for(int i=0;i<conflicting2.length;i++) {
			if(conflicting2[i])return false;
		}
		return ans;
	}

	private static int getLowestColor(int node) {
		Set<Integer> neighborsColors = new HashSet<Integer>();
		//for all neighbors
		for(int i=0; i<graphEdges.length;i++) {
			if(graphEdges[node][i]) {	//if node i is a neighbor
				neighborsColors.add(graphColors[i]);
			}
		}
		int ans = 1;
		while(neighborsColors.contains(ans)) {ans++;}
		return ans;
	}
	
	private static boolean isConflict(int node) {
		int nodeColor = graphColors[node];
		//For all neighbors
		for(int i=0; i<graphEdges.length;i++) { //if node i is a neighbor
			if(graphEdges[node][i]) {
				if(graphColors[i] == nodeColor) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	//Helpers
	private static int triangularSerie(int x) {
		if(x==0)return x;
		return x+triangularSerie(x-1);
	}

	private static void printGraph() {
		for(int i=0;i<graphEdges.length; i++) {
			for(int j=0;j<graphEdges[0].length;j++) {
				char p = graphEdges[i][j] ? 'x' : ' ';
				System.out.print("["+p+"]");
			}
			System.out.print("\r\n");
		}
	}
	
	public static int getMaximumColor() {
		int max = 0;
		for(int i=0; i<graphColors.length;i++) {
			max = Math.max(graphColors[i], max);
		}
		return max;
	}
	
	public static int getMaximumDegree() {
		int max = 0;
		for(int i=0;i<graphEdges.length;i++) {
			int nodeDegree = 0;
			for(int j=0; j<graphEdges[i].length;j++) {
				if(graphEdges[i][j])nodeDegree++;
			}
			max = Math.max(max, nodeDegree);
		}
		return max;
	}
	
	
	private static void printColors() {
		for(int i=0; i<graphColors.length;i++) {
			System.out.print("["+graphColors[i]+"]");
		}
		System.out.println("");
	}
	
	static class NodeColorer implements Runnable{

		@Override
		public void run() {
			//For each element in this thread's part of the conflicting set
			int id = Integer.parseInt(Thread.currentThread().getName());
			for(int i=id; i<conflicting.length;i+=t) {
				//Set color of vertex i to smallest color not used by adjacent node
				graphColors[i] = getLowestColor(i);
			}
		}
	}
	
	static class ConflictDetector implements Runnable{
		
		@Override
		public void run() {
			int id = Integer.parseInt(Thread.currentThread().getName());
			for(int i=id; i<conflicting.length;i+=t) {
				//Atomically set v as conflicting if it has a neighbor of the same color
				conflicting[i] = isConflict(i);
			}
		}
	}
}
