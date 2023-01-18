/*
# MIT License
#
# Copyright (c) 2022 LASER-UMASS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
# ==============================================================================
*/

package parser;

import parser.JavaStatement;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;


public class MethodVisitor{

	static ArrayList<JavaStatement> javaStatements = new ArrayList<JavaStatement>();
	protected static Logger logger = Logger.getLogger(MethodVisitor.class);

	
	public static ArrayList<JavaStatement> getStatements(final CompilationUnit cu, final String filename) {			
		cu.accept(new ASTVisitor() {
 
			public boolean visit (AssertStatement node) {
				int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
			
			public boolean visit (Block node) {
				int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
			
			public boolean visit (BreakStatement node) {
				int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
			public boolean visit (ConstructorInvocation node) {
				int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
			
			public boolean visit (ContinueStatement node) {
				int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
			
			public boolean visit (DoStatement node) {
				int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
			public boolean visit (EmptyStatement node) {
				int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
			
			public boolean visit (EnhancedForStatement node) {
				int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
			
			public boolean visit (ExpressionStatement node) {
				int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
			
			public boolean visit (ForStatement node) {
				int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		   
			public boolean visit (IfStatement node) {
				int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		   
			public boolean visit (LabeledStatement node) {
				int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
			
			public boolean visit (ReturnStatement node) {
				int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
			
			public boolean visit (SuperConstructorInvocation node) {
				int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
			
			public boolean visit (SwitchCase node) {
				int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		
			public boolean visit (SwitchStatement node) {
				int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
			
			public boolean visit (SynchronizedStatement node) {
				int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
			
			public boolean visit (ThrowStatement node) {
				int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
			
			public boolean visit (TryStatement node) {
				int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
			public boolean visit (TypeDeclarationStatement node) {
				int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
			public boolean visit (VariableDeclarationStatement node) {
				int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (WhileStatement node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (MarkerAnnotation node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (NormalAnnotation node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (ArrayAccess node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (ArrayCreation node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (ArrayInitializer node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (Assignment node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (BooleanLiteral node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (CastExpression node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (CharacterLiteral node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (ClassInstanceCreation node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (ConditionalExpression node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (CreationReference node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (ExpressionMethodReference node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (FieldAccess node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (InfixExpression node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    
		    public boolean visit (InstanceofExpression node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (LambdaExpression node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (MethodInvocation node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (SimpleName node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (QualifiedName node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (NullLiteral node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (NumberLiteral node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (ParenthesizedExpression node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (PostfixExpression node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (PrefixExpression node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (StringLiteral node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (SuperFieldAccess node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (SuperMethodInvocation node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (SuperMethodReference node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (ThisExpression node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (TypeLiteral node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (TypeMethodReference node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		    public boolean visit (VariableDeclarationExpression node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    public boolean visit (AnonymousClassDeclaration node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    public boolean visit (SingleVariableDeclaration node) {
		    	int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
			public boolean visit (VariableDeclarationFragment node) {
				int lineNumber = cu.getLineNumber(node.getStartPosition());
				String content = node.toString();
				String comments = "";
				JavaStatement stmt = new JavaStatement(node, filename, content.trim(), comments.trim(), lineNumber);
				javaStatements.add(stmt);
				return true;
			}
		    
		});
		return javaStatements;
	}
}