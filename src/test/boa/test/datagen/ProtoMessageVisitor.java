package boa.test.datagen;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Message;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;

public class ProtoMessageVisitor {
	
	public void visit(Message message) {
		if (preVisit(message)) {
			visitChildren(message);
		}
		postVisit(message);
	}

	public boolean preVisit(Message message) {
		return true;
	}

	private void visitChildren(Message message) {
		for (Iterator<Map.Entry<FieldDescriptor, Object>> iter = message.getAllFields().entrySet().iterator(); iter.hasNext();) {
            Map.Entry<FieldDescriptor, Object> field = iter.next();
            visitField(field.getKey(), field.getValue());
        }
	}

	private void visitField(FieldDescriptor field, Object value) {
		if (field.isRepeated())
            for (Iterator<?> iter = ((List<?>) value).iterator(); iter.hasNext();)
                visitFieldValue(field, iter.next());
        else
        	visitFieldValue(field, value);
	}

	private void visitFieldValue(FieldDescriptor field, Object value) {
		if (field.getType() == Type.MESSAGE)
			visit((Message) value);
	}

	public void postVisit(Message message) {
		return;
	}
}
