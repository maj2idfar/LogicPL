package visitor.typeAnalyzer;

import ast.node.expression.*;
import ast.node.expression.operators.BinaryOperator;
import ast.node.expression.operators.UnaryOperator;
import ast.node.expression.values.IntValue;
import ast.node.expression.values.FloatValue;
import ast.node.expression.values.BooleanValue;
import ast.node.expression.ArrayAccess;
import ast.type.NoType;
import ast.type.Type;
import ast.type.primitiveType.BooleanType;
import ast.type.primitiveType.FloatType;
import ast.type.primitiveType.IntType;
import compileError.CompileError;
import compileError.Type.FunctionNotDeclared;
import compileError.Type.UnsupportedOperandType;
import compileError.Type.VarNotDeclared;
import symbolTable.SymbolTable;
import symbolTable.itemException.ItemNotFoundException;
import symbolTable.symbolTableItems.ArrayItem;
import symbolTable.symbolTableItems.FunctionItem;
import symbolTable.symbolTableItems.VariableItem;
import visitor.Visitor;

import java.util.ArrayList;

public class ExpressionTypeChecker extends Visitor<Type> {
    public ArrayList<CompileError> typeErrors;
    public ExpressionTypeChecker(ArrayList<CompileError> typeErrors){
        this.typeErrors = typeErrors;
    }

    public boolean sameType(Type el1, Type el2){
        //TODO check the two type are same or not
        if (el1 instanceof NoType || el2 instanceof NoType)
            return false;
        if (el1 instanceof IntType && el2 instanceof IntType)
            return true;
        if (el1 instanceof BooleanType && el2 instanceof BooleanType )
            return true;
        if (el1 instanceof FloatType && el2 instanceof FloatType)
            return true;
        return false;
    }

    public boolean isLvalue(Expression expr){ ///????????
        //TODO check the expr are lvalue or not
        if ( expr instanceof Identifier || expr instanceof ArrayAccess){ ////// shak daram
            return true;
        }
        return false;
    }

    @Override
    public Type visit(UnaryExpression unaryExpression) {
        Expression uExpr = unaryExpression.getOperand();
        Type expType = uExpr.accept(this);
        UnaryOperator operator = unaryExpression.getUnaryOperator();

        //TODO check errors and return the type
        if ( operator.equals(UnaryOperator.not)){
            if ( expType instanceof NoType) {
//                UnsupportedOperandType exception = new UnsupportedOperandType(uExpr.getLine(),operator.name());
//                typeErrors.add(exception);
                return new NoType();
            }
            if ( expType instanceof BooleanType) {
                return new BooleanType();
            }
            else {
                UnsupportedOperandType exception = new UnsupportedOperandType(uExpr.getLine(),operator.name());
                typeErrors.add(exception);
                return new NoType();
            }
        }
        else{ // + or -
            if(expType instanceof IntType) {
                return new IntType();
            }
            else if ( expType instanceof FloatType){ //////?????
                return new FloatType();
            }
            else if ( expType instanceof NoType) {
//                typeErrors.add(new UnsupportedOperandType(unaryExpression.getLine(), operator.name()));
                return new NoType();
            }
            else {
                typeErrors.add(new UnsupportedOperandType(unaryExpression.getLine(), operator.name()));
                return new NoType();
            }
        }
    }

