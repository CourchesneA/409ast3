
public class LockFreeTester {
	static int n = 0;
	volatile static boolean terminate = false;
	static LockFreeQueue<QueueItem> queue;
	
	public static void main(String args[]) {
		 if (args.length<3) {
            new Exception("Missing arguments, only "+args.length+" were specified!").printStackTrace();
		 }
        // arg 0 is p -> nb of threads that enqueues items
        int p = Integer.parseInt(args[0]);
        // arg 1 is q -> nb of threads that dequeues items
        int q = Integer.parseInt(args[1]);
        // arg 2 is n -> number of item to dequeue per thread 
        n = Integer.parseInt(args[2]);

        assert(p >= 0);
        assert(q >= 0);
        assert(n >= 0);
        
        queue = new LockFreeQueue<QueueItem>();
        
        Thread[] threadPool = new Thread[p+q];
        
        for(int i=0; i<p; i++) {
        	threadPool[i] = new Thread(new Enqueuer());
        }
        for(int i=p; i<p+q; i++) {
        	threadPool[i] = new Thread(new Dequeuer());
        }
        for(int i=0; i< threadPool.length; i++) {
        	threadPool[i].start();
        }
        
        //Wait for the dequeuer threads to terminate
        for(int i=p; i< threadPool.length; i++) {
        	try {
				threadPool[i].join();
			} catch (InterruptedException e) {}
        }
        terminate = true;
        for(int i=0; i<p; i++) {
        	try {
				threadPool[i].join();
			} catch (InterruptedException e) {}
        }
        
	}
	
	private static class Enqueuer implements Runnable{
		@Override
		public void run() {
			while(!terminate) {
				QueueItem item = new QueueItem();
				queue.enqueue(item.stampEnqueue());
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {}
			}
		}
	}
	
	private static class Dequeuer implements Runnable{
		int dequeueCount = 0;
		
		@Override
		public void run() {
			while(dequeueCount < n) {
				try {
					QueueItem item = queue.dequeue().stampDequeue();
					dequeueCount++; //Successfully dequeued an item
				}catch(EmptyQueueException e) {
					System.out.println("Tried to dequeue from empty list from "+Thread.currentThread().getName());
					//Empty queue, do not increment count and restart
				}finally {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {}
				}
			}
		}
		
	}
	
}
