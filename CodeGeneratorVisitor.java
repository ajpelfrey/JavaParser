package edu.ufl.cise.cop4020fa23;
import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;
import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;

import java.util.*;
import java.lang.*;

public class CodeGeneratorVisitor implements ASTVisitor {

    private Map<String, Integer> declaredVariableCounts = new HashMap<>();

    private int counter = 1;
  //  private int n = counter;
    private SymbolTable symbolTable;
    private String packageName;
    String nameDef = "";

    void setPackageName(String packageName)

    {
        this.packageName = packageName;
    }
    StringBuilder sb = new StringBuilder();
    String block;


    // Program program= ;

    @Override
    public String visitProgram(Program program, Object arg) throws PLCCompilerException {
        boolean isLast = true;

        sb.append("package edu.ufl.cise.cop4020fa23;");
        sb.append("import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;\n");

        if (program.getType()==Type.STRING)
        {
            sb.append("import java.lang.*;\n");
        }
        //get and append class name
        String ident = program.getName();
        sb.append("public class ").append(ident).append("{");
        //get and append type name
        String  type = program.getTypeToken().text();
        if (program.getType()==Type.STRING)
        {
           type ="String";
        }
        sb.append("public static ").append(type).append(" apply(");

        List <NameDef> params = program.getParams();

        if (!params.isEmpty()) {
            for (NameDef param : params) {

                Type nametype = param.getType();
                if (nametype==Type.BOOLEAN)
                {
                    sb.append("boolean ");
                    String  e = param.getName();
                    if (Objects.equals(e, "false")) {
                        sb.append(" isfalse");
                        if (params.indexOf(param) != params.size() - 1) {

                            sb.append(", ");
                        }
                    }
                    else {
                        if (params.indexOf(param) != params.size() - 1) {

                            sb.append(", ");
                        }
                        sb.append(param.getName());
                    }


                }
                if (nametype==Type.INT)
                {
                    sb.append("int ");
                    sb.append(param.getName());
                    if (params.indexOf(param) != params.size() - 1) {

                        sb.append(", ");
                    }

                }
                if (nametype==Type.STRING)
                {
                    sb.append("String ");
                    sb.append(param.getName());
                    if (params.indexOf(param) != params.size() - 1) {

                        sb.append(", ");
                    }

                }
            }
        }
        sb.append(")");
        sb.append("{");

        Block b = program.getBlock();
        String block1 = block;
        String namedefs = "";
        if (!program.getBlock().getElems().isEmpty()) {
            b.visit(this,arg);
        }
        sb.append("}");
        sb.append("}");

        return sb.toString();
    }
    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCCompilerException {

        assignmentStatement.getlValue().visit(this,arg);
        sb.append("=");
        assignmentStatement.getE().visit(this,arg);
        sb.append(";");
return null;
      //  throw new TypeCheckException("visitAssignmentStatement");
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCCompilerException {
        if (binaryExpr.getLeftExpr().getType() == Type.STRING && binaryExpr.getOp().kind() == Kind.EQ)
        {
            binaryExpr.getLeftExpr().visit(this, arg);
            sb.append(".equals(");
            binaryExpr.getRightExpr().visit(this,arg);
            sb.append(")");
        }

        if (binaryExpr.getOp().kind()==Kind.EXP)
        {
            sb.append("((int)Math.round(Math.pow(");
            binaryExpr.getLeftExpr().visit(this, arg);
            sb.append(",");
            binaryExpr.getRightExpr().visit(this, arg);
            sb.append(")))");
        }

        else {
            sb.append("(");
            binaryExpr.getLeftExpr().visit(this,arg);
          if (binaryExpr.getOp().kind()==Kind.GT) //TODO add all cases
          {
              sb.append(">");
          }
            if (binaryExpr.getOp().kind()==Kind.LT) //TODO add all cases
            {
                sb.append("<");
            }
            if (binaryExpr.getOp().kind()==Kind.MINUS) //TODO add all cases
            {
                sb.append("-");
            }
            if (binaryExpr.getOp().kind()==Kind.ASSIGN) //TODO add all cases
            {
                sb.append("=");
            }


           if (binaryExpr.getOp().kind()==Kind.PLUS)
           {
               sb.append("+");
           }
          //  if (binaryExpr.getOp().text()=="+")
           // {
            //.    sb.append("+");
           // }
            binaryExpr.getRightExpr().visit(this,arg);
            sb.append(")");
        }
        return null;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCCompilerException {

        //  StringBuilder sb = new StringBuilder();
        String blockName;

        List<Block.BlockElem> blockElems = block.getElems();
        for (Block.BlockElem elem: blockElems) {
            elem.visit(this, arg);
        }

        return null;
    }

    @Override
    public Object visitBlockStatement(StatementBlock statementBlock, Object arg) throws PLCCompilerException {

        statementBlock.getBlock().visit(this,arg);
        return null;

        //  throw new TypeCheckException("visitBlockStatement");
    }

    @Override
    public Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException {
        throw new TypeCheckException("visitChannelSelector");
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCCompilerException {
        sb.append("(");
        conditionalExpr.getGuardExpr().visit(this,arg);
        sb.append("?");
        conditionalExpr.getTrueExpr().visit(this,arg);
        sb.append(":");
        conditionalExpr.getFalseExpr().visit(this,arg);
        sb.append(")");
        return null;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException {
      declaration.getNameDef().visit(this,arg);

      //sb.append("=");
       // declaration.getInitializer().visit(this,arg);

        if (declaration.getInitializer()==null)
        {
            sb.append(";");
            return null;
        }
        else
        {
            sb.append("=");
            declaration.getInitializer().visit(this,arg);
            sb.append(";");
        }

      return null;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCCompilerException {
        throw new TypeCheckException("visitDimension");
    }

    @Override
    public Object visitDoStatement(DoStatement doStatement, Object arg) throws PLCCompilerException {
        throw new TypeCheckException("visitDoStatement");
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException {
        throw new TypeCheckException("visitExpandedPixelExpr");
    }

    @Override
    public Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
        throw new TypeCheckException("visitGuardedBlock");
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException {
         if (Objects.equals(identExpr.getNameDef().getName(), "false") || Objects.equals(identExpr.getNameDef().getName(), "isfalse"))
         {
             sb.append("isfalse");
             return null;

         }
         if (Objects.equals(identExpr.getNameDef().getName(),"INT"))
         {
             sb.append("int ");
             return null;
         }


       /* if (declaredVariables.contains(identExpr.getName()+"$1"))
        {
            sb.append(identExpr.getNameDef().getName()).append("$").append(identifierCount-1);
        }*/
        else
            sb.append(identExpr.getNameDef().getJavaName());
        //sb.append(identExpr.getNameDef().getName());
        return null;
    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        throw new TypeCheckException("visitIfStatement");
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException {

        if (lValue.firstToken.kind()==Kind.IDENT)
        {
          //  int varCount = declaredVariableCounts.getOrDefault(lValue.firstToken.text(), identifierCount);

            sb.append(lValue.firstToken.text());
        }

       return null;
        //  throw new T;ypeCheckException("visitLValue");
    }
    private int identifierCount = 1;
    List<String> declaredVariables = new ArrayList<>();

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {



            sb.append(nameDef.getType().name().toLowerCase());
            String varName = nameDef.getJavaName();


            sb.append(" ");
            
        sb.append(varName);
        return null;
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException {
           sb.append(numLitExpr.firstToken.text());
        return null;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException {
        throw new TypeCheckException("visitPixelSelector");
    }

    @Override
    public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
        throw new TypeCheckException("visitPostfixExpr");
    }



    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCCompilerException {
        sb.append("return ");

        Expr e  = returnStatement.getE();
         e.visit(this, arg);
        sb.append(";");
        return null;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException {
        sb.append(stringLitExpr.getText());
        return null;
        //        throw new TypeCheckException("visitStringLitExpr");
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCCompilerException {
        sb.append("(");
        Kind opKind = unaryExpr.getOp();
    if (Objects.equals(opKind.toString(), "MINUS"))
            sb.append("-");
    if (Objects.equals(opKind.toString(), "BANG"))
        {
            sb.append("!");
        }
        if (Objects.equals(opKind.toString(), "PLUS"))
        {
            sb.append("+");
        }
        if (Objects.equals(opKind.toString(), "DIV"))
        {
            sb.append("/");
        }
        if (Objects.equals(opKind.toString(), "MOD"))
        {
            sb.append("%");
        }

        Expr e = unaryExpr.getExpr();
          e.visit(this, arg);
        sb.append(")");
        return null;
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
        // Object value = writeStatement.getExpr().visit(this, arg);
        sb.append("ConsoleIO.write(");
        Object value = writeStatement.getExpr().visit(this, arg);


        // Append the value to the Java code.
        sb.append("); ");


        return null;
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws PLCCompilerException {
        if (Objects.equals(booleanLitExpr.getText(), "false"))
        {
            sb.append("false");
        }
        else {
            sb.append("true");
        }
        return null;
    }

    @Override
    public Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
throw new TypeCheckException("visitConstExpr");
    }
}
