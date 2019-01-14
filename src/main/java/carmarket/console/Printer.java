package carmarket.console;

public class Printer {

	public synchronized static void print(final String message) {
		System.out.println(message);
	}

}
