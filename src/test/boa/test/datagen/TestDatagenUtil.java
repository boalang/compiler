package boa.test.datagen;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Message;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;

import boa.datagen.util.ProtoMessageVisitor;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.Declaration;
import boa.types.Diff.ChangedFile;

public class TestDatagenUtil {

	static Declaration getDeclaration(final SequenceFile.Reader ar, final ChangedFile cf, final int nodeId, final HashMap<Integer, Declaration> declarations) {
		long astpos = cf.getKey();
		if (astpos > -1) {
			try {
				ar.seek(astpos);
				Writable astkey = new LongWritable();
				BytesWritable val = new BytesWritable();
				ar.next(astkey, val);
				byte[] bytes = val.getBytes();
				ASTRoot root = ASTRoot.parseFrom(CodedInputStream.newInstance(bytes, 0, val.getLength()));
				ProtoMessageVisitor v = new ProtoMessageVisitor() {
					private boolean found = false;
					
					@Override
					public boolean preVisit(Message message) {
						if (found)
							return false;
						if (message instanceof Declaration) {
							Declaration temp = (Declaration) message;
							Declaration type = declarations.get(temp.getKey());
							if (type == null) {
								type = Declaration.newBuilder(temp).build();
								declarations.put(type.getKey(), type);
							}
							if (type.getKey() == nodeId) {
								found = true;
								return false;
							}
						}
						return true;
					};
				};
				v.visit(root);
			} catch (IOException e) {}
		}
		return declarations.get(nodeId);
	}

	static HashMap<Integer, HashMap<Integer, Declaration>> collectDeclarations(final SequenceFile.Reader ar, List<ChangedFile> snapshot) throws IOException {
		HashMap<Integer, HashMap<Integer, Declaration>> fileNodeDeclaration = new HashMap<Integer, HashMap<Integer, Declaration>>();
		for (int fileIndex = 0; fileIndex < snapshot.size(); fileIndex++) {
			ChangedFile cf = snapshot.get(fileIndex);
			long astpos = cf.getKey();
			if (astpos > -1) {
				ar.seek(astpos);
				Writable astkey = new LongWritable();
				BytesWritable val = new BytesWritable();
				ar.next(astkey, val);
				byte[] bytes = val.getBytes();
				ASTRoot root = ASTRoot.parseFrom(CodedInputStream.newInstance(bytes, 0, val.getLength()));
				HashMap<Integer, Declaration> nodeDeclaration = collectDeclarations(root);
				fileNodeDeclaration.put(fileIndex, nodeDeclaration);
			}
		}
		return fileNodeDeclaration;
	}

	static HashMap<Integer, Declaration> collectDeclarations(Message message) {
		HashMap<Integer, Declaration> nodeDeclaration = new HashMap<Integer, Declaration>();
		if (message instanceof Declaration) {
			nodeDeclaration.put(((Declaration) message).getKey(), (Declaration) message);
		}
		for (Iterator<Map.Entry<FieldDescriptor, Object>> iter = message.getAllFields().entrySet().iterator(); iter.hasNext();) {
            Map.Entry<FieldDescriptor, Object> field = iter.next();
            nodeDeclaration.putAll(collectDeclarations(field.getKey(), field.getValue()));
        }
		return nodeDeclaration;
	}

	static HashMap<Integer, Declaration> collectDeclarations(FieldDescriptor field, Object value) {
		HashMap<Integer, Declaration> nodeDeclaration = new HashMap<Integer, Declaration>();
        if (field.isRepeated()) {
            // Repeated field. Print each element.
            for (Iterator<?> iter = ((List<?>) value).iterator(); iter.hasNext();)
            	if (field.getType() == Type.MESSAGE)
            		nodeDeclaration.putAll(collectDeclarations((Message) iter.next()));
        }
		return nodeDeclaration;
	}

}
