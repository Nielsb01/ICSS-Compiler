package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.datastructures.MyHanLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.ArrayList;
import java.util.HashMap;



public class Checker {

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
            var varDeclaration = checkVarReference((VariableReference) ifClause.conditionalExpression, scopeVars);
            if (varDeclaration == null || !(varDeclaration.expression instanceof BoolLiteral)) ifClause.setError("Condition expression is not a Boolean");
        }

        /*
        checking Literal unnecessary, because parser only parses on (variableReference | boolLiteral),
        if not variableReference then its already boolLiteral. Could change the if clause above to catch all the instances NOT BoolLiteral.
         */

        if(ifClause.elseClause != null) {
            var copy = new MyHanLinkedList<>(scopeVars.getFirstNode());
            checkRuleBody(ifClause.elseClause.body, copy);
        }
        checkRuleBody(ifClause.body, scopeVars);
    }

    private void checkDeclaration(Declaration declaration, MyHanLinkedList<VariableAssignment> scopeVars) {
        checkExpression(declaration.expression, scopeVars);
    }

    /**
     * Checks an Expression ASTNote calls itself recursively,
     * implements Check (CH03)
     * @param expression the current Expression ASTNode
     * @param scopeVars list of available declared variables
     */
    private void checkExpression(Expression expression, MyHanLinkedList<VariableAssignment> scopeVars) {
        if (expression instanceof VariableReference) {
            checkVarReference((VariableReference) expression, scopeVars);
            return;
        }

        if (expression instanceof Literal) return;

        var usesColor = false;
        var usesOperation = false;
        for (var child : expression.getChildren()) {
            if (child instanceof ColorLiteral) usesColor = true;
            if (child instanceof Operation) usesOperation = true;
            checkExpression((Expression) child, scopeVars);
        }

        if (usesColor && usesOperation){
            expression.setError("Illegal use of ColorLiteral in math operation");
        }
    }

    /**
     * Checks a VariableReference ASTNote
     * implements Check (CH01, CHO6)
     * @param reference the current VariableReference ASTNode
     * @param scopeVars list of available declared variables
     * @return VariableAssignment, null if not declared.
     */
    private VariableAssignment checkVarReference(VariableReference reference, MyHanLinkedList<VariableAssignment> scopeVars) {
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
            return null;
        }
        return current.getValue();
    }
}
