import java.util.*;
import syntaxtree.*;
import visitor.GJDepthFirst;
import java.io.*;

public class codeGenerator extends GJDepthFirst<String, MethodType> {

    Integer temp;
    Integer arr_allocNum;
    Integer oob_Num;
    Integer LoopLabelNum;
    Integer newIfLabelNum;
    Integer AndLabelNum;
    SymbolTableVisitor symbolTable;
    Map<String, String> offsets;
    Map<String, Integer> numOfMethods;
    Map<Integer, String> callMap;
    Map<String, Integer> methodOffsets;
    Integer callCounter = 0;
    StringBuilder buffer = new StringBuilder();

    public codeGenerator(SymbolTableVisitor symbolTable, Map offsets, Map callMap, Map methodOffsets) {
        this.symbolTable = symbolTable;
        this.offsets = offsets;
        this.temp = -1;
        this.AndLabelNum = -1;
        this.LoopLabelNum = 0;
        this.newIfLabelNum = 0;
        this.arr_allocNum = 0;
        this.oob_Num = 0;
        this.numOfMethods = new LinkedHashMap<String, Integer>();
        this.callMap = callMap;
        this.methodOffsets = methodOffsets;

        for (String oneclass : symbolTable.classes.keySet()) {
            String tempClassName = oneclass;
            Integer methodsNum = symbolTable.classes.get(oneclass).methods.size();
            emit("@." + tempClassName + "_vtable");
            emit(" = global [");
            for (String methodName : symbolTable.classes.get(oneclass).methods.keySet()) {
                if (methodName.equals("main")) {
                    methodsNum = 0;
                }
                break;
            }
            emit(methodsNum.toString());
            this.numOfMethods.put(oneclass, methodsNum);
            emit(" x i8*] [");
            int count = 0;
            for (String methodName : symbolTable.classes.get(oneclass).methods.keySet()) {
                if(methodName.equals("main")){
                    break;
                }
                if(count != 0){
                    emit(", ");
                }

                emit("i8* bitcast (");
                emit(typeConverter(symbolTable.classes.get(oneclass).methods.get(methodName).returnType));
                emit(" (i8*");
                for(String argName : symbolTable.classes.get(oneclass).methods.get(methodName).arguments.keySet()){
                    String argType = symbolTable.classes.get(oneclass).methods.get(methodName).arguments.get(argName)[0];
                    emit(", ");
                    emit(typeConverter(argType));
                }
                emit(")* " );
                emit("@" + oneclass + "." + methodName + " to i8*)");

                count = count + 1;
            }

            emit("] \n\n");

        }

        //System.out.println(buffer);

        emit("declare i8* @calloc(i32, i32)\n");
        emit("declare i32 @printf(i8*, ...)\n");
        emit("declare void @exit(i32)\n\n");

        emit("@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n");
        emit("@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n");
        emit("define void @print_int(i32 %i) {\n");
        emit("%_str = bitcast [4 x i8]* @_cint to i8*\n");
        emit("call i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n");
        emit("    ret void\n");
        emit("}\n");

        emit("define void @throw_oob() {\n");
        emit("%_str = bitcast [15 x i8]* @_cOOB to i8*\n");
        emit("call i32 (i8*, ...) @printf(i8* %_str)\n");
        emit("call void @exit(i32 1)\n");
        emit("    ret void\n");
        emit("}\n");

    }


    public String visit(MainClass mainClass, MethodType argu) {
        emit("define i32 @main() {\n");

        //mainClass.f11.accept(this, symbolTable.classes.get(mainClass.f1.f0.toString()).methods.get("main"));

        if(mainClass.f14.present()){
            mainClass.f14.accept(this, symbolTable.classes.get(mainClass.f1.f0.toString()).methods.get("main"));
        }

        if (mainClass.f15.present()) {
            mainClass.f15.accept(this, symbolTable.classes.get(mainClass.f1.f0.toString()).methods.get("main"));
        }
        emit("ret i32 0\n");
        emit("}\n");
        return null;
    }

