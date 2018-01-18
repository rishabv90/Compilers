import org.antlr.v4.runtime.*;
import java.util.Stack;
import java.util.HashMap;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Iterator;

class SymbolTable {
	private String scope;
	public LinkedHashMap<String, Symbol> Map;

	public SymbolTable(String scope_in){
		this.Map = new LinkedHashMap<String, Symbol>();
		this.scope = scope_in;
	}

	public void printSymbolTable(){
		// System.out.println("Symbol table " + this.scope);
		Iterator<Symbol> iterator = this.Map.values().iterator();
		while(iterator.hasNext())
		{
			Symbol current = iterator.next();
			if(current.type == "STRING"){
				// System.out.println("name"+ " " + current.name + " type " + current.type + " value " + current.value);
						
			}else {
				// System.out.println("name"+ " " + current.name + " type "+ current.type);
			}
		}
		// System.out.println();
	}
}

public class SymbolTableStack {
	public static Stack<SymbolTable> stack = new Stack<SymbolTable>();
	private static int i = 1; 

	public static void newScope(String name){
		switch(name){
			case "global": 
				stack.push(new SymbolTable("GLOBAL"));
				break;
			case "block":  
				String block = "BLOCK" + " " + i;
				i++;
				stack.push(new SymbolTable(block));
				break;
			default:
				stack.push(new SymbolTable(name));
				break;
		}
	}

	public static void pushin(String name_in, String type_in) {
		SymbolTable table = stack.pop();
		String[] list = name_in.trim().split(",");
		//check dup, add
		for (int i = 0; i < list.length; i++) {
			Symbol sym = new Symbol(list[i], type_in, null);
			
			if(table.Map.containsKey(sym.name)){
				System.out.println("DECLARATION ERROR" + " " + sym.name);
				System.exit(0);
			}
			else{
				table.Map.put(sym.name, sym);
			}

		}
		stack.push(table);
		
	}

	public static void pushin(String name_in, String type_in, String value_in){
		SymbolTable table = stack.pop();
		Symbol sym = new Symbol(name_in,type_in,value_in);
		//check dup, add
		if(table.Map.containsKey(sym.name)){
			System.out.println("DECLARATION ERROR" + " " + sym.name);	
		}
		else{
			table.Map.put(sym.name, sym);
		}
		stack.push(table);
	}

}

class Symbol{
	public String name, type, value;
	public Symbol(String name, String type, String value){
		this.name = name;
		this.type = type;
		this.value = value;
	}
}
