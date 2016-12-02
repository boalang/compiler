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
	private final Path ROOT;
	private String toplevel;
	private String toplevelFile;

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
		Map<String, String> typToFileName = new HashMap<String, String>();

		for (ProtoFile file : protoFiles) {
			System.out.println("File: " + file.filePath());
			for (TypeElement e : file.typeElements()) {
				if (e instanceof MessageElement) {
					MessageElement msg = (MessageElement) e;
					System.out.println("Message: " + msg.name());
					if (!notPossibleToplevel.contains(msg.name())) {
						System.out.println("Adding to possible: " + msg.name());
						possibleToplevel.add(e.name());
					}
					for (FieldElement field : msg.fields()) {
						System.out.println("\t\tField: " + field.name() + " of type: " + field.type().toString());
						String type = field.type().toString();
						if (possibleToplevel.contains(type)) {
							possibleToplevel.remove(type);
						}
						notPossibleToplevel.add(type);
						System.out.println("removing from possible: " + type);
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
		this.toplevelFile = typToFileName.get(toplevel);
		this.toplevel = toplevel;
	}

	public String getToplevel() {
		return this.toplevel;
	}

	public String getToplevelFileName() {
		return this.toplevelFile;
	}

}
