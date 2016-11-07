package boa.dsi.storage.sequencefile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.ReflectionUtils;

import com.aol.cyclops.data.async.Queue;
import com.google.protobuf.GeneratedMessage;

import boa.dsi.DSIProperties;
import boa.dsi.dsource.AbstractSource;
import boa.dsi.storage.AbstractStorage;

public class SequenceFileStorage extends AbstractStorage {
	private Configuration conf;
	SequenceFile.Reader seqFileReader;
	SequenceFile.Writer seqFileWriter;

	public SequenceFileStorage(String location, AbstractSource parser) {
		super(location, parser);
		conf = new Configuration();
	}

	public SequenceFileStorage(AbstractSource parser) {
		super(DSIProperties.HADOOP_SEQ_FILE_LOCATION + "/" + DSIProperties.HADOOP_SEQ_FILE_NAME, parser);
		conf = new Configuration();
	}

	@Override
	public boolean isAvailable(String source) {
		org.apache.hadoop.io.Text key = (org.apache.hadoop.io.Text) ReflectionUtils
				.newInstance(seqFileReader.getKeyClass(), conf);
		org.apache.hadoop.io.BytesWritable keyValue = (org.apache.hadoop.io.BytesWritable) ReflectionUtils
				.newInstance(seqFileReader.getValueClass(), conf);
		try {
			this.seqFileReader.next(key, keyValue);
			AbstractStorage.LOG.info(key.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void store(List<GeneratedMessage> dataInstance) {
		this.openWriter(DSIProperties.HADOOP_SEQ_FILE_LOCATION + "/" + DSIProperties.HADOOP_SEQ_FILE_NAME);
		for (GeneratedMessage data : dataInstance) {
			try {
				this.seqFileWriter.append(new Text("data1"), new BytesWritable(data.toByteArray()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.closeWrite();
	}

	@Override
	public void storeAt(String location, GeneratedMessage dataInstance) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getDataInQueue(Queue<GeneratedMessage> q) {
		return false;
	}

	@Override
	public List<GeneratedMessage> getData() {
		this.openReader(DSIProperties.HADOOP_SEQ_FILE_LOCATION + "/" + DSIProperties.HADOOP_SEQ_FILE_NAME);
		List<GeneratedMessage> data = new ArrayList<GeneratedMessage>();

		org.apache.hadoop.io.Text key = (org.apache.hadoop.io.Text) ReflectionUtils
				.newInstance(this.seqFileReader.getKeyClass(), conf);
		org.apache.hadoop.io.BytesWritable keyValue = (org.apache.hadoop.io.BytesWritable) ReflectionUtils
				.newInstance(this.seqFileReader.getValueClass(), conf);

		try {
			while (this.seqFileReader.next(key, keyValue)) {
				data.add(this.parserSource.parseFrom(com.google.protobuf.CodedInputStream
						.newInstance(keyValue.getBytes(), 0, keyValue.getLength())));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return data;
	}

	private boolean openReader(String seqPath) {
		Path path = new Path(seqPath);
		FileSystem fs;
		try {
			fs = FileSystem.get(conf);
			this.seqFileReader = new SequenceFile.Reader(fs, path, conf);
			return true;
		} catch (IOException e) {
			System.out.println("Exception occured in Program node while creating FileSystem or Reader");
			e.printStackTrace();
			return false;
		}
	}

	private boolean openWriter(String seqPath) {
		FileSystem fileSystem;
		try {
			fileSystem = FileSystem.get(conf);
			this.seqFileWriter = SequenceFile.createWriter(fileSystem, conf, new Path(seqPath), Text.class,
					BytesWritable.class);
			this.seqFileWriter = SequenceFile.createWriter(fileSystem, conf, new Path(seqPath), Text.class,
					BytesWritable.class);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean closeReader() {
		try {
			seqFileReader.close();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	private boolean closeWrite() {
		try {
			seqFileWriter.close();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	@Override
	public String getDataLocation() {
		return DSIProperties.HADOOP_SEQ_FILE_LOCATION;
	}

	@Override
	public void store(Queue<GeneratedMessage> queue) {
		if (queue == null) {
			throw new UnsupportedOperationException();
		}
		int totalMessages = queue.size();
		ArrayList<GeneratedMessage> msg = new ArrayList<GeneratedMessage>();
		for (int i = 0; i < totalMessages; i++) {
			msg.add(queue.get());
		}
		this.store(msg);
	}

}