    public String visit(ClassDeclaration classDec, MethodType argu) {

        String className = classDec.f1.f0.toString();
        MethodType classNameM = new MethodType(className,null,null);
        //if(classDec.f3.present()) {
        //    classDec.f3.accept(this, classNameM);
        //}
        if(classDec.f4.present()) {
            classDec.f4.accept(this, classNameM);
        }

        return null;
    }

    public String visit(ClassExtendsDeclaration classDec, MethodType argu) {

        String className = classDec.f1.f0.toString();
        MethodType classNameM = new MethodType(className,null,null);
        //if(classDec.f5.present()) {
        //    classDec.f5.accept(this, classNameM);
        //}
        if(classDec.f6.present()) {
            classDec.f6.accept(this, classNameM);
        }

        return null;

    }

    public String visit(VarDeclaration var, MethodType argu){

        String Type = var.f0.accept(this,argu);
        String Name = var.f1.f0.toString();
        emit("%");
        emit(Name);
        emit(" = alloca ");
        if(Type.equals("i32") || Type.equals("i1") || Type.equals("i32*")){
            emit(Type);
        }else{
            emit("i8*");
        }

        emit("\n");

        return null;
    }

    public String visit(MethodDeclaration n, MethodType argu) {
        this.temp = -1;

        MethodType thisMethod = symbolTable.classes.get(argu.inClass).methods.get(n.f2.f0.toString());

        emit("define " + typeConverter(thisMethod.returnType) + " @" + thisMethod.inClass + "." + n.f2.f0.toString() +"(");
        //if(n.f4.present()){
         //   n.f4.accept(this, thisMethod);
        //}

        emit("i8* %this");

        for(String args : thisMethod.arguments.keySet()){

            emit(", " + typeConverter(thisMethod.arguments.get(args)[0]) + " " + "%." + args);

        }
        emit(") {\n");

        for(String args : thisMethod.arguments.keySet()){

            emit("%"+ args + " = alloca " + typeConverter(thisMethod.arguments.get(args)[0]) + "\n");
            emit("store " + typeConverter(thisMethod.arguments.get(args)[0]) + " %."+ args + ", " + typeConverter(thisMethod.arguments.get(args)[0]) + "* " + "%"+ args + "\n");

        }

        if(n.f7.present()){
            n.f7.accept(this, thisMethod);
        }
        if(n.f8.present()){
            n.f8.accept(this, thisMethod);
        }
        String returnValue = n.f10.accept(this, thisMethod);
        if(returnValue == null){
            returnValue = typeConverter(thisMethod.returnType) +  " 0";
        }
        //if(returnValue)
        //+ typeConverter(thisMethod.returnType) +
        StringBuilder str = new StringBuilder();
        str.append(returnValue);
        String[] split = str.toString().split("\\s+", -1);

        if(!(split[0].equals(typeConverter(thisMethod.returnType)))){
            String temp3 = newTemp();
            emit(temp3 + " = load " + typeConverter(thisMethod.returnType) + ", " + returnValue + "\n");
            returnValue = typeConverter(thisMethod.returnType) + " " + temp3;
        }
        emit("ret "  + returnValue + "\n");
        emit("}\n");

        return null;

    }

    public String visit(IntegerType n, MethodType argu) {
        return "i32";
    }

    public String visit(BooleanType n, MethodType argu) {
        return "i1";
    }

    public String visit(ArrayType n, MethodType argu) {
        return "i32*";
    }


