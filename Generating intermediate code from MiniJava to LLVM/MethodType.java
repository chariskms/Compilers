import java.util.*;
import java.io.*;

class MethodType{


    String inClass;
    String methodName;
    String returnType;
    Map<String, String[]> arguments;
    Map<String, String> variables;
    Map<Integer, String> tempCallArguments;
    Map<Integer, String> tempCallArgumentsCodeG;
    Integer transferedArg;
    String returnedType;
    public MethodType(String inClass,String returnType,String methodName){
        this.returnType = returnType;
        this.transferedArg = 0;
        this.inClass = inClass;
        this.arguments = new LinkedHashMap<String, String[]>();
        this.variables = new LinkedHashMap<String, String>();
        tempCallArgumentsCodeG = new LinkedHashMap<Integer, String>();
        this.methodName = methodName;
    }
    /*
    public void SetTransferedArg(){
        this.transferedArg = 1;
    }

    public boolean checkTransferedArg(){
        if(this.transferedArg == 0){
            return false;
        }else{
            return true;
        }
    }
    public String getThisType(){
        return this.returnedType;
    }

    public void SaveThisType(String type){
        this.returnedType = type;
    }

    public void CleanTransferedArg(){
        this.transferedArg = 0;
        this.returnedType = null;
    }
    */
    public MethodType(MethodType method){
        this.returnType = method.returnType;
        this.inClass = method.inClass;
        this.arguments = method.arguments;
        this.variables = method.variables;
        this.tempCallArguments = new LinkedHashMap<Integer, String>();
        this.methodName = method.methodName;
    }

    public void setTempCallArguments() {
        this.tempCallArguments = new HashMap<Integer, String>();
    }

}
