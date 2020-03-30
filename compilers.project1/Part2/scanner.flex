
import java_cup.runtime.*;

%%

%class Scanner


%line
%column


%cup

%{
StringBuffer stringBuffer = new StringBuffer();
private Symbol symbol(int type) {
   return new Symbol(type, yyline, yycolumn);
}
private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
}
%}


LineTerminator = \r|\n|\r\n

WhiteSpace     = {LineTerminator} | [ \t\f]

Identifier = [:jletter:] [:jletterdigit:]*

funcToken = ")" [ ]* "{"

IfStatement = "if"
ElseStatement = "else"

Prefix = "prefix"
Suffix = "suffix"

%state STRING

%%


<YYINITIAL> {
/* operators */

 {Prefix} 	{return symbol(sym.PREFIX); }
 {Suffix}	{return symbol(sym.SUFFIX); }
 {IfStatement}   { return symbol(sym.IF); }
 {ElseStatement} { return symbol(sym.ELSE); } 
 ","		{ return symbol(sym.COMMA); }
 "}"		{ return symbol(sym.RBRACKET); }
 "+"            { return symbol(sym.PLUS); }
 "("            { return symbol(sym.LPAREN); }
 ")"            { return symbol(sym.RPAREN); }
 \"             { stringBuffer.setLength(0); yybegin(STRING); }
 {WhiteSpace}   { /* just skip what was found, do nothing */ }
 {funcToken} 	{ return symbol(sym.FUNCTOK); }
 {Identifier}   { return symbol(sym.ID, new String(yytext())); }
}



<STRING> {
      \"                             { yybegin(YYINITIAL);
                                       return symbol(sym.STRING_LITERAL, stringBuffer.toString()); }
      [^\n\r\"\\]+                   { stringBuffer.append( yytext() ); }
      \\t                            { stringBuffer.append('\t'); }
      \\n                            { stringBuffer.append('\n'); }

      \\r                            { stringBuffer.append('\r'); }
      \\\"                           { stringBuffer.append('\"'); }
      \\                             { stringBuffer.append('\\'); }
}


[^]                    { throw new Error("Illegal character <"+yytext()+">"); }