    public String visit(AssignmentStatement n, MethodType argu) {
        String temp1 = "null";

        Integer flag = 0;

        for(String varName : argu.variables.keySet()){
            if(n.f0.f0.toString().equals(varName)){
                temp1 = typeConverter(argu.variables.get(varName)) + "*" + " %" + varName + "\n";

                String temp2 = n.f2.accept(this, argu);
                //System.out.println(temp2);
                emit("store " + temp2 + ", " + temp1 + "\n\n");
                //System.out.println(n.f0.f0.toString());
                return null;
            }
        }

        for(String varName : argu.arguments.keySet()){
            if(n.f0.f0.toString().equals(varName)){
                temp1 = typeConverter(argu.arguments.get(varName)[0]) + "*" + " %" + varName + "\n";

                String temp2 = n.f2.accept(this, argu);
                //System.out.println(temp2);
                emit("store " + temp2 + ", " + temp1 + "\n\n");
                //System.out.println(n.f0.f0.toString());
                return null;
            }
        }

        for(String varName : symbolTable.classes.get(argu.inClass).variables.keySet()){
            if(n.f0.f0.toString().equals(varName)){
                    Integer offsetInt = symbolTable.classes.get(argu.inClass).varOffsets.get(varName) + 8;
                    String temp2 = newTemp();
                    String type = symbolTable.classes.get(argu.inClass).variables.get(varName);
                    String offset = offsetInt.toString();
                    emit(temp2 + " = getelementptr i8, i8* %this, i32 " + offset + "\n");
                    String temp3 = newTemp();
                    emit(temp3 + " = bitcast i8* " + temp2 + " to " + typeConverter(type) + "*\n");
                    flag =1;


                    temp1 = typeConverter(type) + "* " + temp3;

                    String temp15 = n.f2.accept(this, argu);
                    //System.out.println(temp2);
                    emit("store " + temp15 + ", " + temp1 + "\n\n");
                    //System.out.println(n.f0.f0.toString());
                    return null;


            }
        }

            if(symbolTable.classes.get(argu.inClass).parentClass != null){

                if (checkParentForVars(symbolTable.classes.get(argu.inClass).parentClass.className, n.f0.f0.toString()) != null) {
                    Integer offsetInt = symbolTable.classes.get(checkParentForVars(symbolTable.classes.get(argu.inClass).parentClass.className, n.f0.f0.toString())).varOffsets.get(n.f0.f0.toString()) + 8;
                    String temp4 = newTemp();
                    String type = symbolTable.classes.get(checkParentForVars(symbolTable.classes.get(argu.inClass).parentClass.className, n.f0.f0.toString())).variables.get(n.f0.f0.toString());
                    String offset = offsetInt.toString();
                    emit(temp4 + " = getelementptr i8, i8* %this, i32 " + offset + "\n");
                    String temp5 = newTemp();
                    emit(temp5 + " = bitcast i8* " + temp4 + " to " + typeConverter(type) + "*\n");


                    temp1 = typeConverter(type) + "* " + temp5;
                    String temp2 = n.f2.accept(this, argu);
                    //System.out.println(temp2);
                    emit("store " + temp2 + ", " + temp1 + "\n\n");
                    //System.out.println(n.f0.f0.toString());
                    return null;


                }
            }

        return null;

    }

    public String visit(Identifier n, MethodType argu) {

        for(String varName : argu.variables.keySet()){
            if(n.f0.toString().equals(varName)){
                String temp1 = newTemp();
                emit(temp1 + " = load " + typeConverter(argu.variables.get(varName)) + ", " + typeConverter(argu.variables.get(varName)) + "*" + " %" + varName + "\n");
                return typeConverter(argu.variables.get(varName)) + " " + temp1;
            }
        }

        for(String varName : argu.arguments.keySet()){
            if(n.f0.toString().equals(varName)){
                String temp1 = newTemp();
                emit(temp1 + " = load " + typeConverter(argu.arguments.get(varName)[0]) + ", " + typeConverter(argu.arguments.get(varName)[0]) + "*" + " %" + varName + "\n");
                return typeConverter(argu.arguments.get(varName)[0]) + " " + temp1;
            }
        }

        for(String varName : symbolTable.classes.get(argu.inClass).variables.keySet()){
            if(n.f0.toString().equals(varName)){
                Integer offsetInt = symbolTable.classes.get(argu.inClass).varOffsets.get(varName) + 8 ;
                String temp2 = newTemp();
                String type = symbolTable.classes.get(argu.inClass).variables.get(varName);
                String offset = offsetInt.toString();
                emit(temp2 + " = getelementptr i8, i8* %this, i32 " + offset + "\n");
                String temp3 = newTemp();
                emit(temp3 + " = bitcast i8* " + temp2 + " to " + typeConverter(type) + "*\n");
                String temp4 = newTemp();

                emit(temp4 + " = load " + typeConverter(type) + ", " + typeConverter(type) + "* " + temp3 + "\n");

                return typeConverter(type) + " " + temp4;
            }
        }

       if(symbolTable.classes.get(argu.inClass).parentClass != null){

            if(checkParentForVars(symbolTable.classes.get(argu.inClass).parentClass.className,n.f0.toString()) != null) {

                Integer offsetInt = symbolTable.classes.get(checkParentForVars(symbolTable.classes.get(argu.inClass).parentClass.className, n.f0.toString())).varOffsets.get(n.f0.toString()) + 8;
                String temp5 = newTemp();
                String type = symbolTable.classes.get(checkParentForVars(symbolTable.classes.get(argu.inClass).parentClass.className, n.f0.toString())).variables.get(n.f0.toString());

                String offset = offsetInt.toString();
                emit(temp5 + " = getelementptr i8, i8* %this, i32 " + offset + "\n");
                String temp6 = newTemp();
                emit(temp6 + " = bitcast i8* " + temp5 + " to " + typeConverter(type) + "*\n");
                String temp7 = newTemp();

                emit(temp7 + " = load " + typeConverter(type) + ", " + typeConverter(type) + "* " + temp6 + "\n");

                return typeConverter(type) + " " + temp7;
            }
        }


        return n.f0.toString();
    }

