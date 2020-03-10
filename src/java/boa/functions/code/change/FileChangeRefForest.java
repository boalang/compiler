package boa.functions.code.change;

import boa.types.Code.CodeRepository;

public class FileChangeRefForest extends FileChangeForest {

	public FileChangeRefForest(CodeRepository cr, int revCount, boolean debug) {
		super(cr, revCount, debug);
	}

}
