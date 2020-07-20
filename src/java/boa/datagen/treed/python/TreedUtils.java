package boa.datagen.treed.python;

import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.expressions.Expression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonImportAsExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonImportExpression;

public final class TreedUtils {

	public static char buildLabelForVector(ASTNode node) {
		
		char label = (char) node.getNodeType();
		
		if (node.getClass().getSimpleName().endsWith("Literal")||
				node.getClass().getSimpleName().endsWith("Reference")||
				node.getClass().getSimpleName().endsWith("Argument")) {
			return (char) (label | (node.toString().hashCode() << 7));
		}
		
		if (node.getClass().getSimpleName().contains("Python") &&
				node.getClass().getSimpleName().contains("Import"))
//		if(node instanceof PythonImportAsExpression
//				|| node instanceof PythonImportExpression)
			return (char) (label | (node.toString().hashCode() << 7));
		
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
				node.getClass().getSimpleName().endsWith("Reference")
				|| node.getClass().getSimpleName().endsWith("Argument")) {
			return label + "(" + node.toString() + ")";
		}
		if (node.getClass().getSimpleName().contains("Python") &&
				node.getClass().getSimpleName().contains("Import"))
//		if(node instanceof PythonImportAsExpression
//				|| node instanceof PythonImportExpression)
			return label + "(" + node.toString() + ")";
		
		else if (node instanceof Expression) {
			if(((Expression) node).getKind()>=1000 &&
					((Expression) node).getKind()<=1110)
				return label + "(" + ((Expression) node).getOperator().toString() + ")";
		} 
//		return label + "(" + node.toString() + ")";
		return label;
	}

}
