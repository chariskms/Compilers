import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;
import java.io.*;

public class SymbolTableVisitor extends GJDepthFirst<String, Map> {

    Map<String, ClassType> classes;

    public SymbolTableVisitor() {
        this.classes = new LinkedHashMap<String, ClassType>();
    }

    public String visit(MainClass main, Map argu) {

        ClassType newMain = new ClassType(null, main.f1.f0.toString());
        MethodType newMethod = new MethodType(null,"void","main");//main.f1.f0.toString(),"void");
        classes.put(main.f1.f0.toString(), newMain);
        newMain.methods.put("main", newMethod);
        String[] argSpecs = new String[2];
        argSpecs[0] = "String []";
        argSpecs[1] = "1";
        newMethod.arguments.put(main.f11.f0.toString(), argSpecs);
      //  newMethod.arguments.put(main.f11.f0.toString(), 1);
        if(main.f14.present()) {
            main.f14.accept(this, newMethod.variables);
        }
        return null;
    }

    public String visit(ClassDeclaration cl, Map argu){

        if(classes.containsKey(cl.f1.f0.toString())){
            System.err.println("Double declaration of a class.");
            throw new RuntimeException();
        }

        ClassType newClass = new ClassType(null, cl.f1.f0.toString());
        classes.put(cl.f1.f0.toString(),newClass);
        if(cl.f3.present()) {
            cl.f3.accept(this, newClass.variables);
        }
        if(cl.f4.present()) {
            cl.f4.accept(this, newClass.methods);
        }

        return null;

    }

    public String visit(ClassExtendsDeclaration cl, Map argu){

        if(classes.containsKey(cl.f1.f0.toString())){
            System.err.println("Double declaration of a class.");
            throw new RuntimeException();
        }

        if(classes.containsKey(cl.f3.f0.toString())==false){
            System.err.println("Parent class not declared.");
            throw new RuntimeException();
        }

        ClassType newClass = new ClassType(classes.get(cl.f3.f0.toString()), cl.f1.f0.toString());
        classes.put(cl.f1.f0.toString(),newClass);

        if(cl.f5.present()) {
            cl.f5.accept(this, newClass.variables);
        }

//        newClass.methods.putAll((classes.get(cl.f3.f0.toString())).methods);

        if(cl.f6.present()) {
            cl.f6.accept(this, newClass.methods);
        }
        return null;

    }

    public String visit(VarDeclaration var, Map variables) {

        String Type = var.f0.accept(this,null);
        String Name = var.f1.f0.toString();
        if(variables.containsKey(Name)){
            System.err.println("Double declaration of a variable.");
            throw new RuntimeException();
        }
        variables.put(Name, Type);

        return null;
    }

    public String visit(Type n, Map argu) {

            return n.f0.accept(this, null);

    }

    public String visit(IntegerType n, Map argu) {
        return "int";
    }

    public String visit(BooleanType n, Map argu) {
        return "boolean";
    }

    public String visit(ArrayType n, Map argu) {
        return "int []";
    }

    public String visit(Identifier n, Map argu) {
            return n.f0.toString();
    }

    public String visit(MethodDeclaration method, Map methods) {

        if(methods.containsKey(method.f2.f0.toString())){
            System.err.println("Double declaration of a method.");
            throw new RuntimeException();
        }
        MethodType newMethod = new MethodType(null,method.f1.accept(this,null),method.f2.f0.toString());
        if(method.f4.present()) {
            method.f4.accept(this, newMethod.arguments);
        }
        if(method.f7.present()) {
            method.f7.accept(this, newMethod.variables);
        }
        methods.put(method.f2.f0.toString(), newMethod);

        return null;

    }

