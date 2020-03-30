import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;

public class TypeCheckVisitor extends GJDepthFirst<String, MethodType> {

    SymbolTableVisitor symbolTable;

    public TypeCheckVisitor(SymbolTableVisitor symbolTable){
        this.symbolTable = symbolTable;
    }


    public String visit(MainClass mainClass, MethodType argu) {

        if(mainClass.f15.present()){
            mainClass.f15.accept(this, symbolTable.classes.get(mainClass.f1.f0.toString()).methods.get("main"));
        }

        return null;
    }

    public String visit(ClassDeclaration classDec, MethodType argu) {

        String className = classDec.f1.f0.toString();
        MethodType classNameM = new MethodType(className,null,null);
        if(classDec.f4.present()) {
            classDec.f4.accept(this, classNameM);
        }

        return null;

    }

    public String visit(ClassExtendsDeclaration classDec, MethodType argu) {

        String className = classDec.f1.f0.toString();
        MethodType classNameM = new MethodType(className,null,null);
        if(classDec.f6.present()) {
            classDec.f6.accept(this, classNameM);
        }

        return null;

    }

    public String visit(MethodDeclaration method, MethodType className) {

        String methodName = method.f2.f0.toString();
        MethodType thisMethod = symbolTable.classes.get(className.inClass).methods.get(methodName);

        if(method.f8.present()){
            method.f8.accept(this, thisMethod);
        }

        if(!(method.f10.accept(this, thisMethod).equals(thisMethod.returnType))){
            System.err.println("Incompatible return type.");
            //System.exit(-1);
            throw new RuntimeException();
        }

        return null;
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
    public String visit(Block n, MethodType inMethod) {

        if(n.f1.present()){
            n.f1.accept(this, inMethod);
        }
        return null;
    }

    public String visit(AssignmentStatement n, MethodType inMethod) {
        String type = n.f0.accept(this,inMethod);
        if(type == null){
            System.err.println("A variable has not been defined.");
            System.err.println(type);
            throw new RuntimeException();
        }
        String type2 = n.f2.accept(this,inMethod);
        if(type2 != type){
            if((symbolTable.classes.containsKey(type))&&(symbolTable.classes.containsKey(type2))){
                if(symbolTable.classes.get(type2).parentClass != null) {
                    if (checkIfChildHasParent(type2,type)) {

                    } else {
                        System.err.println("Assignment error.");
                        throw new RuntimeException();
                    }
                }else{
                    System.err.println("Assignment error.");
                    throw new RuntimeException();
                }
            }else{
                System.err.println("Assignment error.");
                throw new RuntimeException();
            }
        }
        return null;
    }

    public String visit(ArrayAssignmentStatement n, MethodType inMethod) {
        String type = n.f0.accept(this,inMethod);
        if(!type.equals("int []")){
                System.err.println("not array");
                throw new RuntimeException();
        }
        type = n.f2.accept(this,inMethod);
        if(!type.equals("int")){
            System.err.println("error in array");
            System.err.println(type);
            throw new RuntimeException();
        }
        type = n.f5.accept(this,inMethod);
        if(!type.equals("int")){
            System.err.println("wrong array assignment");
            System.err.println(type);
            throw new RuntimeException();
        }
        return null;
    }

    public String visit(IfStatement n,  MethodType inMethod) {
        String type = n.f2.accept(this, inMethod);
        if(!type.equals("boolean")){
            System.err.println("wrong while expression");
            System.err.println(type);
            throw new RuntimeException();
        }
        n.f4.accept(this, inMethod);
        n.f6.accept(this, inMethod);
        return null;
    }

    public String visit(WhileStatement n,  MethodType inMethod) {

        String type = n.f2.accept(this, inMethod);
        if(!type.equals("boolean")){
            System.err.println("wrong if expression");
            System.err.println(type);
            throw new RuntimeException();
        }
        n.f4.accept(this, inMethod);
        return null;
    }

    public String visit(AndExpression n, MethodType inMethod){

        String type1 = n.f0.accept(this, inMethod);
        String type2 = n.f2.accept(this, inMethod);

        if(type1 == null || type2 == null){
            System.err.println("wrong types in AndExpression");
            throw new RuntimeException();
        }
        if(!type1.equals("boolean")){
            System.err.println("wrong types in AndExpression");
            throw new RuntimeException();
        }
        if(!type2.equals("boolean")){
            System.err.println("wrong types in AndExpression");
            throw new RuntimeException();
        }
        if(!(type1.equals(type2))){
            System.err.println("wrong types in AndExpression");
            throw new RuntimeException();
        }else{
            return type1;
        }
        //return type1;
    }

    public String visit(CompareExpression n, MethodType inMethod){

        String type1 = n.f0.accept(this, inMethod);
        String type2 = n.f2.accept(this, inMethod);

        if(type1 == null || type2 == null){
            System.err.println("wrong types in compare symbol");
            throw new RuntimeException();
        }
        if(!type1.equals("int")){
            System.err.println("wrong types in compare symbol");
            throw new RuntimeException();
        }
        if(!type2.equals("int")){
            System.err.println("wrong types in compare symbol");
            throw new RuntimeException();
        }
        if(!(type1.equals(type2))){
            System.err.println("wrong types in compare symbol");
            throw new RuntimeException();
        }else{
            return "boolean";
        }
        //return "boolean";
    }

    public String visit(PlusExpression n, MethodType inMethod){

        String type1 = n.f0.accept(this, inMethod);
        String type2 = n.f2.accept(this, inMethod);

        if(type1 == null || type2 == null){
            System.err.println("wrong types in plus symbol");
            throw new RuntimeException();
        }
        if(!type1.equals("int")){
            System.err.println("wrong types in plus symbol");;
            throw new RuntimeException();
        }
        if(!type2.equals("int")){
            System.err.println("wrong types in plus symbol");
            throw new RuntimeException();
        }
        if(!(type1.equals(type2))){
            System.err.println("wrong types in plus symbol");
            throw new RuntimeException();
        }else{
            return type1;
        }
        //return type1;
    }

    public String visit(MinusExpression n, MethodType inMethod){

        String type1 = n.f0.accept(this, inMethod);
        String type2 = n.f2.accept(this, inMethod);

        if(type1 == null || type2 == null){
            System.err.println("wrong types in minus symbol");
            throw new RuntimeException();
        }
        if(!type1.equals("int")){
            System.err.println("wrong types in minus symbol");;
            throw new RuntimeException();
        }
        if(!type2.equals("int")){
            System.err.println("wrong types in minus symbol");
            throw new RuntimeException();
        }
        if(!(type1.equals(type2))){
            System.err.println("wrong types in minus symbol");
            throw new RuntimeException();
        }else{
            return type1;
        }
        //return type1;
    }

    public String visit(TimesExpression n, MethodType inMethod){

        String type1 = n.f0.accept(this, inMethod);
        String type2 = n.f2.accept(this, inMethod);

        if(type1 == null || type2 == null){
            System.err.println("wrong types in times symbol");
            throw new RuntimeException();
        }
        if(!type1.equals("int")){
            System.err.println("wrong types in times symbol");;
            throw new RuntimeException();
        }
        if(!type2.equals("int")){
            System.err.println("wrong types in times symbol");
            throw new RuntimeException();
        }
        if(!(type1.equals(type2))){
            System.err.println("wrong types in times symbol");
            throw new RuntimeException();
        }else{
            return type1;
        }
        //return type1;
    }

    public String visit(ArrayLookup n, MethodType inMethod){

        String type1 = n.f0.accept(this, inMethod);
        String type2 = n.f2.accept(this, inMethod);

        if(type1 == null || type2 == null){
            System.err.println("wrong types in array lookup");
            throw new RuntimeException();
        }
        if(!type1.equals("int []")){
            System.err.println("wrong types in array lookup");;
            throw new RuntimeException();
        }
        if(!type2.equals("int")){
            System.err.println("wrong types in array lookup");
            throw new RuntimeException();
        }

        return "int";
    }

    public String visit(ArrayLength n, MethodType inMethod){

        String type1 = n.f0.accept(this, inMethod);

        if(type1 == null){
            System.err.println("wrong type in array's lentgh");
            throw new RuntimeException();
        }
        if(!type1.equals("int []")){
            System.err.println("wrong type in array's lentgh");;
            throw new RuntimeException();
        }

        return "int";
    }




    public String visit(MessageSend n, MethodType inMethod){
        String thisClass = n.f0.accept(this, inMethod);
        //System.err.println(thisClass);

        if(thisClass == null){
            System.err.println("wrong class type");
            throw new RuntimeException();
        }
        if(!(symbolTable.classes.containsKey(thisClass))){

            System.err.println("wrong class type");
            throw new RuntimeException();
        }
        String methodName = n.f2.f0.toString();
        if(!(symbolTable.classes.get(thisClass).methods.containsKey(methodName))){
            if(symbolTable.classes.get(thisClass).parentClass != null) {
                thisClass = checkParentForMethod(thisClass, methodName);
                if (thisClass == null) {
                    System.err.println("wrong method name for this class");
                    throw new RuntimeException();
                }
            }else{
                System.err.println("wrong method name for this class");
                throw new RuntimeException();
            }
        }
        MethodType inTempMethod= new MethodType(inMethod);
        //inTempMethod.setTempCallArguments();
        if(n.f4.present()){
            n.f4.accept(this, inTempMethod);
        }

        String [] args = inTempMethod.tempCallArguments.values().toArray(new String[0]);
        //System.err.println("arguments");
        //System.err.println(methodName);

        if(symbolTable.classes.get(thisClass).methods.get(methodName).arguments.keySet().size() != args.length){
            //System.err.println(args.length);
            //System.err.println(args[0]);
            System.err.println("wrong arguments for a method");
            throw new RuntimeException();
        }

        Map<String, String[]> definedArguments = symbolTable.classes.get(thisClass).methods.get(methodName).arguments;

        for(int i=0; i<args.length; i++){
            for (String argName : definedArguments.keySet()){
                if(Integer.parseInt(definedArguments.get(argName)[1]) == i+1) {
                    if(args[i] == null){
                        System.err.println("variable in arguments not defined");
                        throw new RuntimeException();
                    }
                    if (!(args[i].equals(definedArguments.get(argName)[0]))) {
                        if((symbolTable.classes.containsKey(args[i]))&&(symbolTable.classes.containsKey(definedArguments.get(argName)[0]))){
                            if(symbolTable.classes.get(args[i]).parentClass != null) {
                                if (!(checkIfChildHasParent(args[i],definedArguments.get(argName)[0]))) {
                                    System.err.println("wrong arguments for a method");
                                    throw new RuntimeException();
                                }
                            }else{
                                System.err.println("wrong arguments for a method");
                                throw new RuntimeException();
                            }
                        }else{
                            System.err.println("wrong arguments for a method");
                            throw new RuntimeException();
                        }
                    }
                }
            }
        }

        return symbolTable.classes.get(thisClass).methods.get(methodName).returnType;
    }

    public String visit(ExpressionList n, MethodType inMethod) {

        inMethod.tempCallArguments.put(inMethod.tempCallArguments.size() + 1 ,n.f0.accept(this, inMethod));

        n.f1.accept(this, inMethod);

        return null;

    }

    public String visit(ExpressionTail n, MethodType inMethod) {
        if(n.f0.present()){
            n.f0.accept(this, inMethod);
        }
        return null;
    }

    public String visit(ExpressionTerm n, MethodType inMethod) {

        inMethod.tempCallArguments.put(inMethod.tempCallArguments.size() + 1 ,n.f1.accept(this, inMethod));
        return null;
    }

    public String visit(PrintStatement n, MethodType inMethod) {
        String type = n.f2.accept(this, inMethod);
        if(type == null){
            System.err.println("wrong types in print method");
            throw new RuntimeException();
        }
        //type = checkForVar(type, inMethod);

        if(!type.equals("int") && !type.equals("boolean")){
            System.err.println("wrong types in print method");
            throw new RuntimeException();
        }
        return type;
    }


    public String visit(NotExpression n, MethodType inMethod) {
        String type = n.f1.accept(this, inMethod);
        if(!type.equals("boolean")){
            System.err.println("not boolean after ! symbol");
            throw new RuntimeException();
        }
        return "boolean";

    }

    public String visit(IntegerLiteral n, MethodType inMethod){ return "int"; }

    public String visit(TrueLiteral n, MethodType argu) { return "boolean"; }

    public String visit(FalseLiteral n, MethodType argu) { return "boolean"; }

    public String visit(Identifier n, MethodType inMethod){return checkForVar(n.f0.toString(),inMethod);}

    public String visit(ThisExpression n, MethodType inMethod) {
        return inMethod.inClass;
    }

    public String visit(ArrayAllocationExpression n, MethodType inMethod) {
        if(!(n.f3.accept(this,inMethod).equals("int"))){
            System.err.println("Allocation array error");
            throw new RuntimeException();
        }
        return "int []";
    }

    public String visit(AllocationExpression n, MethodType inMethod) {
        String type = n.f1.f0.toString();
        if(!(symbolTable.classes.containsKey(type))){
            System.err.println(type);
            System.err.println("Allocation error");
            throw new RuntimeException();
        }
        return type;
    }

    public String visit(BracketExpression n, MethodType inMethod) {
        return n.f1.accept(this, inMethod);
    }

    public String checkForVar(String varName, MethodType inMethod){
        if(varName == null){
            return null;
        }

        if(inMethod.variables.containsKey(varName)) {
            return inMethod.variables.get(varName);
        }else if(inMethod.arguments.containsKey(varName)){
            return inMethod.arguments.get(varName)[0];
        }else if(symbolTable.classes.get(inMethod.inClass).variables.containsKey(varName)){
            return symbolTable.classes.get(inMethod.inClass).variables.get(varName);
        }else if(symbolTable.classes.get(inMethod.inClass).parentClass != null){
            return checkParentForVars(inMethod.inClass, varName);
        }
        return null;
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
        if(symbolTable.classes.get(thisClass).parentClass.variables.containsKey(varName)){
            return symbolTable.classes.get(thisClass).parentClass.variables.get(varName);
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
