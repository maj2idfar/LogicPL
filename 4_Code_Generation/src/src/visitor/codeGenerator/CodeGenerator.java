package visitor.codeGenerator;

import ast.node.expression.Expression;
import ast.node.expression.FunctionCall;
import ast.node.statement.AssignStmt;
import ast.node.statement.PrintStmt;
import ast.node.statement.ReturnStmt;
import ast.node.statement.VarDecStmt;
import ast.type.primitiveType.BooleanType;
import ast.type.primitiveType.FloatType;
import ast.type.primitiveType.IntType;
import compileError.Type.FunctionNotDeclared;
import symbolTable.SymbolTable;
import symbolTable.itemException.ItemNotFoundException;
import symbolTable.symbolTableItems.FunctionItem;
import symbolTable.symbolTableItems.MainItem;
import visitor.Visitor;
import visitor.typeAnalyzer.ExpressionTypeChecker;
import ast.node.Program;
import ast.node.declaration.MainDeclaration;

import java.util.ArrayList;
import ast.type.*;
import ast.node.expression.Expression;
import ast.node.expression.BinaryExpression;
import ast.node.expression.UnaryExpression;
import ast.node.expression.Identifier;
import ast.node.expression.operators.BinaryOperator;
import ast.node.expression.operators.UnaryOperator;
import ast.node.declaration.FuncDeclaration;

import ast.node.expression.values.*;

import java.io.*;

public class CodeGenerator extends Visitor<String> {
    private final ExpressionTypeChecker expressionTypeChecker;
    private final String outputPath;
    private int label_index;
    private ArrayList<String> local_variables;

    public CodeGenerator(ExpressionTypeChecker expressionTypeChecker) {
        this.expressionTypeChecker = expressionTypeChecker;
        outputPath = "./";
        label_index = 0;
        local_variables = new ArrayList<String>();
    }

    private String getFreshLabel() {
        label_index++;
        return "Label_" + Integer.toString(label_index);
    }

    private int slotOf(String identifier) {
        boolean found = false;

        int slot_index = 0;
        for(int i = 0; i < local_variables.size(); i++) {
            if(identifier.equals(local_variables.get(i))) {
                slot_index = i;
                found = true;
            }
        }

        if(!found) {
            local_variables.add(identifier);
            slot_index = local_variables.size()-1;
        }

        return slot_index;
    }

    private String getJasminTypeSymbol(Type type) {
        if (type instanceof IntType) {
            return "I";
        } else if (type instanceof FloatType) {
            return "F";
        } else if (type instanceof BooleanType) {
            return "Z";
        } else {
            return ""; // error
        }
    }

    private String load_store(Type type, String identifierName, String command) {
        String jasmin = "i" + command;

        int slot = slotOf(identifierName);

        if(slot < 4) {
            jasmin += "_";
        } else {
            jasmin += " ";
        }

        jasmin += Integer.toString(slot);
        return jasmin;
    }

    private String functionNameAndArgs(FuncDeclaration funcDeclaration) {
        Type func_return_type = funcDeclaration.getType();
        String func_name = funcDeclaration.getIdentifier().getName();

        ArrayList<Type> arg_types = new ArrayList<>();
        for (var iden : funcDeclaration.getArgs()){
            Type arg_type = iden.getType();
            arg_types.add(arg_type);
        }

        StringBuilder input_args = new StringBuilder("(");
        for (Type t :arg_types)
            input_args.append(getJasminTypeSymbol(t));
        input_args.append(")");
        input_args.append(getJasminTypeSymbol(func_return_type));

        String jasmin = func_name;
        jasmin += input_args.toString();

        return jasmin;
    }

    private String add_operands(BinaryExpression bexp){
        String jasmin ="";
        jasmin += bexp.getLeft().accept(this);
        jasmin += bexp.getRight().accept(this);
        return jasmin;
    }

    private String add_operands(UnaryExpression uexp){
        String jasmin ="";
        jasmin += uexp.getOperand().accept(this);
        return jasmin;
    }

    private void write_to_file(String s) {
        try {
            File file = new File(outputPath + "/LogicPL.class");
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            System.out.println(s);
            bw.write(s);
            bw.close();
            System.out.println("String written to file successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing to file: " + e.getMessage());
        }
    }

