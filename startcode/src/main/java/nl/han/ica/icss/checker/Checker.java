package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.MyHanLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.ArrayList;


public class Checker {
    public void check(AST ast) {
        var globalVariables = new MyHanLinkedList<VariableAssignment>();
        for (var child : ast.root.getChildren()) {
            if (child instanceof VariableAssignment) {
                globalVariables.addFirst((VariableAssignment) child);
            }

            if (child instanceof Stylerule) {
                var copy = new MyHanLinkedList<>(globalVariables.getFirstNode());
                checkRuleBody(((Stylerule) child).body, copy);
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
            var variableReference = (VariableReference) ifClause.conditionalExpression;
            var expressionType = checkVarReference(variableReference, scopeVars);
            if (expressionType != ExpressionType.BOOL) {
                ifClause.setError("Illegal IfClause condition: "+variableReference.name+" is not a Boolean");
            }
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

    /**
     * Checks an Declaration ASTNote
     * implements Check (CH04) and allowed Property usage
     * @param declaration the current Declaration ASTNode
     * @param scopeVars list of available declared variables
     */
    private void checkDeclaration(Declaration declaration, MyHanLinkedList<VariableAssignment> scopeVars) {
        var expressionType= checkExpression(declaration.expression, scopeVars);
        var propertyName = declaration.property.name;
        switch (propertyName) {
            case "color":
            case "background-color":
                if (expressionType != ExpressionType.COLOR) {
                    declaration.setError("Illegal use of literal for property: "+propertyName+", it must be a ColorLiteral.");
                }
                break;
            case "width":
            case "height":
                if (expressionType != ExpressionType.PERCENTAGE && expressionType != ExpressionType.PIXEL) {
                    declaration.setError("Illegal use of literal for property: "+propertyName+", a size property should use Percentage or PixelLiteral.");
                }
                break;
            default:
                declaration.setError("Illegal property used: "+propertyName+", Only (color, background-color, width, height) are allowed.");
                break;
        }
    }

    /**
     * Checks an Expression ASTNote,
     * @param expression the current Expression ASTNode.
     * @param scopeVars list of available declared variables.
     * @return the ExpressionType of the given Expression,
     * or UNDEFINED if expression option is not implemented.
     */
    private ExpressionType checkExpression(Expression expression, MyHanLinkedList<VariableAssignment> scopeVars) {
        if (expression instanceof Literal) return getExpressionType(expression, scopeVars);

        if (expression instanceof VariableReference) {
            return checkVarReference((VariableReference) expression, scopeVars);
        }

        if (expression instanceof Operation) {
            return checkOperation((Operation) expression, scopeVars);
        }

        System.out.println("Undefined expression " + expression.getClass().toString());
        return ExpressionType.UNDEFINED;
    }

    /**
     * Checks an Operation ASTNote calls checkExpression recursively,
     * implements Check (CH02, CH03)
     * @param operation the current Operation ASTNode.
     * @param scopeVars list of available declared variables.
     * @return the ExpressionType of the given Operation,
     * or UNDEFINED if Operation results in an error.
     */
    private ExpressionType checkOperation(Operation operation, MyHanLinkedList<VariableAssignment> scopeVars) {
        ExpressionType left = checkExpression(operation.lhs, scopeVars);
        ExpressionType right = checkExpression(operation.rhs, scopeVars);
        if (left == ExpressionType.COLOR || right == ExpressionType.COLOR) {
            operation.setError("Illegal use of ColorLiteral in math operation");
            System.out.println("Color check "+ left.toString() + right.toString());
            return ExpressionType.UNDEFINED;
        }
        if (operation instanceof MultiplyOperation) {
            if (left != ExpressionType.SCALAR && right != ExpressionType.SCALAR) {
                operation.setError("Illegal use of only non ScalarLiterals in multiply operation");
                System.out.println("Mul check "+ left.toString() + right.toString());
                return ExpressionType.UNDEFINED;
            }
            if (left != ExpressionType.SCALAR) return left;
            return right;
        }
        if ((operation instanceof SubtractOperation || operation instanceof AddOperation) && left != right){
            operation.setError("Illegal use of different Literals in add or subtract operations");
            System.out.println("Add/Sub check "+ left.toString() + right.toString());
            return ExpressionType.UNDEFINED;
        }

        //System.out.println("base "+ left.toString() + right.toString());
        return left;
    }

    /**
     * Checks an Expression ASTNote,
     * @param expression the current Expression ASTNode.
     * @param scopeVars list of available declared variables.
     * @return the ExpressionType of the given expression if Literal or call checkExpression recursively.
     */
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
            reference.setError("Illegal use of variable: "+reference.name+", it is undefined or cant be used in current scope");
            return ExpressionType.UNDEFINED;
        }
        return getExpressionType(current.getValue().expression, scopeVars);
    }
}
