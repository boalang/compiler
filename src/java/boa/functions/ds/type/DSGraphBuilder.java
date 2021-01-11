package boa.functions.ds.type;

import java.util.Iterator;
import boa.functions.ds.ClassObject;

public class DSGraphBuilder {

	public DSDatabase db;

	public DSGraphBuilder(DSDatabase db) {
		this.db = db;
	}

	public void build() {
		for (Iterator<ClassObject> itr = db.classes.iterator(); itr.hasNext();) {
			ClassObject obj = itr.next();
			if (db.contains(obj))
				continue;
			DSNodeBuilder b = new DSNodeBuilder(db, itr.next());
			if (b.isNode()) {
				b.buildNode();
			} else {
				b.buildObject();
			}
		}
	}

}
