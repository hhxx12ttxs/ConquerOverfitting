/*
 * Copyright ÂŠ 2009 Reinier Zwitserloot and Roel Spilker.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.eclipse.handlers;

import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.lang.reflect.Modifier;

import lombok.Synchronized;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil.MemberExistsResult;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.mangosdk.spi.ProviderFor;

/**
 * Handles the {@code lombok.Synchronized} annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
public class HandleSynchronized implements EclipseAnnotationHandler<Synchronized> {
	private static final char[] INSTANCE_LOCK_NAME = "$lock".toCharArray();
	private static final char[] STATIC_LOCK_NAME = "$LOCK".toCharArray();
	
	@Override public boolean handle(AnnotationValues<Synchronized> annotation, Annotation source, EclipseNode annotationNode) {
		int p1 = source.sourceStart -1;
		int p2 = source.sourceStart -2;
		long pos = (((long)p1) << 32) | p2;
		EclipseNode methodNode = annotationNode.up();
		if (methodNode == null || methodNode.getKind() != Kind.METHOD || !(methodNode.get() instanceof MethodDeclaration)) {
			annotationNode.addError("@Synchronized is legal only on methods.");
			return true;
		}
		
		MethodDeclaration method = (MethodDeclaration)methodNode.get();
		if (method.isAbstract()) {
			annotationNode.addError("@Synchronized is legal only on concrete methods.");
			return true;
		}
		
		char[] lockName = annotation.getInstance().value().toCharArray();
		boolean autoMake = false;
		if (lockName.length == 0) {
			autoMake = true;
			lockName = method.isStatic() ? STATIC_LOCK_NAME : INSTANCE_LOCK_NAME;
		}
		
		if (fieldExists(new String(lockName), methodNode) == MemberExistsResult.NOT_EXISTS) {
			if (!autoMake) {
				annotationNode.addError("The field " + new String(lockName) + " does not exist.");
				return true;
			}
			FieldDeclaration fieldDecl = new FieldDeclaration(lockName, 0, -1);
			Eclipse.setGeneratedBy(fieldDecl, source);
			fieldDecl.declarationSourceEnd = -1;
			
			fieldDecl.modifiers = (method.isStatic() ? Modifier.STATIC : 0) | Modifier.FINAL | Modifier.PRIVATE;
			
			//We use 'new Object[0];' because quite unlike 'new Object();', empty arrays *ARE* serializable!
			ArrayAllocationExpression arrayAlloc = new ArrayAllocationExpression();
			Eclipse.setGeneratedBy(arrayAlloc, source);
			arrayAlloc.dimensions = new Expression[] { new IntLiteral(new char[] { '0' }, 0, 0) };
			Eclipse.setGeneratedBy(arrayAlloc.dimensions[0], source);
			arrayAlloc.type = new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, new long[] { 0, 0, 0 });
			Eclipse.setGeneratedBy(arrayAlloc.type, source);
			fieldDecl.type = new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, new long[] { 0, 0, 0 });
			Eclipse.setGeneratedBy(fieldDecl.type, source);
			fieldDecl.initialization = arrayAlloc;
			injectFieldSuppressWarnings(annotationNode.up().up(), fieldDecl);
		}
		
		if (method.statements == null) return false;
		
		Block block = new Block(0);
		Eclipse.setGeneratedBy(block, source);
		block.statements = method.statements;
		Expression lockVariable;
		if (method.isStatic()) lockVariable = new QualifiedNameReference(new char[][] {
				methodNode.up().getName().toCharArray(), lockName }, new long[] { pos, pos }, p1, p2);
		else {
			lockVariable = new FieldReference(lockName, pos);
			ThisReference thisReference = new ThisReference(p1, p2);
			Eclipse.setGeneratedBy(thisReference, source);
			((FieldReference)lockVariable).receiver = thisReference;
		}
		Eclipse.setGeneratedBy(lockVariable, source);
		
		method.statements = new Statement[] {
				new SynchronizedStatement(lockVariable, block, 0, 0)
		};
		Eclipse.setGeneratedBy(method.statements[0], source);
		
		methodNode.rebuild();
		
		return true;
	}
}

