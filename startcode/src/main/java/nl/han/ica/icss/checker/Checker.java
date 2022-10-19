package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.datastructures.MyHanLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.ArrayList;
import java.util.HashMap;



public class Checker {
    private final String WIDTH = "width";

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public Checker() {
        this.variableTypes = new MyHanLinkedList<>();
    }

    public void check(AST ast) {
        var globalVariables = new MyHanLinkedList<VariableAssignment>();
        for (var child : ast.root.getChildren()) {
            if (child instanceof VariableAssignment) {
                globalVariables.addFirst((VariableAssignment) child);
            }

            if (child instanceof Stylerule) {
                checkRuleBody(((Stylerule) child).body, globalVariables);
            }
        }
    }

    private void checkRuleBody(ArrayList<ASTNode> body, MyHanLinkedList<VariableAssignment> scopeVars) {
        for (var child : body) {
            if (child instanceof VariableAssignment) {
                scopeVars.addFirst((VariableAssignment) child);
            }

            if (child instanceof Declaration) {
                checkDeclaration((Declaration) child, scopeVars);
            }

            if (child instanceof IfClause) {
                checkIfClause((IfClause) child, scopeVars);
            }
        }
    }

    /**
     * Checks an IfClause ASTNote
     * implements Check (CH05)
     * @param ifClause the current IfClause ASTNode
     * @param scopeVars list of available declared variables
     */
    private void checkIfClause(IfClause ifClause, MyHanLinkedList<VariableAssignment> scopeVars) {
        if (ifClause.conditionalExpression instanceof VariableReference) {
            var expressionType = checkVarReference((VariableReference) ifClause.conditionalExpression, scopeVars);
            if (expressionType != ExpressionType.BOOL) ifClause.setError("Condition expression is not a Boolean");
        }

        /*
        checking Literal unnecessary, because parser only parses on (variableReference | boolLiteral),
        if not variableReference then its already boolLiteral.
        The if clause above could be changed to catch all the instances NOT BoolLiteral.
         */

        if(ifClause.elseClause != null) {
            var copy = new MyHanLinkedList<>(scopeVars.getFirstNode());
            checkRuleBody(ifClause.elseClause.body, copy);
        }
        checkRuleBody(ifClause.body, scopeVars);
    }

    private void checkDeclaration(Declaration declaration, MyHanLinkedList<VariableAssignment> scopeVars) {
        checkExpression(declaration.expression, scopeVars);
        //TODO implment CH04
        //if (declaration.property.equals(WIDTH) && !())
    }

    /**
     * Checks an Expression ASTNote calls itself recursively,
     * implements Check (CH03)
     * @param expression the current Expression ASTNode.
     * @param scopeVars list of available declared variables.
     * @return the ExpressionType of the given expression.
     */
    private ExpressionType checkExpression(Expression expression, MyHanLinkedList<VariableAssignment> scopeVars) {
        if (expression instanceof Literal) return getExpressionType(expression, scopeVars);

        if (expression instanceof VariableReference) {
            return checkVarReference((VariableReference) expression, scopeVars);
        }

        var usesPixels = false;
        var usesPercentages = false;
        var usesColor = false;
        Operation operation = null;
        for (var child : expression.getChildren()) {
            if (child instanceof ColorLiteral) usesColor = true;
            if (child instanceof PixelLiteral) usesPixels = true;
            if (child instanceof PercentageLiteral) usesPercentages = true;
            if (child instanceof Operation) operation = (Operation) child;
            var expressionType = checkExpression((Expression) child, scopeVars);
        }
        //TODO implment CH02
        if (operation != null){
            if (usesColor && operation instanceof MultiplyOperation) {
                expression.setError("Illegal use of ColorLiteral in math operation");
            }
            if (usesPercentages && usesPixels && !(operation instanceof MultiplyOperation)) {
                expression.setError("Illegal use of PixelLiteral and ");
            }
        }

        return ExpressionType.UNDEFINED;
    }

    private ExpressionType getExpressionType(Expression expression, MyHanLinkedList<VariableAssignment> scopeVars) {
        if (expression instanceof Literal) {
            if (expression instanceof PercentageLiteral) {
                return ExpressionType.PERCENTAGE;
            } else if (expression instanceof PixelLiteral) {
                return ExpressionType.PIXEL;
            } else if (expression instanceof ColorLiteral) {
                return ExpressionType.COLOR;
            } else if (expression instanceof ScalarLiteral) {
                return ExpressionType.SCALAR;
            } else if (expression instanceof BoolLiteral) {
                return ExpressionType.BOOL;
            }
        }
        return checkExpression(expression, scopeVars);
    }

    /**
     * Checks a VariableReference ASTNote
     * implements Check (CH01, CHO6)
     * @param reference the current VariableReference ASTNode
     * @param scopeVars list of available declared variables
     * @return the ExpressionType of a declared variable or UNDEFINED if not declared.
     */
    private ExpressionType checkVarReference(VariableReference reference, MyHanLinkedList<VariableAssignment> scopeVars) {
        var varIsDeclared = false;
        var current = scopeVars.getFirstNode();

        while (current!= null) {
            if (current.getValue().name.name.equals(reference.name)) {
                varIsDeclared = true;
                break;
            }
            current = current.getNext();
        }

        if (!varIsDeclared) {
            reference.setError("Variable:" + reference.name + "is undefined or cant be used in current scope");
            return ExpressionType.UNDEFINED;
        }
        return getExpressionType(current.getValue().expression, scopeVars);
    }
}
