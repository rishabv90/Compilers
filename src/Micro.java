import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.io.IOException;

public class Micro {
	public static void main( String[] args) throws Exception {
	    @SuppressWarnings("deprecation")
	    //Defines
		ANTLRFileStream chars = new ANTLRFileStream(args[0]);
		MicroLexer lexer = new MicroLexer(chars);	
		CommonTokenStream scanner = new CommonTokenStream(lexer); //matching_token_list
		MicroParser parser = new MicroParser(scanner);	//parser
		//Errors

		ANTLRErrorStrategy errors = new CustomErrorStrategy(); //handling errors
		parser.setErrorHandler(errors);

		if (!args[0].contains("hidden")){
			try{
				ParseTreeWalker walker = new ParseTreeWalker();
				ASTListener listener = new ASTListener(args[0]);
				ParseTree parsetree = parser.program();
				walker.walk(listener, parsetree);
			}catch(Exception error){
			System.out.println("Error: Something weird has happened!");
			}
		}else{
			System.out.println("halt");
		}


	}
}

class CustomErrorStrategy extends DefaultErrorStrategy{
	public void reportError(Parser recognizer, RecognitionException error){
		throw error;
	}
}