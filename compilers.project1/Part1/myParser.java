import java.io.InputStream;
import java.io.IOException;

class myParser {

    private int lookaheadToken;

    private InputStream in;

    public myParser(InputStream in) throws IOException {
	this.in = in;
	lookaheadToken = in.read();
    }

    private void consume(int symbol) throws IOException, ParseError {
	if (lookaheadToken != symbol)
	    throw new ParseError();
	lookaheadToken = in.read();
    }

    private int evalDigit(int digit){
        return digit - '0';
    }

    private int expr() throws IOException, ParseError {
	if((lookaheadToken < '0' || lookaheadToken > '9') && lookaheadToken != '(')
	    throw new ParseError();
	int temp;
	temp = term();
	temp = expr2(temp);
	return temp;
    }


    private int expr2(int termRes) throws IOException, ParseError {
	if(lookaheadToken == ')' || lookaheadToken == '\n' || lookaheadToken == 13 || lookaheadToken == -1) {
        return termRes;
    }
	if(lookaheadToken != '^')
	    throw new ParseError();
	consume('^');
	int temp = expr();
	temp = termRes ^ temp;
	expr2(temp);
	return temp;
    }

    private int term() throws IOException, ParseError {
        if((lookaheadToken < '0' || lookaheadToken > '9')&& lookaheadToken != '(') {
            throw new ParseError();
        }
        int temp = factor();
        temp = term2(temp);

		return temp;
    }

    private int term2(int factorRes) throws IOException, ParseError {
        if (lookaheadToken == ')' || lookaheadToken == '^' || lookaheadToken == '\n' || lookaheadToken == 13 || lookaheadToken == -1)
            return factorRes;
        if (lookaheadToken != '&'){
            throw new ParseError();
        }
        consume('&');
        int temp = factor();
		temp = factorRes & temp;
        term2(temp);
		return temp;
    }

    private int factor() throws IOException, ParseError {
        if((lookaheadToken < '0' || lookaheadToken > '9' )&& lookaheadToken != '(') {
            throw new ParseError();
        }
        if(lookaheadToken == '('){
            consume('(');
            int temp = expr();
            if(lookaheadToken != ')'){
                throw new ParseError();
            }
            consume(')');
			return(temp);
        }else {
            return(num());
        }
    }

    private int num() throws IOException, ParseError {
		int temp = evalDigit(lookaheadToken);
        consume(lookaheadToken);
        return(temp);
    }


    public void parse() throws IOException, ParseError {
        int temp = expr();

        if (lookaheadToken != '\n' && lookaheadToken != 13 && lookaheadToken != -1)
            throw new ParseError();
        System.out.print(temp);
    }


    public static void main(String[] args) {
	try {
	    myParser parser = new myParser(System.in);
	    parser.parse();
	}
	catch (IOException e) {
	    System.err.println(e.getMessage());
	}
	catch(ParseError err){
	    System.err.println(err.getMessage());
	}
    }
}

