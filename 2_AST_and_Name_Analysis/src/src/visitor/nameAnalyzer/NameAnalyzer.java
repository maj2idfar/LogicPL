package visitor.nameAnalyzer;

import ast.node.Program;
import ast.node.declaration.*;
import ast.node.statement.ArrayDecStmt;
import ast.node.statement.ForloopStmt;
import ast.node.statement.ImplicationStmt;
import ast.node.statement.VarDecStmt;
import ast.type.primitiveType.IntType;
import compileError.*;
import compileError.Name.*;
import main.grammar.LogicPLParser;
import symbolTable.SymbolTable;
import symbolTable.symbolTableItems.*;
import symbolTable.itemException.ItemAlreadyExistsException;
import symbolTable.symbolTableItems.VariableItem;
import visitor.Visitor;

import java.util.ArrayList;

public class NameAnalyzer extends Visitor<Void> {

    public ArrayList<CompileError> nameErrors = new ArrayList<>();

    private static int number = 0;

    @Override
    public Void visit(Program program) {
        SymbolTable.root = new SymbolTable();
        SymbolTable.push(SymbolTable.root);

        for (FuncDeclaration functionDeclaration : program.getFuncs()) {
            functionDeclaration.accept(this);
        }

        for (var stmt : program.getMain().getMainStatements()) {
            if(stmt instanceof VarDecStmt) {
                stmt.accept(this);
            }

            if(stmt instanceof ArrayDecStmt) {
                stmt.accept(this);
            }

            if(stmt instanceof ImplicationStmt) {
                stmt.accept(this);
            }

            if(stmt instanceof ForloopStmt) {
                stmt.accept(this);
            }
        }

        return null;
    }

    @Override
    public Void visit(FuncDeclaration funcDeclaration) {
        var functionItem = new FunctionItem(funcDeclaration);
        var functionSymbolTable = new SymbolTable(SymbolTable.top, funcDeclaration.getName().getName());
        functionItem.setFunctionSymbolTable(functionSymbolTable);

        boolean not_existed = false;
        do {
            try {
                SymbolTable.top.put(functionItem);
                not_existed = true;
            } catch (ItemAlreadyExistsException var_exist) {
                FunctionRedefinition func_error = new FunctionRedefinition(funcDeclaration.getLine(), funcDeclaration.getName().getName());
                nameErrors.add(func_error);
                functionItem.setName("function" + number);
            }
        } while(!not_existed);

        SymbolTable.push(functionSymbolTable);

        for (ArgDeclaration varDeclaration : funcDeclaration.getArgs()) {
            varDeclaration.accept(this);
        }

        for (var stmt : funcDeclaration.getStatements()) {
            if(stmt instanceof VarDecStmt) {
                stmt.accept(this);
            }

            if(stmt instanceof ArrayDecStmt) {
                stmt.accept(this);
            }

            if(stmt instanceof ImplicationStmt) {
                stmt.accept(this);
            }

            if(stmt instanceof ForloopStmt) {
                stmt.accept(this);
            }
        }

        SymbolTable.pop();
        return null;
    }

    @Override
    public Void visit(VarDecStmt varDeclaration) {
        var variableItem = new VariableItem(varDeclaration);

        boolean not_existed = false;
        do {
            try {
                SymbolTable.top.put(variableItem);
                not_existed = true;
            } catch (ItemAlreadyExistsException var_exist) {
                VariableRedefinition var_error = new VariableRedefinition(varDeclaration.getLine(), varDeclaration.getIdentifier().getName());
                nameErrors.add(var_error);
                variableItem.setName("variable" + number);
                number++;
            }
        } while(!not_existed);

        return null;
    }

    @Override
    public Void visit(ArgDeclaration argDeclaration) {
        var argumentItem = new VariableItem(argDeclaration.getIdentifier().getName(), argDeclaration.getType());

        try {
            SymbolTable.top.put(argumentItem);
        } catch (ItemAlreadyExistsException var_exist) {
            VariableRedefinition var_error = new VariableRedefinition(argDeclaration.getLine(), argDeclaration.getIdentifier().getName());
            nameErrors.add(var_error);
        }

        return null;
    }

    @Override
    public Void visit(ArrayDecStmt arrayDecStmt) {
        var arrayItem = new ArrayItem(arrayDecStmt);

        boolean not_existed = false;
        do {
            try {
                SymbolTable.top.put(arrayItem);
                not_existed = true;
            } catch (ItemAlreadyExistsException var_exist) {
                VariableRedefinition var_error = new VariableRedefinition(arrayDecStmt.getLine(), arrayDecStmt.getIdentifier().getName());
                nameErrors.add(var_error);
                arrayItem.setName("array" + number);
                number++;
            }
        } while(!not_existed);

        return null;
    }

    @Override
    public Void visit(ImplicationStmt implicationStmt) {
        var implicationSymbolTable = new SymbolTable(SymbolTable.top, "implication"+number);
        number++;

        SymbolTable.push(implicationSymbolTable);

        for (var stmt : implicationStmt.getStatements()) {
            if(stmt instanceof VarDecStmt) {
                stmt.accept(this);
            }

            if(stmt instanceof ArrayDecStmt) {
                stmt.accept(this);
            }

            if(stmt instanceof ImplicationStmt) {
                stmt.accept(this);
            }

            if(stmt instanceof ForloopStmt) {
                stmt.accept(this);
            }
        }

        SymbolTable.pop();
        return null;
    }

    @Override
    public Void visit(ForloopStmt forloopStmt) {
        var forloopSymbolTable = new SymbolTable(SymbolTable.top, "forloop"+number);
        number++;

        SymbolTable.push(forloopSymbolTable);

        try {
            var IteratorItem = new VariableItem(forloopStmt.getIterator().getName(), new IntType()); // type is considered integer, but is not important.
            SymbolTable.top.put(IteratorItem);
        } catch(ItemAlreadyExistsException e) {} // never happens.

        for (var stmt : forloopStmt.getStatements()) {
            if(stmt instanceof VarDecStmt) {
                stmt.accept(this);
            }

            if(stmt instanceof ArrayDecStmt) {
                stmt.accept(this);
            }

            if(stmt instanceof ImplicationStmt) {
                stmt.accept(this);
            }

            if(stmt instanceof ForloopStmt) {
                stmt.accept(this);
            }
        }

        SymbolTable.pop();
        return null;
    }
}