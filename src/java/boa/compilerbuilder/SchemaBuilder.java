package boa.compilerbuilder;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.squareup.protoparser.FieldElement;
import com.squareup.protoparser.MessageElement;
import com.squareup.protoparser.ProtoFile;
import com.squareup.protoparser.ProtoParser;
import com.squareup.protoparser.TypeElement;

public class SchemaBuilder {
	/**
	 * @return the toplevelPackage
	 */
	public String getToplevelPackage() {
		return toplevelPackage;
	}

	private final Path ROOT;
	private String toplevel;
	private String toplevelFile;
	private String toplevelPackage;

	public SchemaBuilder(String path) {
		this.ROOT = new File(path).toPath();
	}

	public List<ProtoFile> getSchema() throws IOException {
		final ArrayList<ProtoFile> schema = new ArrayList<ProtoFile>();
		final AtomicLong total = new AtomicLong();
		final AtomicLong failed = new AtomicLong();

		Files.walkFileTree(this.ROOT, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				String fileName = file.getFileName().toString();
				if (file.getFileName().toString().endsWith(".proto")) {
					total.incrementAndGet();
					String data = new String(Files.readAllBytes(file), UTF_8);
					try {
						schema.add(ProtoParser.parse(fileName, data));
					} catch (Exception e) {
						e.printStackTrace();
						failed.incrementAndGet();
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});

		if (failed.get() == 0 || total.get() > 0) {
			setToplevelDetails(schema);
			return schema;
		}
		return null;
	}

	private void setToplevelDetails(ArrayList<ProtoFile> protoFiles) {
		Set<String> possibleToplevel = new HashSet<String>();
		Set<String> notPossibleToplevel = new HashSet<String>();
		Map<String, ProtoFile> typToFile = new HashMap<String, ProtoFile>();

		for (ProtoFile file : protoFiles) {
			for (TypeElement e : file.typeElements()) {
				if (e instanceof MessageElement) {
					MessageElement msg = (MessageElement) e;
					if (!notPossibleToplevel.contains(msg.name())) {
						possibleToplevel.add(e.name());
						typToFile.put(e.name(), file);
					}
					for (FieldElement field : msg.fields()) {
						String type = field.type().toString();
						if (possibleToplevel.contains(type)) {
							possibleToplevel.remove(type);
							typToFile.remove(type);
						}
						notPossibleToplevel.add(type);
					}
				}
			}
		}
		Iterator<String> iterator = possibleToplevel.iterator();
		if (!iterator.hasNext()) {
			throw new RuntimeException("No Toplevel node found in given schema");
		}
		String toplevel = iterator.next();
		if (iterator.hasNext()) {
			while (iterator.hasNext()) {
				System.err.println(iterator.next());
			}
			throw new RuntimeException("More than one top level node found");
		}
		
		String name = typToFile.get(toplevel).filePath();
		setToplevelFileName(name.substring(0, name.lastIndexOf('.')));
		this.toplevelPackage = typToFile.get(toplevel).packageName();
		this.toplevel = toplevel;
	}

	public String getToplevel() {
		return this.toplevel;
	}

	public String getToplevelFileName() {
		return this.toplevelFile;
	}
	
	private void setToplevelFileName(String name) {
		this.toplevelFile = name.substring(0, 1).toUpperCase() + name.substring(1);
	}
	

}
