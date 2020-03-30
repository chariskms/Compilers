import java_cup.runtime.*;
import java.io.*;

class Main {
    public static void main(String[] argv) throws Exception{
        System.out.println("public class Main {");
        Parser p = new Parser(new Scanner(new InputStreamReader(System.in)));
        p.parse();
	System.out.println("}");
    }
}
