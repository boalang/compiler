package boa.externalDataSources;

import org.apache.log4j.Logger;

import com.google.protobuf.GeneratedMessage;

import boa.compiler.BoaCompiler;

/**
 * Created by nmtiwari on 11/2/16.
 */
public class ExternalDataSources {
	private static Logger LOG = Logger.getLogger(ExternalDataSources.class);
	public static void main(String[] args) {
		String url = "https://github.com/boalang/compiler";
		getDataFrom(url);
	}

	public static GeneratedMessage getDataFrom(String source) {
		AbstractDataReader reader = AbstractDataReader.getDataReaders(source);
		GeneratedMessage data = reader.getData();
		LOG.info(data.toString());
		return data;
	}
}
