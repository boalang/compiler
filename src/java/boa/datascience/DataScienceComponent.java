package boa.datascience;

import com.google.protobuf.GeneratedMessage;

public abstract class DataScienceComponent {
	public abstract GeneratedMessage getProcessedData();
	
	public abstract GeneratedMessage getProcessedDataFrom(String source);
}
