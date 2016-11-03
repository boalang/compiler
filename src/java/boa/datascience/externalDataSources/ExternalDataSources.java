package boa.datascience.externalDataSources;

import org.apache.log4j.Logger;

import com.google.protobuf.GeneratedMessage;

import boa.datascience.DataScienceComponent;

/**
 * Created by nmtiwari on 11/2/16.
 */
public class ExternalDataSources extends DataScienceComponent {
	private static Logger LOG = Logger.getLogger(ExternalDataSources.class);

	public static void main(String[] args) {
		String url = "https://github.com/boalang/compiler";
		ExternalDataSources external = new ExternalDataSources();
		external.getProcessedDataFrom(url);
	}

	public GeneratedMessage getProcessedDataFrom(String source) {
		AbstractDataReader reader = AbstractDataReader.getDataReaders(source);
		GeneratedMessage data = reader.getData();
		return data;
	}

	@Override
	public GeneratedMessage getProcessedData() {
		// TODO Auto-generated method stub
		return null;
	}
}
