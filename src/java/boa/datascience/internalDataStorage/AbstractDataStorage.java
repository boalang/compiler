package boa.datascience.internalDataStorage;

import org.apache.log4j.Logger;

import com.google.protobuf.GeneratedMessage;

public abstract class AbstractDataStorage {
	protected static Logger LOG = Logger.getLogger(AbstractDataStorage.class);
	protected String location;

	public AbstractDataStorage(String location) {
		this.location = location;
	}

	public abstract boolean isAvailable(String source);

	public abstract GeneratedMessage getData(Class<?> type);
	
	public abstract void store(GeneratedMessage dataInstance);
	
	public abstract void storeAt(String location, GeneratedMessage dataInstance);
}
