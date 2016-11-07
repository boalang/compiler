package boa.dsi.storage;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.log4j.Logger;

import com.aol.cyclops.data.async.Queue;
import com.google.protobuf.GeneratedMessage;

import boa.dsi.DSComponent;

public abstract class AbstractStorage implements DSComponent {
	protected static Logger LOG = Logger.getLogger(AbstractStorage.class);
	protected String location;
	protected Method parser;

	public AbstractStorage(String location, String parserClassName) {
		this.location = location;
		this.parser = createParserFrom(parserClassName);
	}

	public abstract boolean isAvailable(String source);

	public abstract void store(List<GeneratedMessage> dataInstance);

	public abstract void store(Queue<GeneratedMessage> queue);

	public abstract void storeAt(String location, GeneratedMessage dataInstance);

	public abstract String getDataLocation();

	private Method createParserFrom(String className) {
		Class<?> clas;
		String parentClass = className.substring(0, className.lastIndexOf('.'));
		String childName = className.substring(className.lastIndexOf('.') + 1);
		try {
			clas = Class.forName(parentClass);
			// Object parent = clas.newInstance();
			Class<?> innerClass = Class.forName(parentClass + "$" + childName);
			Method method = innerClass.getMethod("parseFrom", com.google.protobuf.CodedInputStream.class);
			return method;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new UnsupportedOperationException();
	}

	public boolean getDataInQueue(Queue<GeneratedMessage> queue) {
		if (queue == null || !queue.isOpen()) {
			throw new IllegalStateException("Your queue is not yet initialized");
		}
		// getData().forEach(data -> queue.offer(data));
		for (GeneratedMessage message : getData()) {
			queue.offer(message);
		}
		queue.close();
		return true;
	}
}
