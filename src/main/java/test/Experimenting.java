package test;

public class Experimenting extends Experimenting2 implements ExperimentingInterface<Integer>{

	int k = 7;
	
	@Override
	public boolean supports(Integer i) {
		return i != null && i.equals(k);
	}
	
	public static void main(String args[]) {
		
		
		
		System.out.println();
	}

	

}
