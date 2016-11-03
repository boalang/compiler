package boa.externalDataSources;

import boa.datagen.util.CandoiaProperties;
import boa.datagen.util.FileIO;
import com.google.protobuf.GeneratedMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by nmtiwari on 11/2/16.
 */
public abstract class AbstractDataReader {
	protected String dataSource;
	
	public AbstractDataReader(String source){
		this.dataSource = source;
	}

    public static final AbstractDataReader getDataReaders(String source) throws UnsupportedOperationException {
        File settingFile = new File(System.getProperty("user.dir") + "/"
                + CandoiaProperties.SETTINGS_JSON_FILE_PATH + CandoiaProperties.SETTINGS_JSON_FILE_NAME);
        String setting = FileIO.readFileContents(settingFile);
        JSONArray settings = new JSONArray(setting);
        JSONObject readersList = settings.getJSONObject(CandoiaProperties.DATAREADER_FIELD_INDEX_JSON);
        String[] names = readersList.getString(CandoiaProperties.DATAREADER_FIELD_IN_JSON).split(",");

        for (String name : names) {
            try {
                @SuppressWarnings("unchecked")
                Class<AbstractDataReader> clas = (Class<AbstractDataReader>) Class.forName(name);
                AbstractDataReader reader = clas.getConstructor(String.class).newInstance(source);
                if (reader.isReadable(source)) {
                    return reader;
                }
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        throw new UnsupportedOperationException();
    }

    public abstract boolean isReadable(String source);

    public abstract GeneratedMessage getData();
}
