package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.datastructures.MyHanLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.ArrayList;
import java.util.HashMap;

public class Evaluator implements Transform {

    @Override
    public void apply(AST ast) {
        var globalVariables = new MyHanLinkedList<VariableAssignment>();
        for (var child : ast.root.getChildren()) {
            if (child instanceof VariableAssignment) {
                globalVariables.addFirst((VariableAssignment) child);
                transformVariableAssignment((VariableAssignment) child, globalVariables);
            }

            if (child instanceof Stylerule) {
                var stylerule = (Stylerule) child;
                stylerule.body = transformRuleBody(stylerule.body, globalVariables);
            }
        }
    }

    private ArrayList<ASTNode> transformRuleBody(ArrayList<ASTNode> body, MyHanLinkedList<VariableAssignment> scopeVars) {
        var temp = new ArrayList<ASTNode>();
        for (var child : body) {
            if (child instanceof VariableAssignment) {
                scopeVars.addFirst((VariableAssignment) child);
                transformVariableAssignment((VariableAssignment) child, scopeVars);
            }

            if (child instanceof Declaration) {
                transformDeclaration((Declaration) child, scopeVars);
                temp.add(child);
                //System.out.println(temp.toString());
            }

            if (child instanceof IfClause) {
                temp.addAll(transformIfClause((IfClause) child, scopeVars));
            }
        }
        return temp;
    }

    private void transformVariableAssignment(VariableAssignment variableAssignment, MyHanLinkedList<VariableAssignment> scopeVars) {
        variableAssignment.expression = transformExpression(variableAssignment.expression, scopeVars);
    }

    private void transformDeclaration(Declaration declaration, MyHanLinkedList<VariableAssignment> scopeVars) {
        declaration.expression = transformExpression(declaration.expression, scopeVars);
    }

    private ArrayList<ASTNode> transformIfClause(IfClause ifClause, MyHanLinkedList<VariableAssignment> scopeVars) {
        ifClause.conditionalExpression = transformExpression(ifClause.conditionalExpression, scopeVars);
        //System.out.println("con "+ ifClause.conditionalExpression);

        if (((BoolLiteral) ifClause.conditionalExpression).value) {
            ifClause.elseClause= null;
        }
        else {
            if (ifClause.elseClause == null) {
                ifClause.body = new ArrayList<>();
            }
            else {
                ifClause.body = ifClause.elseClause.body;
                ifClause.elseClause = null;
            }
        }
        return transformRuleBody(ifClause.body, scopeVars);
    }

    private Literal transformExpression(Expression expression, MyHanLinkedList<VariableAssignment> scopeVars) {
        if (expression instanceof Operation) {
            return transformOperation((Operation) expression, scopeVars);
        }

        if (expression instanceof VariableReference) {
            return getVariableLiteral((VariableReference) expression, scopeVars);
        }

        if (expression instanceof Literal) {
            return (Literal) expression;
        }

        System.out.println("transformExpression returned null");
        return null;
    }

    private Literal transformOperation(Operation operation, MyHanLinkedList<VariableAssignment> scopeVars) {
        Literal leftLiteral = transformExpression(operation.lhs, scopeVars);
        Literal rightLiteral = transformExpression(operation.rhs, scopeVars);
        //System.out.println("literal "+ leftLiteral.toString() + rightLiteral.toString());

        var leftValue = getLiteralValue(leftLiteral);
        var rightValue= getLiteralValue(rightLiteral);;
        System.out.println("value "+ leftValue + "|" + rightValue);

        if (operation instanceof AddOperation) {
            System.out.println("Add "+ (leftValue + rightValue));
            return createSumLiteral(leftLiteral, leftValue + rightValue);
        }
        if (operation instanceof SubtractOperation) {
            System.out.println("Sub "+ (leftValue - rightValue));
            return createSumLiteral(leftLiteral, leftValue - rightValue);
        }
        if (operation instanceof MultiplyOperation) {
            System.out.println("Mul "+ (leftValue * rightValue));
            var type = leftLiteral instanceof ScalarLiteral ? rightLiteral : leftLiteral;
            return createSumLiteral(type, leftValue * rightValue);
        }

        System.out.println("transformOperation returned null");
        return null;
    }

    private Literal getVariableLiteral(VariableReference variableReference, MyHanLinkedList<VariableAssignment> scopeVars) {
        var currentNode = scopeVars.getFirstNode();

        while (currentNode!= null) {
            var variableAssignment = currentNode.getValue();
            var name = variableAssignment.name.name;
            if (name.equals(variableReference.name)) {
                //System.out.println("getVariableLiteral returned " + variableAssignment.expression.toString());
                return (Literal) variableAssignment.expression;
            }
            currentNode = currentNode.getNext();
        }

        System.out.println("getVariableLiteral returned null");
        return null;
    }

    private int getLiteralValue(Literal literal){
        if (literal instanceof PercentageLiteral) return ((PercentageLiteral) literal).value;
        if (literal instanceof PixelLiteral) return ((PixelLiteral) literal).value;
        return ((ScalarLiteral) literal).value;
    }

    private Literal createSumLiteral(Literal type, int value) {
        if (type instanceof PercentageLiteral) return new PercentageLiteral(value);
        if (type instanceof PixelLiteral) return new PixelLiteral(value);
        return new ScalarLiteral(value);
    }
}