    public String visit(FormalParameter args, Map arguments){

        String Type = args.f0.accept(this,null);
        String Name = args.f1.f0.toString();

        if(arguments.containsKey(Name)){
            System.err.println("Double declaration of a parameter.");
            throw new RuntimeException();
        }

        String[] argSpecs = new String[2];
        argSpecs[0] = Type;
        Integer argNum = arguments.size() + 1 ;
        argSpecs[1] = argNum.toString();
        arguments.put(Name, argSpecs);

        return null;

    }

    public void secondCheck(){
        int oneMainReturn = 0;
        int oneMainArg = 0;

        for (ClassType oneclass : classes.values()) {

            //dipli dilwsi san orisma kai san metavliti
            for (String methodName1 : oneclass.methods.keySet()) {
                for (String checkSameVar : oneclass.methods.get(methodName1).variables.keySet()) {
                    for (String checkSameArg : oneclass.methods.get(methodName1).arguments.keySet()) {
                        if (checkSameVar.equals(checkSameArg)) {
                            System.err.println("Double declaration of a variable");
                            throw new RuntimeException();

                        }
                    }
                }
            }

            if(oneclass.parentClass != null){
                for (String methodName : oneclass.methods.keySet()) {
                    for (String parentMethodName : oneclass.parentClass.methods.keySet()) {
                        if(methodName.equals(parentMethodName)){
                            if(!compareArgs(oneclass.methods.get(methodName).arguments,oneclass.parentClass.methods.get(parentMethodName).arguments)){
                                System.err.println("Double declaration of a method in subclass.");
                                throw new RuntimeException();
                            }
                            if(!(oneclass.methods.get(methodName).returnType.equals(oneclass.parentClass.methods.get(parentMethodName).returnType))){
                                System.err.println("Double declaration of a method in subclass.");
                                throw new RuntimeException();
                            }
                        }
                    }
                }
            }

            for(String tempVar : oneclass.variables.values()){

                if(!checkVarType(tempVar)){
                    System.err.println("Wrong type of a variable.");
                    throw new RuntimeException();
                }
            }

            for (MethodType method : oneclass.methods.values()) {
                method.inClass = oneclass.className;
                for(String tempVar : method.variables.values()){

                    if(!checkVarType(tempVar)){
                        System.err.println("Wrong type of a variable.");
                        throw new RuntimeException();
                    }
                }
                boolean equalFlag = checkVarType(method.returnType);

                if(oneMainReturn == 0 && method.returnType.equals("void")){
                    oneMainReturn = 1;
                    equalFlag = true;
                }

                if(!equalFlag){
                    System.err.println("Wrong type of a variable.");
                    throw new RuntimeException();
                }

                for (String[] argsSpecs : method.arguments.values()) {
                    boolean equalFlagA = checkVarType(argsSpecs[0]);

                    if(oneMainArg == 0 && argsSpecs[0].equals("String []")){
                        oneMainArg= 1;
                        equalFlagA = true;
                    }

                    if(!equalFlagA){
                        System.err.println("Wrong type of a variable.");
                        throw new RuntimeException();
                    }
                }
            }
        }

    }

    public boolean checkVarType(String tempVar){
        boolean equalFlag = false;
        if(tempVar.equals("int") || tempVar.equals("boolean") || tempVar.equals("int []")) {
            equalFlag = true;
        }
        for(String className : classes.keySet()){
            if(className.equals(tempVar)){
                equalFlag = true;
            }
        }
        return equalFlag;
    }

    public boolean compareArgs(Map<String, String[]> args1, Map<String, String[]> args2){
        int equalFlag = 0;
        for(String argName1 : args1.keySet()){
            for (String argName2 : args2.keySet()){
                if(argName1.equals(argName2)){
                    if((args1.get(argName1)[0]).equals(args1.get(argName2)[0])) {
                        if ((args1.get(argName1)[1]).equals(args1.get(argName2)[1])){
                            equalFlag = equalFlag + 1;
                        }
                    }
                }
            }
        }
        if(equalFlag == args1.size() && equalFlag == args2.size()){
            return true;
        }else{
            return false;
        }
    }
}
