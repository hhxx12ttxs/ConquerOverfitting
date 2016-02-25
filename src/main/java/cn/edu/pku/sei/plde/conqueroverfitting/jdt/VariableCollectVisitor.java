package cn.edu.pku.sei.plde.conqueroverfitting.jdt;

import java.util.List;
import java.util.ArrayList;

import org.eclipse.jdt.core.dom.*;

import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeInference;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.MethodInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;

/**
 * ASTVisit that helps to generate a tree
 * 
 * @author jiewang
 */
class VariableCollectVisitor extends ASTVisitor {
	private ArrayList<VariableInfo> filedInClassList;
	private ArrayList<VariableInfo> parametersInMethodList;
	private ArrayList<VariableInfo> localsInMethodList;
	private ArrayList<MethodInfo> methodsInClassList;
	private int[] lineCounter;

	public VariableCollectVisitor(int[] lineCounter) {
		filedInClassList = new ArrayList<VariableInfo>();
		parametersInMethodList = new ArrayList<VariableInfo>();
		localsInMethodList = new ArrayList<VariableInfo>();
		methodsInClassList = new ArrayList<MethodInfo>();
		this.lineCounter = lineCounter;
	}

	public ArrayList<VariableInfo> getFiledsInClassList() {
		return filedInClassList;
	}

	public ArrayList<VariableInfo> getParametersInMethodList() {
		return parametersInMethodList;
	}

	public ArrayList<VariableInfo> getLocalInMethodList() {
		return localsInMethodList;
	}
    

	public ArrayList<MethodInfo> getMethodsInClassList() {
		return methodsInClassList;
	}
	
	@Override
	public boolean visit(FieldDeclaration node) {
		TypeInference typeInference = new TypeInference(node.getType()
				.toString());
		for (Object obj : node.fragments()) {
			VariableInfo variableInfo = null;
			VariableDeclarationFragment v = (VariableDeclarationFragment) obj;
			String varName = v.getName().toString();

			if (typeInference.isSimpleType) {
				variableInfo = new VariableInfo(varName, typeInference.type,
						typeInference.isSimpleType, null, node.toString()
								.contains("public"));
			} else {
				variableInfo = new VariableInfo(varName, null,
						typeInference.isSimpleType, typeInference.otherType,
						node.toString().contains("public"));
			}
			filedInClassList.add(variableInfo);
		}

		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if(node.getReturnType2() == null || node.getReturnType2().toString().equals("void")){
			return true;
		}

		List<SingleVariableDeclaration> parameters = node.parameters();
		if(parameters.size() == 0){
			MethodInfo methodInfo = null;
			String methodName = node.getName().toString();

			if(node.getReturnType2() == null) {
				return true;
			}
			TypeInference typeInference = new TypeInference(node.getReturnType2().toString());
			if (typeInference.isSimpleType) {
				methodInfo = new MethodInfo(methodName, typeInference.type,
						typeInference.isSimpleType, null, node.toString()
								.contains("public"), node.toString().contains("static"));
			} else {
				methodInfo = new MethodInfo(methodName, null,
						typeInference.isSimpleType, typeInference.otherType,
						node.toString().contains("public"), node.toString().contains("static"));
			}
			methodsInClassList.add(methodInfo);
		}
		
		for (SingleVariableDeclaration parameter : parameters) {
			TypeInference typeInference = new TypeInference(parameter.getType()
					.toString());
			VariableInfo variableInfo = null;
			if (typeInference.isSimpleType) {
				if (node.getStartPosition() >= lineCounter.length || node.getStartPosition() + node.getLength() >= lineCounter.length){
					variableInfo = new VariableInfo(parameter.getName().toString(), typeInference.type, typeInference.isSimpleType, null, 0, 0);
				}else {
					variableInfo = new VariableInfo(parameter.getName().toString(), typeInference.type, typeInference.isSimpleType, null,
							lineCounter[node.getStartPosition()], lineCounter[node.getStartPosition() + node.getLength()]);
				}
			} else {
				if (node.getStartPosition() >= lineCounter.length || node.getStartPosition() + node.getLength() >= lineCounter.length) {
					variableInfo = new VariableInfo(parameter.getName().toString(), null, typeInference.isSimpleType, typeInference.otherType, 0, 0);
				}
				else {
					variableInfo = new VariableInfo(parameter.getName().toString(), null, typeInference.isSimpleType, typeInference.otherType,
							lineCounter[node.getStartPosition()], lineCounter[node.getStartPosition() + node.getLength()]);
				}
			}
			parametersInMethodList.add(variableInfo);
		}
		return true;
	}


