package boa;

import java.io.IOException;
import java.util.Collection;


public interface BoaTup {
    public String[] getValues();
    public byte[] serialize(Object o) throws IOException;
    public Object getValue(String f);
    public String toString();
    public <T> T[] asArray(T[] type);
    public String[] getFieldNames();
}