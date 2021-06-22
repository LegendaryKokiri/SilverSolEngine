package silverSol.engine.render.texture.cubeMap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class CubeMapFace {

	protected ByteBuffer buffer;
	protected int width, height;
	
	public CubeMapFace(String textureFilePath) {
		try {
			InputStream in = CubeMapFace.class.getResourceAsStream(textureFilePath);
			PNGDecoder decoder = new PNGDecoder(in);
			this.width = decoder.getWidth();
			this.height = decoder.getHeight();
			this.buffer = ByteBuffer.allocateDirect(4 * width * height);
			decoder.decode(buffer, width * 4, Format.RGBA);
			buffer.flip();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
