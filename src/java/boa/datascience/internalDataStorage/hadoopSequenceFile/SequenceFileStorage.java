package boa.datascience.internalDataStorage.hadoopSequenceFile;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.ReflectionUtils;

import com.google.protobuf.GeneratedMessage;

import boa.datascience.externalDataSources.DatagenProperties;
import boa.datascience.internalDataStorage.AbstractDataStorage;

public class SequenceFileStorage extends AbstractDataStorage {
	private Configuration conf;
	SequenceFile.Reader seqFileReader;
	SequenceFile.Writer seqFileWriter;

	public SequenceFileStorage(String location) {
		super(location);
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
			AbstractDataStorage.LOG.info(key.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public GeneratedMessage getData(Class<?> type) {
		this.openReader(DatagenProperties.HADOOP_SEQ_FILE_LOCATION + "/" + DatagenProperties.HADOOP_SEQ_FILE_NAME);
		return null;
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
		Path path = new Path(seqPath);
		FileSystem fileSystem;
		try {
			fileSystem = FileSystem.get(conf);
			SequenceFile.Writer w = SequenceFile.createWriter(fileSystem, conf, new Path(seqPath), Text.class,
					BytesWritable.class);
			SequenceFile.Writer astWriter = SequenceFile.createWriter(fileSystem, conf, new Path(seqPath), Text.class,
					BytesWritable.class);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
	public void store(GeneratedMessage dataInstance) {
		this.openWriter(DatagenProperties.HADOOP_SEQ_FILE_LOCATION + "/" + DatagenProperties.HADOOP_SEQ_FILE_NAME);
		try {
			this.seqFileWriter.append("data1", dataInstance);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void storeAt(String location, GeneratedMessage dataInstance) {
		// TODO Auto-generated method stub
	}

}
