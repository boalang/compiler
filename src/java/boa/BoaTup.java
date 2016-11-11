package boa;

import java.io.IOException;
import java.lang.ClassNotFoundException;

public interface BoaTup {
	public String[] getValues();
	public String[] getFieldNames();
	public byte[] serialize(Object o) throws IOException;
	public Object getValue(String f);
}
