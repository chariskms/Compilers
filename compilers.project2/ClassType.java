import java.util.*;
import java.io.*;

class ClassType{

    String className;
    ClassType parentClass;
    Map<String, String> variables;
    Map<String, MethodType> methods;

    public ClassType(ClassType parentClass,String className){
        this.className = className;
        if(parentClass != null){
            this.parentClass = parentClass;
        }
        this.variables = new LinkedHashMap<String, String>();
        this.methods = new LinkedHashMap<String, MethodType>();

    }
}
