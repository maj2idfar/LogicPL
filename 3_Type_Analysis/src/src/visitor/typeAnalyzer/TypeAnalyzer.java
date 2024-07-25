package visitor.typeAnalyzer;

import ast.node.Program;
import ast.node.declaration.Declaration;
import ast.node.declaration.FuncDeclaration;
import ast.node.declaration.MainDeclaration;
import ast.node.expression.*;
import ast.node.expression.operators.BinaryOperator;
import ast.node.statement.AssignStmt;
import ast.node.statement.ForloopStmt;
import ast.node.statement.ImplicationStmt;
import ast.node.statement.VarDecStmt;
import ast.node.statement.ReturnStmt;
import ast.node.statement.PrintStmt;
import ast.node.statement.ArrayDecStmt;
import ast.type.NoType;
import ast.type.Type;
import compileError.CompileError;
import compileError.Type.FunctionNotDeclared;
import compileError.Type.LeftSideNotLValue;
import compileError.Type.UnsupportedOperandType;
import compileError.Type.ConditionTypeNotBool;
import symbolTable.SymbolTable;
import symbolTable.itemException.ItemAlreadyExistsException;
import symbolTable.itemException.ItemNotFoundException;
import symbolTable.symbolTableItems.ForLoopItem;
import symbolTable.symbolTableItems.FunctionItem;
import symbolTable.symbolTableItems.MainItem;
import symbolTable.symbolTableItems.VariableItem;
import visitor.Visitor;
import ast.type.primitiveType.BooleanType;
import ast.type.primitiveType.FloatType;
import ast.type.primitiveType.IntType;

import java.util.ArrayList;

public class TypeAnalyzer extends Visitor<Void> {
    public ArrayList<CompileError> typeErrors = new ArrayList<>();
    ExpressionTypeChecker expressionTypeChecker = new ExpressionTypeChecker(typeErrors);

    @Override
    public Void visit(Program program) {
        for(var functionDec : program.getFuncs()) {
            functionDec.accept(this);
        }

        program.getMain().accept(this);

        return null;
    }

    @Override
    public Void visit(FuncDeclaration funcDeclaration) {
        try {
            FunctionItem functionItem = (FunctionItem)  SymbolTable.root.get(FunctionItem.STARTKEY + funcDeclaration.getName().getName());
            SymbolTable.push((functionItem.getFunctionSymbolTable()));
        } catch (ItemNotFoundException e) {
            //unreachable
        }

        for (var stmt : funcDeclaration.getStatements()) {
            stmt.accept(this);
        }

        SymbolTable.pop();

        return null;
    }

    @Override
    public Void visit(MainDeclaration mainDeclaration) {
        var mainItem = new MainItem(mainDeclaration);
        var mainSymbolTable = new SymbolTable(SymbolTable.top, "main");
        mainItem.setMainItemSymbolTable(mainSymbolTable);

        SymbolTable.push(mainItem.getMainItemSymbolTable());

        for (var stmt : mainDeclaration.getMainStatements()) {
            stmt.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(ForloopStmt forloopStmt) {
        try {
            ForLoopItem forLoopItem = (ForLoopItem) SymbolTable.root.get(FunctionItem.STARTKEY + forloopStmt.toString());
            SymbolTable.push((forLoopItem.getForLoopSymbolTable()));
        } catch (ItemNotFoundException e) {

        }

        Type t = forloopStmt.getArrayName().accept(expressionTypeChecker);

        try {
            var IteratorItem = new VariableItem(forloopStmt.getIterator().getName(), t); // type is considered integer, but is not important.
            SymbolTable.top.put(IteratorItem);
        } catch(ItemAlreadyExistsException e) {} // never happens.

        for (var stmt : forloopStmt.getStatements()) {
            stmt.accept(this);
        }

        SymbolTable.pop();

        return null;
    }

    @Override
    public Void visit(AssignStmt assignStmt) {
        Type tl = assignStmt.getLValue().accept(expressionTypeChecker);
        Type tr = assignStmt.getRValue().accept(expressionTypeChecker);

        if (!(tl instanceof NoType) && !(tr instanceof NoType)) {
            if (!expressionTypeChecker.isLvalue(assignStmt.getLValue()))
                typeErrors.add(new LeftSideNotLValue(assignStmt.getLine()));
            if (!expressionTypeChecker.sameType(tl, tr)) {
                UnsupportedOperandType exception = new UnsupportedOperandType(assignStmt.getRValue().getLine(), "assign");
                typeErrors.add(exception);
            }
        } else {
            UnsupportedOperandType exception = new UnsupportedOperandType(assignStmt.getRValue().getLine(), "assign");
            typeErrors.add(exception);
        }

        return null;
    }

    @Override
    public Void visit(ImplicationStmt implicationStmt) {
        Type t = implicationStmt.getCondition().accept(expressionTypeChecker);

        if (!(t instanceof BooleanType)) {
            typeErrors.add(new ConditionTypeNotBool(implicationStmt.getCondition().getLine()));
        }

        for (var stmt : implicationStmt.getStatements()) {
            stmt.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(VarDecStmt varDecStmt) {
        if(varDecStmt.getInitialExpression() != null) {
            Type exprType = varDecStmt.getInitialExpression().accept(expressionTypeChecker);

            if(!expressionTypeChecker.sameType(varDecStmt.getType(), exprType)) {
                UnsupportedOperandType exception = new UnsupportedOperandType(varDecStmt.getLine(), "assign");
                typeErrors.add(exception);
            }
        }

        return null;
    }

    @Override
    public Void visit(ArrayDecStmt arrayDecStmt) {
        for (var varDec : arrayDecStmt.getInitialValues()) {
            Type varType = varDec.accept(expressionTypeChecker);

            if(!expressionTypeChecker.sameType(arrayDecStmt.getType(), varType)) {
                UnsupportedOperandType exception = new UnsupportedOperandType(arrayDecStmt.getLine(), "assign");
                typeErrors.add(exception);
            }
        }

        return null;
    }

    @Override
    public Void visit(PrintStmt printStmt) {
        printStmt.getArg().accept(expressionTypeChecker);

        return null;
    }

    @Override
    public Void visit(ReturnStmt returnStmt) { // check type of expression with return type of function?
        if(returnStmt.getExpression() != null) {
            returnStmt.getExpression().accept(expressionTypeChecker);
        }

        return null;
    }
}