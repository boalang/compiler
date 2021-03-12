package boa.aggregators.ml.util;

import static boa.io.EmitValue.deserialize;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

public class SeqCollection<E> implements Iterable<E> {

	private Path path;
	private FileSystem fs;
	private Configuration conf;

	public SeqCollection(Path path) {
		this.path = path;
		try {
			conf = new Configuration();
			fs = FileSystem.get(conf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public E firstInstance() {
		E first = null;
		SeqIterator<E> itr = new SeqIterator<E>();
		if (itr.hasNext()) {
			first = itr.next();
			itr.close();
		}
		return first;
	}

	@Override
	public Iterator<E> iterator() {
		return new SeqIterator<E>();
	}

	class SeqIterator<T> implements Iterator<T> {

		private SequenceFile.Reader reader;
		private Text textKey = new Text();
		private BytesWritable value = new BytesWritable();
//		private int count = 0;
		
		public SeqIterator() {
			openReader();
		}

		@Override
		public boolean hasNext() {

			boolean hasNextOne = false;

			try {
				hasNextOne = reader.next(textKey, value);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (!hasNextOne)
				closeReader();
//			else
//				System.out.println(count++ + " mem: " + freemem());

			return hasNextOne;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T next() {
			return (T) deserialize(value.getBytes());
		}

		public void close() {
			closeReader();
		}

		private void closeReader() {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void openReader() {
			try {
				conf = new Configuration();
				fs = FileSystem.get(conf);
				System.out.println(path);
				reader = new SequenceFile.Reader(fs, path, conf);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}