    @Override
    public String visit(Program program) {
        String jasmin = "";
        jasmin += ".class public Main\n";
        jasmin += ".super java/lang/Object\n";
        jasmin += "\n";

        for(var functionDec : program.getFuncs()) {
            local_variables = new ArrayList<String>();
            jasmin += functionDec.accept(this) + "\n";
        }

        local_variables = new ArrayList<String>();
        jasmin += program.getMain().accept(this);

        write_to_file(jasmin);
        return jasmin;
    }

    @Override
    public String visit(MainDeclaration mainDeclaration) {
        String jasmin = "";
        jasmin += ".method public static main()V\n";
        jasmin += ".limit stack 140\n"; // based on the assumptions
        jasmin += ".limit locals 140\n"; // based on the assumptions
        jasmin += "\n";

        for (var stmt : mainDeclaration.getMainStatements()) {
            // doesn't work (varDecStmt -> InitialExpression -> FunctionCall)
            jasmin += stmt.accept(this);
        }

        jasmin += "\n";
        jasmin += ".end method\n";

        return jasmin;
    }

    @Override
    public String visit(FuncDeclaration funcDeclaration) {
        String jasmin = "";
        jasmin += ".method public " + functionNameAndArgs(funcDeclaration) + "\n";
        jasmin += ".limit stack 140\n";
        jasmin += ".limit locals 140\n";
        jasmin += "\n";

        for(var stmt : funcDeclaration.getStatements())
            jasmin += stmt.accept(this);

        jasmin += "\n";
        jasmin += ".end method\n";
        return jasmin;
    }

//    @Override
//    public String visit(FunctionCall functionCall) {
//        String jasmin = "";
//
//        try {
//            FunctionItem functionItem = (FunctionItem) SymbolTable.root.get(FunctionItem.STARTKEY + functionCall.getUFuncName().getName());
//            jasmin += "invokestatic Main/"  + functionNameAndArgs(functionItem.getHandlerDeclaration()) + "\n";
//
//        } catch (ItemNotFoundException e) {
//            // checked in previous phase.
//        }
//
//        return jasmin;
//    }

    @Override
    public String visit(ReturnStmt returnStmt) {
        String jasmin = "";

        if(returnStmt.getExpression() != null) {
            jasmin += returnStmt.getExpression().accept(this);
            jasmin += "ireturn\n";
        } else {
            jasmin += "return\n";
        }

        return jasmin;
    }

//    @Override
//    public String visit(PrintStmt print_ ){
//        String jasmin ="";
//        jasmin += "getstatic java/lang/System/out Ljava/io/PrintStream;\n";
//
//        jasmin += print_.getArg().accept(this);
//        Type print_type = print_.getArg().accept(expressionTypeChecker);
//
//        jasmin += "invokevirtual java/io/PrintStream/print(" + getJasminTypeSymbol(print_type) + ")V\n";
//
//        return jasmin;
//    }