    public String visit(AndExpression n, MethodType argu) {
        String temp1 = n.f0.accept(this, argu);


        String temp3 = newTemp();
        String label1 = newAndLabel();
        String label2 = newAndLabel();
        String label3 = newAndLabel();
        String label4 = newAndLabel();
        emit("br label " + "%" + label1 + "\n\n");
        emit(label1 + ":\n");
        emit("br " + temp1 + ", label " + "%" + label2 + ", label " + "%" + label4 + "\n");
        emit(label2 + ":\n");
        String temp2 = n.f2.accept(this, argu);
        emit("br label " + "%" + label3 + "\n\n");
        emit(label3 + ":\n");
        emit("br label " + "%" + label4 + "\n\n");
        emit(label4 + ":\n");

        StringBuilder str = new StringBuilder();
        str.append(temp2);
        String[] split = str.toString().split("\\s+", -1);

        if(split[0].equals("null")){
            emit(temp3 + " = phi i1 [ 0, "+ "%" + label1 + "], [ " + "   " +", " + "%" + label3 + "]\n");
        }else {
            emit(temp3 + " = phi i1 [ 0, "+ "%" + label1 + "], [ " + split[1] +", " + "%" + label3 + "]\n");
        }


        return "i1 "+ temp3;
    }

    public String visit(TimesExpression n, MethodType argu) {

        String temp3 = newTemp();
        //Integer nullEx = this.temp - 1;
        String temp1 = n.f0.accept(this, argu);
        String temp2 = n.f2.accept(this, argu);
        StringBuilder str = new StringBuilder();
        str.append(temp2);
        String[] split = str.toString().split("\\s+", -1);

        if(split[0].equals("null")){
            emit(temp3 + " = " + "mul " + temp1 + ", " + "%_" + "\n");
        }else {
            emit(temp3 + " = " + "mul " + temp1 + ", " + split[1] + "\n");
        }

        return "i32 " + temp3;
    }

    public String visit(PlusExpression n, MethodType argu) {
        String temp3 = newTemp();
        //Integer nullEx = this.temp - 1;
        String temp1 = n.f0.accept(this, argu);
        String temp2 = n.f2.accept(this, argu);
        StringBuilder str = new StringBuilder();
        str.append(temp2);
        String[] split = str.toString().split("\\s+", -1);

        if(split[0].equals("null")){
            emit(temp3 + " = " + "add " + temp1 + ", " + "%_"  + "\n");
        }else {
            emit(temp3 + " = " + "add " + temp1 + ", " + split[1] + "\n");
        }

        return "i32 " + temp3;
    }

    public String visit(MinusExpression n, MethodType argu) {

        String temp3 = newTemp();
        //Integer nullEx = this.temp - 1;
        String temp1 = n.f0.accept(this, argu);
        String temp2 = n.f2.accept(this, argu);
        StringBuilder str = new StringBuilder();
        str.append(temp2);
        String[] split = str.toString().split("\\s+", -1);


        if(split[0].equals("null")){
            emit(temp3 + " = " + "sub " + temp1 + ", " + "%_"  + "\n");
        }else {
            emit(temp3 + " = " + "sub " + temp1 + ", " + split[1] + "\n");
        }

        return "i32 " + temp3;
    }

