
public class EmptyQueueException extends Exception {

		private static final long serialVersionUID = 1L;
		
		public EmptyQueueException() {
			super("The queue is empty, unable to dequeue");
		}
		
}
