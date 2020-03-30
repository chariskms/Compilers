import syntaxtree.*;
import visitor.*;
import java.util.*;
import java.io.*;

public class Main {
    public static void main (String [] args) {

        for (int i = 0; i < args.length; i++) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(args[i]);
                MiniJavaParser parser = new MiniJavaParser(fis);
                Goal root = parser.Goal();
                //System.err.println("Program parsed successfully.");
                SymbolTableVisitor symbolTable = new SymbolTableVisitor();
                root.accept(symbolTable, null);
                    //System.out.println(symbolTable.classes.toString());

                symbolTable.secondCheck();

                TypeCheckVisitor typeCheck = new TypeCheckVisitor(symbolTable);
                root.accept(typeCheck, null);
                int varOffset ;
                int methodOffset ;

                int classCount = 0;
                Map<String, Integer[]> offsets= new HashMap<String, Integer[]>();
                for (String oneclass : symbolTable.classes.keySet()) {
                    if(classCount == 0) {
                        Integer[] temp = new Integer[2];
                        temp[0] = 0;
                        temp[1] = 0;
                        offsets.put(oneclass,temp);
                        classCount ++;
                        continue;
                    }
                    //System.out.println("class : " + oneclass);

                    varOffset = 0;
                    methodOffset = 0;
                    if(symbolTable.classes.get(oneclass).parentClass != null){
                        varOffset = offsets.get(symbolTable.classes.get(oneclass).parentClass.className)[0];
                        methodOffset = offsets.get(symbolTable.classes.get(oneclass).parentClass.className)[1];
                    }
                    for (String varName : symbolTable.classes.get(oneclass).variables.keySet()) {

                        System.out.println(oneclass + "." + varName + " : " + varOffset);
                        if(symbolTable.classes.get(oneclass).variables.get(varName).equals("int")){
                            varOffset = varOffset + 4;
                        }else if(symbolTable.classes.get(oneclass).variables.get(varName).equals("boolean")){
                            varOffset = varOffset + 1;
                        }else if(symbolTable.classes.get(oneclass).variables.get(varName).equals("int []")){
                            varOffset = varOffset + 8;
                        }else{
                            varOffset = varOffset + 8;
                        }
                    }
                    for (String methodName : symbolTable.classes.get(oneclass).methods.keySet()) {
                        if(symbolTable.classes.get(oneclass).parentClass != null){
                            if(symbolTable.classes.get(oneclass).parentClass.methods.containsKey(methodName)){
                                //skip
                            }else {
                                System.out.println(oneclass + "." + methodName + " : " + methodOffset);
                                methodOffset = methodOffset + 8;
                            }
                        }else{
                            System.out.println(oneclass + "." + methodName + " : " + methodOffset);
                            methodOffset = methodOffset + 8;
                        }
                    }
                    Integer[] temp = new Integer[2];
                    temp[0] = varOffset;
                    temp[1] = methodOffset;
                    offsets.put(oneclass,temp);

                }

                System.out.println("Program parsed and type checked successfully.");
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            } catch (FileNotFoundException ex) {
                System.err.println(ex.getMessage());
            }catch (RuntimeException run){
                    continue;
            } finally {
                try {
                    if (fis != null) fis.close();
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
}
