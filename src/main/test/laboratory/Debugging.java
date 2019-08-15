package laboratory;

public class Debugging {
	public static void main(String args []) {
		int k = 0;
		for (int i = 0; i<10; i++) {
			k = i%2 == 0 ? 88 : 99;
			System.out.println(k);
			System.out.println(i);
		}
		
	}
}
