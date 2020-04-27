package boa.runtime;

import java.io.IOException;
import java.lang.ClassNotFoundException;

public interface Tuple {
	public String[] getValues();
	public String[] getFieldNames();
	public byte[] serialize(Object o) throws IOException;
	public Object getValue(String f);
}
