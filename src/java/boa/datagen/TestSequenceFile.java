package boa.datagen;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import com.google.protobuf.CodedInputStream;

import boa.dsi.DSIProperties;

public class TestSequenceFile {

	public static void main(String[] args) throws IOException {
		Configuration conf = new Configuration();
		FileSystem fileSystem = FileSystem.get(conf);
//		
//		SequenceFile.Writer w = SequenceFile.createWriter(fileSystem, conf, new Path("seq"), Text.class, BytesWritable.class);
//		for (int i = 0; i < 1; i++) {
//			ASTRoot.Builder ast = ASTRoot.newBuilder();
//			ast.addImports("a.b.C");
//			w.append(new Text(i + ""), new BytesWritable(ast.build().toByteArray()));
//			System.out.println("Parse before writing to sequence file: " + ASTRoot.parseFrom(ast.build().toByteArray()).getImportsList());
//		}
//		w.close();
		
		Text key = new Text();
		BytesWritable val = new BytesWritable();
		SequenceFile.Reader r = new SequenceFile.Reader(fileSystem, new Path(DSIProperties.HADOOP_SEQ_FILE_LOCATION + "/" + DSIProperties.HADOOP_WEATHER_FILE_NAME), conf);
	//	SequenceFile.Reader ast = new SequenceFile.Reader(fileSystem, new Path(DSIProperties.HADOOP_SEQ_FILE_LOCATION + "/" + "data"), conf);
		while (r.next(key, val)) {
			System.out.println("next project");
			byte[] bytes = val.getBytes();
			System.out.print("Parse after writing to sequence file: ");
			System.out.println(boa.types.TransportationSchema.WeatherRoot.parseFrom(CodedInputStream.newInstance(bytes, 0, val.getLength())));
		}
//		while (ast.next(key, val)) {
//			byte[] bytes = val.getBytes();
//			System.out.println(ASTRoot.parseFrom(CodedInputStream.newInstance(bytes, 0, val.getLength())));
//		}
		r.close();
	}

}
