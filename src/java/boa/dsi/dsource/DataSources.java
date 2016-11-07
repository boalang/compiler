package boa.dsi.dsource;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.protobuf.GeneratedMessage;

import boa.dsi.DSComponent;

/**
 * Created by nmtiwari on 11/2/16.
 */
public class DataSources extends DSComponent {
	private static Logger LOG = Logger.getLogger(DataSources.class);
	ArrayList<String> sources;

	public DataSources(ArrayList<String> sources) {
		this.sources = sources;
	}

	@Override
	public List<GeneratedMessage> getData() {
		ArrayList<GeneratedMessage> data = new ArrayList<GeneratedMessage>();
		for (String source : this.sources) {
			AbstractDataReader reader = AbstractDataReader.getDataReaders(source);
//			reader.getData().forEach(x -> data.add(x));
			for(GeneratedMessage message: reader.getData()){
				data.add(message);
			}
		}
		return data;
	}

	public String getProtoBufParser() {
		return AbstractDataReader.getDataReaders(this.sources.get(0)).getParserClassName();
	}
}