   public String visit(ArrayLookup n, MethodType argu) {

        String temp2 = n.f0.accept(this, argu);


        String temp4 = n.f2.accept(this, argu);
        //if(temp4.indexOf('%') < 0){
        //    temp4 = "i32 " + temp4;
       // }

        //String temp2 = newTemp();
        //emit(temp2 + " = load i32*, " + temp1 + "\n");
        //%_20 = getelementptr i8, i8* %this, i32 8
        //        %_21 = bitcast i8* %_20 to i32**
        String temp3 = newTemp();
        emit(temp3 + " = load i32, " + temp2 + "\n");
	    //%_22 = load i32*, i32** %_21
        //        %_12 = load i32, i32 *%_22
        String temp5 = newTemp();
        emit(temp5 + " = icmp ult " + temp4 + ", " + temp3 + "\n");
                //%_13 = icmp ult i32 0, %_12
        String label1 = newObbLabel();
        String label2 = newObbLabel();
        String label3 = newObbLabel();
        emit("br i1 " + temp5 + ", label " + "%" + label1 + ", label " + "%" + label2 + "\n\n");
        emit(label1 + ":\n");
        String temp6 = newTemp();
        emit(temp6 + " = add " + temp4 + ", 1\n");
        String temp8 = newTemp();
        String temp9 = newTemp();
        emit(temp8 + " = getelementptr i32, " + temp2 + ", i32 " + temp6 + "\n");

        emit(temp9 + " = load i32, i32* " + temp8 + "\n");
        emit("br label " + "%" + label3 + "\n\n");
        emit(label2 + ":\n");
        emit("call void @throw_oob()\n");
        emit("br label " + "%" + label3 + "\n\n");
        emit(label3 + ":\n");

        return "i32 " + temp9;

    }

    public String visit(BracketExpression n, MethodType argu) {

        return n.f1.accept(this, argu);

    }

    public String visit(Block n, MethodType argu) {
        if(n.f1.present()) {
            return n.f1.accept(this, argu);
        }else{
            return "";
        }

    }


    public String visit(ArrayAssignmentStatement n, MethodType argu) {

        String temp2 = n.f0.accept(this, argu);

        String temp4 = n.f2.accept(this, argu);
        //if(temp4.indexOf('%') < 0 ){
        //    temp4 = "i32 " + temp4;
        //}
        String temp10 = n.f5.accept(this, argu);
        //if(temp10.indexOf('%') < 0 ){
        //    temp10 = "i32 " + temp10;
        //}

        //String temp2 = newTemp();
        //emit(temp2 + " = load i32*, " + temp1 + "\n");
        //%_20 = getelementptr i8, i8* %this, i32 8
        //        %_21 = bitcast i8* %_20 to i32**
        String temp3 = newTemp();
        emit(temp3 + " = load i32, " + temp2 + "\n");
        //%_22 = load i32*, i32** %_21
        //        %_12 = load i32, i32 *%_22
        String temp5 = newTemp();
        emit(temp5 + " = icmp ult " + temp4 + ", " + temp3 + "\n");
        //%_13 = icmp ult i32 0, %_12
        String label1 = newObbLabel();
        String label2 = newObbLabel();
        String label3 = newObbLabel();
        emit("br i1 " + temp5 + ", label " + "%" + label1 + ", label " + "%" + label2 + "\n\n");
        emit(label1 + ":\n");
        String temp6 = newTemp();
        emit(temp6 + " = add " + temp4 + ", 1\n");
        String temp7 = newTemp();
        emit(temp7 + " = getelementptr i32, " + temp2 + ", i32 " + temp6 + "\n");
        emit("store "+ temp10 +", i32* " + temp7 + "\n");
        emit("br label " + "%" + label3 + "\n\n");
        emit(label2 + ":\n");
        emit("call void @throw_oob()\n");
        emit("br label " + "%" + label3 + "\n\n");
        emit(label3 + ":\n");

        return temp10;


    }