	@Override
	public boolean visit(VariableDeclarationStatement node) {
		for (Object obj : node.fragments()) {
			VariableInfo variableInfo = null;
			VariableDeclarationFragment v = (VariableDeclarationFragment) obj;
			String varName = v.getName().toString();

			TypeInference typeInference = new TypeInference(node.getType()
					.toString());
			
			ASTNode parent = node.getParent();
			
			if (typeInference.isSimpleType) {
				variableInfo = new VariableInfo(varName,
						typeInference.type, typeInference.isSimpleType, null,
						lineCounter[parent.getStartPosition()], lineCounter[parent.getStartPosition() + parent.getLength()], lineCounter[node.getStartPosition()]);
			} else {
				variableInfo = new VariableInfo(varName.toString(),
						null, typeInference.isSimpleType,
						typeInference.otherType,
						lineCounter[parent.getStartPosition()], lineCounter[parent.getStartPosition() + parent.getLength()], lineCounter[node.getStartPosition()]);
			}
			localsInMethodList.add(variableInfo);
		}
		return true;
	}
	
	@Override
	public boolean visit(InfixExpression node) {
		return true;
	}

	@Override
	public boolean visit(SimpleName node) {
		return true;
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		return true;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		return true;
	}

	@Override
	public boolean visit(TryStatement node) {
		return true;
	}

	@Override
	public boolean visit(NumberLiteral node) {
		return true;
	}

	@Override
	public boolean visit(StringLiteral node) {
		return true;
	}

	@Override
	public boolean visit(NullLiteral node) {
		return true;
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		return true;
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		return true;
	}

	@Override
	public boolean visit(TypeLiteral node) {
		return true;
	}

	@Override
	public boolean visit(ReturnStatement node) {
		return true;
	}

	@Override
	public boolean visit(PrefixExpression node) {
		return true;
	}

	@Override
	public boolean visit(ThisExpression node) {
		return true;
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		return true;
	}

	@Override
	public boolean visit(CastExpression node) {
		return true;
	}

	@Override
	public boolean visit(Modifier node) {

		return true;
	}

	@Override
	public boolean visit(TypeDeclaration node) {

		return true;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		return true;
	}

	@Override
	public boolean visit(CompilationUnit node) {
		return true;
	}

	@Override
	public boolean visit(PrimitiveType node) {
		return true;
	}

	@Override
	public boolean visit(ArrayType node) {
		return true;
	}

	@Override
	public boolean visit(ArrayCreation node) {
		return true;
	}

	@Override
	public boolean visit(QualifiedName node) {
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		return true;
	}

	@Override
	public boolean visit(Assignment node) {
		return true;
	}

	@Override
	public boolean visit(SimpleType node) {
		return true;
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		return true;
	}

	@Override
	public boolean visit(CatchClause node) {
		return true;
	}

	@Override
	public boolean visit(Block node) {
		return true;
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		return true;
	}

	@Override
	public boolean visit(IfStatement node) {
		return true;
	}

	@Override
	public boolean visit(UnionType node) {
		return true;
	}

	@Override
	public boolean visit(TypeDeclarationStatement node) {
		return true;
	}

	@Override
	public boolean visit(TextElement node) {
		return false;
	}

	@Override
	public boolean visit(TagElement node) {
		return false;
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		return true;
	}

	@Override
	public boolean visit(QualifiedType node) {
		return true;
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		return true;
	}

	@Override
	public boolean visit(MethodRefParameter node) {
		return false;
	}

	@Override
	public boolean visit(MethodRef node) {
		return false;
	}

	@Override
	public boolean visit(MemberValuePair node) {
		return true;
	}

	@Override
	public boolean visit(MemberRef node) {
		return false;
	}

	@Override
	public boolean visit(LabeledStatement node) {
		return true;
	}

	@Override
	public boolean visit(LineComment node) {
		return false;
	}

	@Override
	public boolean visit(Javadoc node) {
		return false;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		return true;
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		return true;
	}

	@Override
	public boolean visit(BlockComment node) {

		return false;
	}

	@Override
	public boolean visit(AssertStatement node) {
		return true;
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		return true;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		return true;
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		return true;
	}

	@Override
	public boolean visit(ContinueStatement node) {
		return true;
	}

	@Override
	public boolean visit(WhileStatement node) {
		return true;
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		return true;
	}

	@Override
	public boolean visit(EmptyStatement node) {
		return true;
	}

	@Override
	public boolean visit(TypeParameter node) {
		return true;
	}

	@Override
	public boolean visit(ThrowStatement node) {
		return true;
	}

	@Override
	public boolean visit(WildcardType node) {
		return true;
	}

	@Override
	public boolean visit(ArrayAccess node) {
		return true;
	}

	@Override
	public boolean visit(DoStatement node) {
		return true;
	}

	@Override
	public boolean visit(Initializer node) {
		return true;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		return true;
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		return true;
	}

	@Override
	public boolean visit(PostfixExpression node) {
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationExpression node) {
		return true;
	}

	@Override
	public boolean visit(ForStatement node) {
		return true;
	}

	@Override
	public boolean visit(BreakStatement node) {
		return true;
	}

	@Override
	public boolean visit(SwitchCase node) {
		return true;
	}

	@Override
	public boolean visit(SwitchStatement node) {
		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		return true;
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		return true;
	}

	@Override
	public boolean visit(ParameterizedType node) {
		return true;
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		return true;
	}

	@Override
	public boolean visit(ParenthesizedExpression node) {
		return true;
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		return true;
	}

	@Override
	public boolean visit(FieldAccess node) {
		return true;
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		return true;
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		return true;
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		return true;
	}
}
