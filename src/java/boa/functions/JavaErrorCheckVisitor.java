package boa.functions;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

/**
 * @author rdyer
 */
public class JavaErrorCheckVisitor extends ASTVisitor {
	public boolean hasError = false;

	public boolean preVisit2(ASTNode node) {
		if ((node.getFlags() & ASTNode.MALFORMED) != 0)
			hasError = true;
		return !hasError;
	}
}
