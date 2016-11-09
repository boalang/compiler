package boa.dsi.dsource.dbf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFReader;

public class DBF {
	private String filePath;

	public DBF(String source) {
		this.filePath = source;
	}

	public boolean isReadable(String source) {
		return source.endsWith(".dbf");
	}

	public List<Object[]> getData(List<String> fields) {
		ArrayList<Object[]> data = new ArrayList<Object[]>();
		InputStream in;
		try {
			in = new FileInputStream(this.filePath);
			DBFReader dr = new DBFReader(in);
			int fieldCount = dr.getFieldCount();
			for (int i = 0; i < fieldCount; i++) {
				fields.add(dr.getField(i).getName());
			}
			int records = dr.getRecordCount();
			for (int i = 0; i < records; i++) {
				data.add(dr.nextRecord());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (DBFException e) {
			e.printStackTrace();
		}
		return data;

	}
}
