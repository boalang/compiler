package boa.datagen.treed.python;

import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.expressions.Expression;

public final class TreedUtils {

	public static char buildLabelForVector(ASTNode node) {
		
		char label = (char) node.getNodeType();
		
		if (node.getClass().getSimpleName().endsWith("Literal")||
				node.getClass().getSimpleName().endsWith("Reference")) {
			return (char) (label | (node.toString().hashCode() << 7));
		}
		
		else if (node instanceof Expression) {
			if(((Expression) node).getKind()>=1000 &&
					((Expression) node).getKind()<=1110)
				return (char) (label | (((Expression) node).getOperator().hashCode() << 7));
		}
		return label;
	}

	public static String buildASTLabel(ASTNode node) {
		String label = node.getClass().getSimpleName();
		
		if (node.getClass().getSimpleName().endsWith("Literal")||
				node.getClass().getSimpleName().endsWith("Reference")) {
			return label + "(" + node.toString() + ")";
		}
		
		else if (node instanceof Expression) {
			if(((Expression) node).getKind()>=1000 &&
					((Expression) node).getKind()<=1110)
				return label + "(" + ((Expression) node).getOperator().toString() + ")";
		} 
//		return label + "(" + node.toString() + ")";
		return label;
	}

}