    public String visit(IfStatement n, MethodType argu) {

        String temp1 = n.f2.accept(this, argu);
        String label1 = newIfLabel();
        String label2 = newIfLabel();
        String label3 = newIfLabel();
        emit("br " + temp1 +", label " + "%" + label1 + ", label " + "%" + label2 + "\n\n");
        emit(label1 + ":\n");
        n.f4.accept(this, argu);
        emit("br label " + "%" + label3 + "\n\n");
        emit(label2 + ":\n");
        n.f6.accept(this, argu);
        emit("br label " + "%" + label3 + "\n\n");
        emit(label3 + ":\n");

        return null;
    }

    public String visit(WhileStatement n, MethodType argu) {

        String label1 = newLoopLabel();
        String label2 = newLoopLabel();
        String label3 = newLoopLabel();
        emit("br label " + "%" + label1 + "\n\n");
        emit(label1 + ":\n\n");
        String temp1 = n.f2.accept(this, argu);

       //if(temp1.indexOf('%') < 0 ){
       //    temp1 = "i32 " + temp1;
       //}

        emit("br "+ temp1 + ", label " + "%" + label2 + ", label " + "%" + label3 + "\n\n");
        emit(label2 + ":\n");
        n.f4.accept(this,argu);
        emit("br label " + "%" + label1 + "\n\n");

        emit(label3 + ":\n");
        return null;
    }

    public String visit(IntegerLiteral n, MethodType argu) {
        return "i32 " + n.f0.toString();
    }

    public String visit(TrueLiteral n, MethodType argu) {

        return "i1 1";
    }


    public String visit(FalseLiteral n, MethodType argu) {
        return "i1 0";
    }

    public String visit(ThisExpression n, MethodType argu) {
        return "i8* %this";
    }

    public String visit(ArrayAllocationExpression n, MethodType argu) {


        String expressionVar = n.f3.accept(this,argu);
        //%_9 = load i32, i32* %sz
        String temp1 = newTemp();
    	emit(temp1 + " = icmp slt " + expressionVar + ", 0\n");
    	//= icmp slt i32 %_9, 0
        String label1 = newArrAlocLabel();
        String label2 = newArrAlocLabel();
        emit("br i1 " + temp1 + ", label " + "%" + label1 + ", label " + "%" + label2 + "\n");
    	//br i1 %_6, label %arr_alloc7, label %arr_alloc8
        emit("\n");
        emit(label1 + ":\n");
        emit("call void @throw_oob()\n");
        emit("br label " + "%" + label2 + "\n");
        emit(label2 + ":\n");
        String temp3 = newTemp();
        emit(temp3 + " = add " + expressionVar + ", 1\n" );
        String temp4 = newTemp();
        emit(temp4 + " = call i8* @calloc(i32 4, i32 " + temp3 + ")\n");
        String temp5 = newTemp();
        emit(temp5 + " = bitcast i8* " + temp4 + " to i32*\n");
        emit("store " + expressionVar + ", i32* "+ temp5 + "\n");
        /*String temp6 = newTemp();
        emit(temp6 + " = getelementptr i8, i8* %this, 132 8\n");
        String temp7 = newTemp();
        emit(temp7 + " = bitcast i8* " + temp6 + " to i32**\n");
        emit("store i32* " + temp5 + ", i32** " + temp7 + "\n");
        arr_alloc7:
    	call void @throw_oob()
    	br label %arr_alloc8

        arr_alloc8:
	    %_3 = add i32 %_9, 1
	    %_4 = call i8* @calloc(i32 4, i32 %_3)
	    %_5 = bitcast i8* %_4 to i32*
    	store i32 %_9, i32* %_5
    	%_10 = getelementptr i8, i8* %this, i32 8
	    %_11 = bitcast i8* %_10 to i32**
    	store i32* %_5, i32** %_11*/
        return "i32* " + temp5;

    }

