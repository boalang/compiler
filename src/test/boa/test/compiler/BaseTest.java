package boa.test.compiler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public abstract class BaseTest {
	protected String load(final String fileName) throws IOException {
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(fileName));
			final byte[] bytes = new byte[(int) new File(fileName).length()];
			in.read(bytes);
			return new String(bytes);
		} finally {
			if (in != null)
				in.close();
		}
	}

	protected final void delete(final File f) throws IOException {
		if (f.isDirectory())
			for (final File g : f.listFiles())
				delete(g);

		if (!f.delete())
			throw new IOException("unable to delete file " + f);
	}
}
