package test;

import java.nio.ByteBuffer;

public class Experimenting {

	private static int convertBytesToInt(final byte one, final byte two) {
		final ByteBuffer bb = ByteBuffer.wrap(new byte[] { two, one });
		final Short sh = new Short(bb.getShort());
		return sh.intValue();
	}

	public static void main(String args[]) {
		
		var k = 0;
		System.out.println(k);
		byte[] b = { 4, 127 };
		System.out.println((byte) 0x0001);
		System.out.println(convertBytesToInt((byte) -103, (byte) -4));
	}

}