    public String visit(AllocationExpression n, MethodType thisMethod) {

        String type = n.f1.accept(this, thisMethod);

        String temp1 = newTemp();
        emit(temp1 + " = call i8* @calloc(i32 1, i32 ");
        emit(offsets.get(type));
        emit(")\n");
        String temp2 = newTemp();
        emit(temp2 + " = bitcast i8* " + temp1 + " to i8***\n");
        String temp3 = newTemp();
        emit(temp3 + " = getelementptr [");
        emit((numOfMethods.get(type)).toString());
        emit(" x i8*], [");
        emit((numOfMethods.get(type)).toString());
        emit(" x i8*]* @.");
        emit(type + "_vtable, i32 0, i32 0\n");
        emit("store i8** " + temp3 + ", i8*** " + temp2 + "\n");
        //emit("store i8* " + temp1 + ", i8** ");

        return "i8* " + temp1;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public String visit(MessageSend n, MethodType argu) {

        //argu.SetTransferedArg();
        String temp1 = n.f0.accept(this, argu);
        this.callCounter++;
        String classType = this.callMap.get(callCounter);
        //String classType = argu.getThisType();
        //argu.CleanTransferedArg();
        //System.out.println(classType);

        n.f2.accept(this, argu);
        Integer vtableOffset = methodOffsets.get(classType + "." + n.f2.f0.toString())/8;

        emit("; " + classType + "." + n.f2.f0.toString() + " " + ": " + vtableOffset.toString() + "\n");
        String temp2 = newTemp();
        emit(temp2 + " = bitcast " + temp1 + " to i8***\n");
        String temp3 = newTemp();
        emit(temp3 + " = load i8**, i8*** " + temp2 + "\n");
        String temp4 = newTemp();
        String temp5 = newTemp();
        String temp6 = newTemp();
        String temp7 = newTemp();
        emit(temp4 + " = getelementptr i8*, i8** " + temp3 + ", i32 " + vtableOffset + "\n");
        emit(temp5 + "= load i8*, i8** " + temp4 + "\n");


        emit(temp6 + " = bitcast i8* " + temp5 + " to " + typeConverter(symbolTable.classes.get(classType).methods.get(n.f2.f0.toString()).returnType) + " (i8*");
        for(String arg : symbolTable.classes.get(classType).methods.get(n.f2.f0.toString()).arguments.keySet()){
            emit(", " + typeConverter(symbolTable.classes.get(classType).methods.get(n.f2.f0.toString()).arguments.get(arg)[0]));
        }
        emit(")*\n");

        n.f4.accept(this, argu);

        emit(temp7 + " = call " + typeConverter(symbolTable.classes.get(classType).methods.get(n.f2.f0.toString()).returnType) + " " + temp6 + "(" + temp1);
        for(String arg : argu.tempCallArgumentsCodeG.values()){
            emit(", " + arg);
        }


        argu.tempCallArgumentsCodeG.clear();
        /*
        %_4 = bitcast i8* %_3 to i8***
        %_5 = load i8**, i8*** %_4
                %_6 = getelementptr i8*, i8** %_5, i32 0
                %_7 = load i8*, i8** %_6
                %_8 = bitcast i8* %_7 to i1 (i8*,i32)*
	    %_9 = call i1 %_8(i8* %_3, i32 16)
        */

        emit(")\n");

        return typeConverter(symbolTable.classes.get(classType).methods.get(n.f2.f0.toString()).returnType) + " " + temp7;
    }

    public String visit(ExpressionList n, MethodType argu) {

        argu.tempCallArgumentsCodeG.put(argu.tempCallArgumentsCodeG.size()+1, n.f0.accept(this, argu));
        n.f1.accept(this, argu);

        return null;
    }

    public String visit(ExpressionTail n, MethodType argu) {
        if(n.f0.present()) {
            return n.f0.accept(this, argu);
        }else{
            return "";
        }
    }

    public String visit(ExpressionTerm n, MethodType argu) {
        argu.tempCallArgumentsCodeG.put(argu.tempCallArgumentsCodeG.size()+1, n.f1.accept(this, argu));
        return null;
    }

    public String visit(PrintStatement n, MethodType argu) {
        String temp1 = n.f2.accept(this, argu);
        StringBuilder str = new StringBuilder();
        str.append(temp1);
        String[] split = str.toString().split("\\s+", -1);

        if(split[0].equals("i32")){
            emit("\ncall void (i32) @print_int(" + temp1 + ")\n\n");
        }else {
            emit("\ncall void (i32) @print_int(i32 " +  temp1 + ")\n\n");
        }

        return null;
    }

    public String visit(Expression n, MethodType argu) {
        return n.f0.accept(this, argu);
    }

    public String visit(NotExpression n, MethodType argu) {
        String temp1 = n.f1.accept(this, argu);
        String temp2 = newTemp();
        StringBuilder str = new StringBuilder();
        str.append(temp1);
        String[] split = str.toString().split("\\s+", -1);

        if(split[0].equals("null")){
            emit(temp2 + " = xor i1 1, " +  " " + "\n");
        }else {
            emit(temp2 + " = xor i1 1, " +  split[1] + "\n");
        }

        return "i1 " + temp2;
    }

   public String visit(CompareExpression n, MethodType argu) {

        String temp1 = n.f0.accept(this, argu);

        String temp2 = n.f2.accept(this, argu);

        StringBuilder str = new StringBuilder();
        str.append(temp2);
        String[] split = str.toString().split("\\s+", -1);

        String temp3 = newTemp();
        if(split[0].equals("null")){
            emit(temp3 + " = icmp slt " + temp1 + ", " + temp2 + "\n");
        }else{
            emit(temp3 + " = icmp slt " + temp1 + ", " + split[1] + "\n");
        }


        //if(split[0].equals(null)){
        //    System.out.println("nai" );
        //    emit(temp3 + " = icmp slt " + temp1 + ", " + temp2 + "\n");
        //}else{

        //}
        return "i1 " + temp3;
    }

    public String visit(ArrayLength n, MethodType argu) {
        String temp0 = n.f0.accept(this, argu);
        String temp1 = newTemp();
        String temp2 = newTemp();
        emit(temp1 + " = getelementptr i32, " + temp0 + ", i32 0 \n");

        emit(temp2 + " = load i32, i32* " + temp1 + "\n");

        return "i32 " + temp2;
    }

    public void emit(String temp){

        buffer.append(temp);
    }

    public String newTemp(){
        this.temp =  this.temp + 1;
        return "%_" + this.temp.toString();
    }
    public String newIfLabel(){

        this.newIfLabelNum =  this.newIfLabelNum + 1;
        return "if" +  this.newIfLabelNum.toString();
    }


    public String newArrAlocLabel(){

        this.arr_allocNum = this.arr_allocNum + 1;
        return "arr_alloc" +  this.arr_allocNum.toString();
    }

    public String newAndLabel(){

        this.AndLabelNum = this.AndLabelNum + 1;
        return "andLabel" +  this.AndLabelNum.toString();
    }

    public String newLoopLabel(){

        this.LoopLabelNum =  this.LoopLabelNum + 1;
        return "loop" +  this.LoopLabelNum.toString();
    }
    public String newObbLabel(){

        this.oob_Num =  this.oob_Num + 1;
        return "oob" +  this.oob_Num.toString();
    }

    public StringBuilder printEmit(){

        return this.buffer;
    }

    public String typeConverter(String type){
        if(type.equals("int")){
            return "i32";
        }else if(type.equals("boolean")){
            return "i1";
        }else if(type.equals("int []")){
            return "i32*";
        }else{
            return "i8*";
        }
    }

    public String checkParentForMethod(String thisClass,String methodName){
        if(symbolTable.classes.get(thisClass).parentClass.methods.containsKey(methodName)){
            return symbolTable.classes.get(thisClass).parentClass.className;
        }else if(symbolTable.classes.get(thisClass).parentClass != null){
            return checkParentForMethod(symbolTable.classes.get(thisClass).parentClass.className,methodName);
        }else{
            return null;
        }
    }

    public String checkParentForVars(String thisClass,String varName){

        if(symbolTable.classes.get(thisClass).variables.containsKey(varName)){
            return symbolTable.classes.get(thisClass).className;
        }else if(symbolTable.classes.get(thisClass).parentClass != null){
            return checkParentForVars(symbolTable.classes.get(thisClass).parentClass.className,varName);
        }else{
            return null;
        }
    }

    public boolean checkIfChildHasParent(String child,String Parent){
        if(symbolTable.classes.get(child).parentClass.className.equals(Parent)){
            return true;
        }else if(symbolTable.classes.get(child).parentClass != null){
            return checkIfChildHasParent(symbolTable.classes.get(child).parentClass.className,Parent);
        }else{
            return false;
        }
    }
}
