import java.util.*;
import java.io.*;

class MethodType{


    String inClass;
    String methodName;
    String returnType;
    Map<String, String[]> arguments;
    Map<String, String> variables;
    Map<Integer, String> tempCallArguments;
    public MethodType(String inClass,String returnType,String methodName){
        this.returnType = returnType;
        this.inClass = inClass;
        this.arguments = new HashMap<String, String[]>();
        this.variables = new HashMap<String, String>();
        this.methodName = methodName;
    }

    public MethodType(MethodType method){
        this.returnType = method.returnType;
        this.inClass = method.inClass;
        this.arguments = method.arguments;
        this.variables = method.variables;
        this.tempCallArguments = new HashMap<Integer, String>();
        this.methodName = method.methodName;
    }

    public void setTempCallArguments() {
        this.tempCallArguments = new HashMap<Integer, String>();
    }

}
