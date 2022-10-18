package nl.han.ica.icss.parser;

import java.util.Stack;


import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.datastructures.MyHanStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {
	
	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private IHANStack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new MyHanStack<>();
	}
    public AST getAST() {
        return ast;
    }

	@Override public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		currentContainer.push(new Stylesheet());
	}

	@Override public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		ast.setRoot((Stylesheet) currentContainer.pop());
	}

	@Override public void enterStyleRule(ICSSParser.StyleRuleContext ctx) {
		currentContainer.push(new Stylerule());
	}

	@Override public void exitStyleRule(ICSSParser.StyleRuleContext ctx) {
		var t = currentContainer.pop();
		currentContainer.peek().addChild(t);
	}

	@Override public void enterVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		currentContainer.push(new VariableAssignment());
	}

	@Override public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		var t = currentContainer.pop();
		currentContainer.peek().addChild(t);
	}

	@Override public void enterVariableReference(ICSSParser.VariableReferenceContext ctx) {
		currentContainer.peek().addChild(new VariableReference(ctx.getText()));
	}

	@Override public void enterDecleration(ICSSParser.DeclerationContext ctx) {
		currentContainer.push(new Declaration());
	}

	@Override public void exitDecleration(ICSSParser.DeclerationContext ctx) {
		var t = currentContainer.pop();
		currentContainer.peek().addChild(t);
	}

	@Override public void enterPropertyName(ICSSParser.PropertyNameContext ctx) {
		currentContainer.peek().addChild(new PropertyName(ctx.getText()));
	}

	@Override public void enterBoolLiteral(ICSSParser.BoolLiteralContext ctx) {
		currentContainer.peek().addChild(new BoolLiteral(ctx.getText()));
	}

	@Override public void enterColorLiteral(ICSSParser.ColorLiteralContext ctx) {
		currentContainer.peek().addChild(new ColorLiteral(ctx.getText()));
	}

	@Override public void enterPercentageLiteral(ICSSParser.PercentageLiteralContext ctx) {
		currentContainer.peek().addChild(new PercentageLiteral(ctx.getText()));
	}

	@Override public void enterPixelLiteral(ICSSParser.PixelLiteralContext ctx) {
		currentContainer.peek().addChild(new PixelLiteral(ctx.getText()));
	}

	@Override public void enterScalarLiteral(ICSSParser.ScalarLiteralContext ctx) {
		currentContainer.peek().addChild(new ScalarLiteral(ctx.getText()));
	}

	@Override public void enterClassSelector(ICSSParser.ClassSelectorContext ctx) {
		currentContainer.push(new ClassSelector(ctx.getText()));
	}

	@Override public void exitClassSelector(ICSSParser.ClassSelectorContext ctx) {
		var t = currentContainer.pop();
		currentContainer.peek().addChild(t);
	}

	@Override public void enterIdSelector(ICSSParser.IdSelectorContext ctx) {
		currentContainer.push(new IdSelector(ctx.getText()));
	}

	@Override public void exitIdSelector(ICSSParser.IdSelectorContext ctx) {
		var t = currentContainer.pop();
		currentContainer.peek().addChild(t);
	}

	@Override public void enterTagSelector(ICSSParser.TagSelectorContext ctx) {
		currentContainer.push(new TagSelector(ctx.getText()));
	}

	@Override public void exitTagSelector(ICSSParser.TagSelectorContext ctx) {
		var t = currentContainer.pop();
		currentContainer.peek().addChild(t);
	}
}