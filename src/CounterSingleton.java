
public class CounterSingleton {
	private static CounterSingleton instance;
	private static int count = 1;
	
	public static int getAndIncrement() {
		if(instance == null) {
			instance = new CounterSingleton();
		}
		return count++; 
	}
}
