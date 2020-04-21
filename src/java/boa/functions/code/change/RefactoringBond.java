package boa.functions.code.change;

import java.util.regex.Matcher;

import boa.functions.code.change.refactoring.BoaCodeElementLevel;
import boa.functions.code.change.refactoring.BoaRefactoringType;
import boa.types.Code.CodeRefactoring;

public class RefactoringBond {
	
	private String leftElement;
	private String rightElement;
	private String leftDecl;
	private String rightDecl;
	private CodeRefactoring refactoring;
	private BoaCodeElementLevel level;
	
	public RefactoringBond(CodeRefactoring ref) {
		this.refactoring = ref;
		this.level = BoaCodeElementLevel.getCodeElementLevel(ref.getType());
		this.leftElement = preprocess(ref.getLeftSideLocations(0).getCodeElement());
		this.rightElement = preprocess(ref.getRightSideLocations(0).getCodeElement());
		String[] declSigs = getDeclSigs(ref.getDescription());
		this.leftDecl = declSigs[0];
		this.rightDecl = declSigs[1];
	}

	public CodeRefactoring getRefactoring() {
		return refactoring;
	}
	
	public String getType() {
		return refactoring.getType();
	}

	public BoaCodeElementLevel getLevel() {
		return level;
	}
	
	public String getLeftElement() {
		return leftElement;
	}

	public String getRightElement() {
		return rightElement;
	}

	public String getLeftDecl() {
		return leftDecl;
	}

	public String getRightDecl() {
		return rightDecl;
	}
	
	private String[] getDeclSigs(String description) {
		BoaRefactoringType type = BoaRefactoringType.extractFromDescription(description);
		Matcher m = type.getRegex().matcher(description);
		String[] decls = new String[2];
		if (m.matches()) {
			switch (type) {
			case RENAME_CLASS:
            case MOVE_CLASS: {
            	decls[0] = m.group(1);
            	decls[1] = m.group(2);
            	break;
            }
            case PUSH_DOWN_OPERATION:
            case PULL_UP_OPERATION:
			case MOVE_OPERATION: {
				decls[0] = m.group(2);
				decls[1] = m.group(4);
				break;
			}
			case RENAME_METHOD: {
				decls[0] = m.group(3);
				decls[1] = m.group(3);
				break;
			}
            case MOVE_ATTRIBUTE:
            case PULL_UP_ATTRIBUTE:
            case PUSH_DOWN_ATTRIBUTE: {
            	decls[0] = m.group(2);
				decls[1] = m.group(4);
            	break;
            }
			default:
				return null;
			}
		}
		return decls;
	}
	
	private String preprocess(String element) {
		String sig = element;
		if (sig.startsWith("package "))
			sig = sig.replaceFirst("package ", "public ");
		if (sig.contains("abstract "))
			sig = sig.replaceFirst("abstract ", "");
		return sig;
	}

}
