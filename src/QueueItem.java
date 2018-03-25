public class QueueItem {
	int id;
	long enqueueTime;
	long dequeueTime;
	
	public QueueItem() {
		id = CounterSingleton.getAndIncrement();
	}
	
	QueueItem stampEnqueue() {
		this.enqueueTime = System.currentTimeMillis();
		System.out.println("["+enqueueTime+"] enq     "+id+" THREAD: "+Thread.currentThread().getName());
		return this;
	}
	
	QueueItem stampDequeue() {
		this.dequeueTime = System.currentTimeMillis();
		System.out.println("["+dequeueTime+"]     deq "+id+" THREAD: "+Thread.currentThread().getName());
		return this;
	}
}
