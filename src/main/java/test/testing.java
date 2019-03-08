package test;

import java.nio.ByteBuffer;

public class testing {

	private static int convertBytesToInt(final byte one, final byte two) {
		final ByteBuffer bb = ByteBuffer.wrap(new byte[] {two, one});
		final Short sh = new Short(bb.getShort());
		return sh.intValue();
	}
    
	public static void main(String args []) {
		byte[] b = {4,127};
		System.out.println((byte)0x0001);
		System.out.println(convertBytesToInt((byte)-103,(byte)-4));
	}
    	
}
