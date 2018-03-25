
public class BlockingQueue<T> {

	private volatile QueueNode<T> head = null;
	private volatile QueueNode<T> tail = null;
	private Object enqLock = new Object();
	private Object deqLock = new Object();
	
	
	public BlockingQueue() {
		head = new QueueNode<T>(null);
		tail = head;
	}
	/**
	 * Puts x at the end of the queue (tail)
	 * @param x
	 */
	public void enqueue(T x) {
		synchronized(enqLock) {
			QueueNode<T> newNode = new QueueNode<T>(x);
			tail.next = newNode;
			tail = newNode;
		}
	}
	
	/**
	 * Remove and returns the head of this queue
	 * @return
	 */
	public T dequeue() throws EmptyQueueException {
		T result;
		synchronized(deqLock) {
			if(head.next == null) {
				throw new EmptyQueueException();
			}
			result = head.next.item;
			head = head.next;
		}
		return result;
	}

	class QueueNode<T> {
		volatile QueueNode<T> next;
		public T item;
		
		public QueueNode(T item) {
			this.item = item;
			this.next = null;
		}
	}
}





