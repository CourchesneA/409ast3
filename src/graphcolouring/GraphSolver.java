package graphcolouring;

import java.util.Random;

import sun.awt.Graphics2Delegate;

public class GraphSolver {

	volatile static boolean[][] graphEdges;	//[i][j] is true if there is an edge, only bottom left part is used (where i > j)
	volatile static int[] graphColors;
	static int nbEntries; //Number of usable entry in the array (i.e. number of possible edges)
	static int edgeCount=0;
	static Random rnd = new Random();
	static int n;
	static int e;
	static int t;

	public static void main(String args[]) {
		/* if (args.length<3) {
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
       assert(t > 0);*/
		n = 5;
		e = 3;

		graphEdges = new boolean[n][n];
		graphColors = new int[n];
		nbEntries = triangularSerie(n-1);

		//Sequentially create the graph, edges between random pairs of nodes
		constructRandomGraph();
		printGraph();
		//Concurrently solve graph coloring without blocking (i.e. use atomics primitives (CAS,TS) and volatile)
		//Print time to solve the graph coloring
		//Verify correct coloring
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
				//if(rnd.nextDouble() <= (e-edgeCount)/(nbEntries-edgeCount)) {
				if(p1 <= p2) {
					graphEdges[i][j] = true;
					edgeCount++;

				}
			}
			j++;
		}
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
}
