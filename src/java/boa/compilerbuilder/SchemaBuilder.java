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
import java.util.concurrent.atomic.AtomicLong;

import com.squareup.protoparser.ProtoFile;
import com.squareup.protoparser.ProtoParser;

public class SchemaBuilder {
	private final Path ROOT;

	public SchemaBuilder(String path) {
		this.ROOT = new File(path).toPath();
	}

	public ProtoFile getSchema() throws IOException {
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
			return mergeProtoFiles(schema);
		}
		return null;
	}

	private ProtoFile mergeProtoFiles(ArrayList<ProtoFile> protoFiles) {
		switch (protoFiles.size()) {
		case 0:
			return null;
		case 1:
			return protoFiles.get(0);
		default:
			return mergeSchema(protoFiles);
		}
	}

	private ProtoFile mergeSchema(ArrayList<ProtoFile> protoFiles) {
		throw new UnsupportedOperationException();
	}

}
