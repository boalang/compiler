package boa;

import java.io.IOException;


public interface BoaTup {
    public String[] getValues();
    public byte[] serialize(Object o) throws IOException;
    public Object getValue(String f);
    public String toString();
    public String[] getFieldNames();
}