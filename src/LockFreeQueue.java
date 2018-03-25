import java.util.concurrent.atomic.AtomicReference;

public class LockFreeQueue<T> {
	volatile AtomicReference<QueueNode<T>> head = null;
	volatile AtomicReference<QueueNode<T>> tail = null;
	
	public LockFreeQueue() {
		head = new AtomicReference<QueueNode<T>>(new QueueNode<T>(null));
		tail = new AtomicReference<QueueNode<T>>(head.get());
	}
	
	/**
	 * Puts x at the end of the queue (tail)
	 * @param x
	 */
	public void enqueue(T x) {
		QueueNode<T> node = new QueueNode<T>(x);
		while(true) {
			QueueNode<T> last = tail.get();
			QueueNode<T> next = last.next.get();
			if(last == tail.get()) {
				if(next == null) {
					if(last.next.compareAndSet(next, node)) {
						tail.compareAndSet(last,node);
						return;
					}
				}else {
					tail.compareAndSet(last, next);
				}
			}
		}
	}
	
	/**
	 * Remove and returns the head of this queue
	 * @return
	 */
	public T dequeue() throws EmptyQueueException {
		while(true) {
			QueueNode<T> first = head.get();
			QueueNode<T> last = tail.get();
			QueueNode<T> next = first.next.get();
			if(first == head.get()) {
				if(first == last) {
					if(next == null) {
						throw new EmptyQueueException();
					}
					tail.compareAndSet(last, next);
				}else {
					T item = next.item;
					if(head.compareAndSet(first, next)) {
						return item;
					}
				}
			}
		}
	}

	class QueueNode<T> {
		AtomicReference<QueueNode<T>> next;
		public T item;
		
		public QueueNode(T item) {
			this.item = item;
			next = new AtomicReference<QueueNode<T>>(null);
		}
	}
}