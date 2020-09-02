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

	public SeqCollection(Path path) {
		this.path = path;
	}

	@Override
	public Iterator<E> iterator() {
		return new SeqIterator<E>(path);
	}

}

class SeqIterator<T> implements Iterator<T> {

	private Path path;
	private SequenceFile.Reader reader;
	private FileSystem fs;
	private Configuration conf;
	private Text textKey = new Text();
	private BytesWritable value = new BytesWritable();

	public SeqIterator(Path path) {
		this.path = path;
	}

	@Override
	public boolean hasNext() {
		if (reader == null)
			openReader();

		boolean hasNextOne = false;

		try {
			hasNextOne = reader.next(textKey, value);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (hasNextOne == false)
			closeReader();

		return hasNextOne;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T next() {
		return (T) deserialize(value.getBytes());
	}

	private void closeReader() {
//		System.out.println("close");
		try {
			if (reader != null)
				reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void openReader() {
//		System.out.println("open");
		try {
			conf = new Configuration();
			fs = FileSystem.get(conf);
			reader = new SequenceFile.Reader(fs, path, conf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}