    @Override
    public String visit(BinaryExpression bexp){
        String command = "";
        BinaryOperator bop = bexp.getBinaryOperator();

        if(bop.equals(BinaryOperator.add)){
            command += add_operands(bexp);
            command += "iadd\n";
        }
        else if (bop.equals(BinaryOperator.sub)) {
            command += add_operands(bexp);
            command += "isub\n";
        }
        else if (bop.equals(BinaryOperator.mult)) {
            command += add_operands(bexp);
            command += "imul\n";
        }
        else if (bop.equals(BinaryOperator.div)) {
            command += add_operands(bexp);
            command += "idiv\n";
        }
        else if (bop.equals(BinaryOperator.mod)) {
            command += add_operands(bexp);
            command += "irem\n";
        }
//        else if(bop.equals(BinaryOperator.gt) || bop.equals(BinaryOperator.lt) || bop.equals(BinaryOperator.gte) || bop.equals(BinaryOperator.lte)) { // never happens.
//            command += add_operands(bexp);
//
//            String nFalse = getFreshLabel();
//            String nAfter = getFreshLabel();
//
//            if(bop.equals(BinaryOperator.gt) || bop.equals(BinaryOperator.gte))
//                command += "if_icmple " + nFalse + "\n";
//            else
//                command += "if_icmpge " + nFalse + "\n";
//
//            command += "ldc 1\n";
//            command += "goto " + nAfter + "\n";
//            command += nFalse + ":\n";
//            command += "ldc 0\n";
//            command += nAfter + ":\n";
//        }
//        else if(bop.equals(BinaryOperator.eq) || bop.equals(BinaryOperator.neq)) { // never happens.
//            command += add_operands(bexp);
//
//            String nTrue = getFreshLabel();
//            String nAfter = getFreshLabel();
//
//            if (bop.equals(BinaryOperator.eq))
//                command += "if_icmpeq " + nTrue + "\n";
//            else
//                command += "if_icmpne " + nTrue + "\n";
//
//            command += "ldc 0\n";
//            command += "goto " + nAfter + "\n";
//            command += nTrue + ":\n";
//            command += "ldc 1\n";
//            command += nAfter + ":\n";
//        }
//        else if(bop.equals(BinaryOperator.and)) { // never happens.
//            command += bexp.getLeft().accept(this);
//            String nFalse = getFreshLabel();
//            command += "ifeq " + nFalse + "\n";
//            command += bexp.getRight().accept(this);
//            command += "ifeq " + nFalse + "\n";
//            String nAfter = getFreshLabel();
//            command+= "ldc 1\n";
//            command += "goto " + nAfter + "\n";
//            command += nFalse + ":\n";
//            command += "ldc 0\n";
//            command += nAfter + ":\n";
//
//        }
//        else if(bop.equals(BinaryOperator.or)) { // never happens.
//            command += bexp.getLeft().accept(this);
//            String nTrue = getFreshLabel();
//            command += "ifne " + nTrue + "\n";
//            command += bexp.getRight().accept(this);
//            command += "ifne " + nTrue + "\n";
//            String nAfter = getFreshLabel();
//            command += "ldc 0\n";
//            command += "goto " + nAfter + "\n";
//            command += nTrue + ":\n";
//            command += "ldc 1\n";
//            command += nAfter + ":\n";
//        }

//        else if(bop.equals(BinaryOperator.assign)) {
//            Type firstType = bexp.getLeft().accept(expressionTypeChecker);
//            Type secondType = bexp.getRight().accept(expressionTypeChecker);
//            String secondOperandCommands = bexp.getRight().accept(this);
//            if(bexp.getLeft() instanceof Identifier) {
//                command += secondOperandCommands;
//                String id = (((Identifier) bexp.getLeft()).getName());
//                if(secondType instanceof IntType)
//                    command += "invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;\n";
//                if(secondType instanceof BooleanType)
//                    command += "invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;\n";
//                Integer slot = slotOf(id);
//                command += "astore " + slot.toString() + "\n";
//                command += bexp.getLeft().accept(this);
//            }
//        }

        return command;
    }

    @Override
    public String visit(UnaryExpression uexp) {
        String jasmin = "";
        UnaryOperator uop = uexp.getUnaryOperator();

        if (uop.equals(UnaryOperator.not)) { // ! - never happens.
            jasmin += add_operands(uexp);
            jasmin += "iconst_1\n";
            jasmin += "ixor\n";
        }
        else if (uop.equals(UnaryOperator.minus)) { // -
            jasmin += add_operands(uexp);
            jasmin += "ineg\n";
        } else { // +
            jasmin += add_operands(uexp);
            // do nothing for operand
        }

        return jasmin;
    }

    @Override
    public String visit(Identifier iden){
        Type iden_type = iden.accept(expressionTypeChecker);
        return load_store(iden_type, iden.getName(), "load") + "\n";
    }

    @Override
    public String visit(AssignStmt assignStmt) {
        String jasmin = assignStmt.getRValue().accept(this);
        jasmin += load_store(assignStmt.getLValue().getType(), assignStmt.getLValue().toString().substring(assignStmt.getLValue().toString().indexOf(' ') + 1), "store") + "\n";
        // assignment.getLValue().toString() is splitted to get name of identifier.

        return jasmin;
    }

    @Override
    public String visit(VarDecStmt varDecStmt) {
        String jasmin = "";

        if(varDecStmt.getInitialExpression() != null) {
            jasmin += varDecStmt.getInitialExpression().accept(this) + "\n";
        } else {
            jasmin += "iconst_0\n";
        }

        jasmin += load_store(varDecStmt.getType(), varDecStmt.getIdentifier().getName(), "store") + "\n";
        return jasmin;
    }

    @Override
    public String visit(IntValue intValue) {
        int value = intValue.getConstant();
        String command = "";

        if(value <= 5 && 0 <= value) {
            command += "iconst_";
            command += Integer.toString(value);
        } else {
            command += "bipush ";
            command += Integer.toString(value);
        }

        return command + "\n";
    }
}
