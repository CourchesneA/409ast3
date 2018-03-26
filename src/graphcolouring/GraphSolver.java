package graphcolouring;

public class GraphSolver {

	
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
       
       //Sequentially create the graph, edges between random pairs of nodes
       //Concurrently solve graph coloring without blocking (i.e. use atomics primitives (CAS,TS) and volatile)
       //Print time to solve the graph coloring
       //Verify corect coloring
	}
}