    @Override
    public Type visit(BinaryExpression binaryExpression) {
        Type tl = binaryExpression.getLeft().accept(this);
        Type tr = binaryExpression.getRight().accept(this);

        BinaryOperator operator =  binaryExpression.getBinaryOperator();

        if ( operator.equals(BinaryOperator.and) || operator.equals(BinaryOperator.or)){
            if ((tl instanceof BooleanType) && (tr instanceof BooleanType)) {
                return new BooleanType();
            }
            if ((tl instanceof NoType || tl instanceof BooleanType) && (tr instanceof BooleanType || tr instanceof NoType)) {
//                UnsupportedOperandType exception = new UnsupportedOperandType(binaryExpression.getRight().getLine(), operator.name());
//                typeErrors.add(exception);
                return new NoType();
            }
        }
        else if(operator.equals(BinaryOperator.eq) || operator.equals(BinaryOperator.neq)){ /////??????
            if(!sameType(tl,tr)) {
                UnsupportedOperandType exception = new UnsupportedOperandType(binaryExpression.getRight().getLine(), operator.name());
                typeErrors.add(exception);
                return new NoType();
            }
            else if((tl instanceof NoType) && (tr instanceof NoType)) {
//                UnsupportedOperandType exception = new UnsupportedOperandType(binaryExpression.getRight().getLine(), operator.name());
//                typeErrors.add(exception);
                return new NoType();
            }
            else {
                return new BooleanType();
            }
        }
        else if(operator.equals(BinaryOperator.gt) || operator.equals(BinaryOperator.lt) || operator.equals(BinaryOperator.gte) || operator.equals(BinaryOperator.lte)){
            if(!sameType(tl, tr)) {
                UnsupportedOperandType exception = new UnsupportedOperandType(binaryExpression.getRight().getLine(), operator.name());
                typeErrors.add(exception);
                return new NoType();
            }
            if (tl instanceof IntType && tr instanceof IntType)
                return new BooleanType();
            if( tl instanceof FloatType && tr instanceof FloatType) //////??????
                return new BooleanType();
            if ((tl instanceof NoType || tl instanceof IntType) && (tr instanceof IntType || tr instanceof NoType) ) {
//                UnsupportedOperandType exception = new UnsupportedOperandType(binaryExpression.getRight().getLine(), operator.name());
//                typeErrors.add(exception);
                return new NoType();
            }
            if ((tl instanceof NoType || tl instanceof FloatType) && (tr instanceof FloatType || tr instanceof NoType) ) { //////?????
//                UnsupportedOperandType exception = new UnsupportedOperandType(binaryExpression.getRight().getLine(), operator.name());
//                typeErrors.add(exception);
                return new NoType();
            }
        }
        else if (operator.equals(BinaryOperator.assign)){
            Expression lpart = binaryExpression.getLeft();

            if( isLvalue(lpart)){
                if ((tl instanceof IntType) && (tr instanceof IntType))
                    return new IntType();
                if ((tl instanceof FloatType) && (tr instanceof FloatType))
                    return new FloatType();
                if ((tl instanceof BooleanType) && (tr instanceof BooleanType))
                    return new BooleanType();
//                else {
//                    UnsupportedOperandType exception = new UnsupportedOperandType(binaryExpression.getRight().getLine(), operator.name());
//                    typeErrors.add(exception);
//                    return new NoType();
//                }
            }
//            else {
//                UnsupportedOperandType exception = new UnsupportedOperandType(binaryExpression.getRight().getLine(), operator.name());
//                typeErrors.add(exception);
//                return new NoType();
//            }
        }
        else { // + or - or / or * or %
            if (tl instanceof IntType && tr instanceof IntType)
                return new IntType();
            if (tl instanceof FloatType && tr instanceof FloatType)
                return new FloatType();
            if ((tl instanceof NoType || tl instanceof IntType) && (tr instanceof IntType || tr instanceof NoType)) {
//                UnsupportedOperandType exception = new UnsupportedOperandType(binaryExpression.getRight().getLine(), operator.name());
//                typeErrors.add(exception);
                return new NoType();
            }
            if ((tl instanceof NoType || tl instanceof FloatType) && (tr instanceof FloatType || tr instanceof NoType)) {
//                UnsupportedOperandType exception = new UnsupportedOperandType(binaryExpression.getRight().getLine(), operator.name());
//                typeErrors.add(exception);
                return new NoType();
            }
        }

        UnsupportedOperandType exception = new UnsupportedOperandType(binaryExpression.getLeft().getLine(), operator.name());
        typeErrors.add(exception);
        return new NoType();
    }

    @Override
    public Type visit(Identifier identifier) {
        try {
            VariableItem variableItem = (VariableItem) SymbolTable.top.get(VariableItem.STARTKEY + identifier.getName());
            return variableItem.getType();
        } catch (ItemNotFoundException e) {
            typeErrors.add(new VarNotDeclared(identifier.getLine(), identifier.getName()));
            return new NoType();
        }
    }

    @Override
    public Type visit(ArrayAccess arrayAccess) {
        try {
            VariableItem arrayItem = (VariableItem) SymbolTable.top.get(VariableItem.STARTKEY + arrayAccess.getName());

            Type indexType = arrayAccess.getIndex().accept(this);
            if(!(indexType instanceof IntType)) {
                UnsupportedOperandType exception = new UnsupportedOperandType(arrayAccess.getLine(), "brackets");
                typeErrors.add(exception);
            }

            return arrayItem.getType();
        } catch (ItemNotFoundException e) {
            typeErrors.add(new VarNotDeclared(arrayAccess.getLine(), arrayAccess.getName()));
            return new NoType();
        }
    }

    @Override
    public Type visit(FunctionCall functionCall) {
        try {
            FunctionItem functionItem = (FunctionItem) SymbolTable.root.get(FunctionItem.STARTKEY + functionCall.getUFuncName().getName());

            if(functionItem.getHandlerDeclaration().getArgs().size() != functionCall.getArgs().size()){
                typeErrors.add(new FunctionNotDeclared(functionCall.getLine(), functionCall.getUFuncName().getName()));
                return new NoType();
            }

            for (int i = 0; i < functionItem.getHandlerDeclaration().getArgs().size(); i++) {
                Type argType = functionItem.getHandlerDeclaration().getArgs().get(i).getType();
                Type callType = functionCall.getArgs().get(i).accept(this);

                if(!sameType(argType, callType)){
                    typeErrors.add(new FunctionNotDeclared(functionCall.getLine(), functionCall.getUFuncName().getName()));
                    return new NoType();
                }
            }

            return functionItem.getHandlerDeclaration().getType();
        } catch (ItemNotFoundException e) {
            typeErrors.add(new FunctionNotDeclared(functionCall.getLine(), functionCall.getUFuncName().getName()));
            return new NoType();
        }
    }

    @Override
    public Type visit(QueryExpression queryExpression) {
        if(queryExpression.getVar() == null) {
            return new NoType(); // how to return actual type of the return list?
        }

        else {
            Type varType = queryExpression.getVar().accept(this);

            if(varType instanceof NoType) {
                return new NoType();
            }

            else {
                return new BooleanType();
            }
        }
    }

    @Override
    public Type visit(IntValue value) {
        return new IntType();
    }

    @Override
    public Type visit(FloatValue value) {
        return new FloatType();
    }

    @Override
    public Type visit(BooleanValue value) {
        return new BooleanType();
    }
}
