import java.nio.ByteBuffer;

public class OrderedByteBuffer {

	private final int writePosition;
	private final ByteBuffer data;

	public OrderedByteBuffer(int position, ByteBuffer data) {
		writePosition = position;
		this.data = data;
	}

	public ByteBuffer getData() {
		return data;
	}

	public int getWritePosition() {
		return writePosition;
	}
}
