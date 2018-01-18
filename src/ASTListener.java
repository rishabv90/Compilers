import org.antlr.v4.runtime.*;
import java.util.*;

public class ASTListener extends MicroBaseListener {
	private List<Node> IRNodeList = new ArrayList<Node>();
	private List<Node> IRNodeStore = new ArrayList<Node>();
	private List<Node> TinyNodeList = new ArrayList<Node>();
	private List<String> PrintList = new ArrayList<String>();
	private Hashtable<String, String> TypeMap = new Hashtable<String, String>();
	private Hashtable<String, String> ValueMap = new Hashtable<String, String>();
	private Hashtable<String, String> TinyTypeMap = new Hashtable<String, String>();
	private Hashtable<String, String> RegMap = new Hashtable<String, String>();
	private Stack<String> Stack = new Stack<String>();
	private Stack<String> LabelStack = new Stack<String>();
	private int Counter = 0;
	private int label_count = 0;
	private boolean newline = true;
	private boolean for_enable = false;
	private boolean if_enable = false;
	private boolean incr_enable = false;
	private boolean call_enable = false;
	private int i;
	private int reg_count = 0, extra_reg = 0, call_count= 0;
	private Stack<String> Call_Stack = new Stack<String>();
	private String fp;
	private boolean print;

	public ASTListener(String output){
		this.fp = output;
		if (fp.contains("test") & !fp.contains("hidden")){
			// System.out.println(";asdasdasd")
			print = false;
		} else{
			print = false;
		}

	}

	private Node getNode (String opcode, String first, String second, String dest){
		Node node = new Node(opcode,first,second,dest);
		return node;
	}
	private boolean isFloat (String Text){
		if(Text.matches("[0-9]*\\.[0-9]+")){
			return true;
		}
		return false;
	}
	private boolean isInt (String Text){
		if(Text.matches("[0-9]+")){
			return true;
		} 
		return false;
	}
	private boolean isString (String Text){
		if(isFloat(Text) || isInt(Text)){
			return false;
		}else{
			return true;
		}
	}
	private String getT (){
		return "!T"+Integer.toString(Counter++);
	}

	private String getT (boolean boo){
		if(boo){
			return "!T"+Integer.toString(Counter++);
		}else{
			int tmp = Counter - 1;
			return "!T"+Integer.toString(tmp);
		}
		
	}

	private int Dollar_Counter = 1;
	private String getDollar (){
		return "$-"+Integer.toString(Dollar_Counter++);
	}


	@Override public void exitExpr_prefix(MicroParser.Expr_prefixContext ctx) {
		if(ctx.getChildCount() == 0 || ctx.getChild(0).getChildCount() <=1 ){
			return;
		}else{ 
			//Save op_tmp
			String op_tmp = Stack.pop();
			//Construct IR Node with 3 pops
			String second = Stack.pop();
			String op = Stack.pop();
			String first = Stack.pop();
			String dest = getT();

			//Value Update
			String first_ValueMap = update_ValueMap(first);
			String Second_ValueMap = update_ValueMap(second);

			//get modified opcode for IR Node
			String IRopcode = getCorrectOpcode(TypeMap.get(first),op);
			Stack.push(dest);
			//add dest with type
			// System.out.println(first_ValueMap + " "+Second_ValueMap);
			TypeMap.put(dest, TypeMap.get(first));
			if(incr_enable){
				IRNodeStore.add(getNode(IRopcode,first_ValueMap,Second_ValueMap,dest));
			}else{
				IRListAdd(IRopcode,first_ValueMap,Second_ValueMap,dest);
			}		

			//Recover op_tmp
			Stack.push(op_tmp);
		}
	}

	@Override public void exitFactor_prefix(MicroParser.Factor_prefixContext ctx) {
		if(ctx.getChildCount() == 0 || ctx.getChild(0).getChildCount() <= 1){
			return;
		}else{ 
			//Save op_tmp
			String op_tmp = Stack.pop();
			//Construct IR Node with 3 pops
			String second = Stack.pop();
			String op = Stack.pop();
			String first = Stack.pop();
			String dest = getT();

			//Value Update
			String first_ValueMap = update_ValueMap(first);
			String Second_ValueMap = update_ValueMap(second);

			// System.out.println(first_ValueMap + " "+Second_ValueMap);
			//get modified opcode for IR Node
			String IRopcode = getCorrectOpcode(TypeMap.get(first),op);
			Stack.push(dest);
			//add dest with type
			TypeMap.put(dest, TypeMap.get(first));
			//add to IR Node list
			if(incr_enable){
				IRNodeStore.add(getNode(IRopcode,first_ValueMap,Second_ValueMap,dest));
			}else{
				IRListAdd(IRopcode,first_ValueMap,Second_ValueMap,dest);
			}
			//Recover op_tmp
			Stack.push(op_tmp);
		}
	}

	@Override public void exitExpr(MicroParser.ExprContext ctx) {
		
		if(isInt(ctx.getText()) || isFloat(ctx.getText()) || (ctx.getChild(0).getChildCount() <= 1)) {
			
		}else{
			//Construct IR Node with 3 pops
			String second = Stack.pop();
			String op = Stack.pop();
			String first = Stack.pop();
			String dest = getT();
			// System.out.println(op+first+second+dest+TypeMap.get(first));
			//get modified opcode for IR Node
			// String IRopcode = getCorrectOpcode(TypeMap.get(first),op);

			// System.out.println(dest+ " " + TypeMap.get(first));
			//Value Update
			String first_ValueMap = update_ValueMap(first);
			String Second_ValueMap = update_ValueMap(second);

			String IRopcode = getCorrectOpcode(TypeMap.get(first),op);
			//call
			if (call_enable){
				Call_Stack.push(dest);
			}
			if (call_enable){
			call_count += 1;
			Call_Stack.push(dest);
			// ValueMap
			}
			// System.out.println(first_ValueMap + " "+Second_ValueMap);
			Stack.push(dest);
			TypeMap.put(dest,TypeMap.get(first));
			ValueMap.put(dest, dest);
			//add dest with type
			// TypeMap.put(dest, TypeMap.get(first));
			//add to IR Node list
			if(incr_enable){
				IRNodeStore.add(getNode(IRopcode,first_ValueMap,Second_ValueMap,dest));
			}else{
				IRListAdd(IRopcode,first_ValueMap,Second_ValueMap,dest);
			}
			return;
		}
		if (call_enable){
			String second = Stack.pop();
			String op = Stack.pop();
			String first = Stack.pop();
			// System.out.println(first + " " + second + " " +op + ValueMap.get(first));

			Stack.push(first);
			Stack.push(op);
			Stack.push(second);
			call_count += 1;
			Call_Stack.push(second);
			ValueMap.put(second, ValueMap.get(second));
		}

	}

	@Override public void exitFactor(MicroParser.FactorContext ctx) {
		if(isInt(ctx.getText()) || isFloat(ctx.getText()) || (ctx.getChild(0).getChildCount() <= 1)){
			return;
		}else{
			//Construct IR Node with 3 pops
			String second = Stack.pop();
			String op = Stack.pop();
			String first = Stack.pop();
			String dest = getT();
			// System.out.println(Dest);
			//Value Update
			String first_ValueMap = update_ValueMap(first);
			String Second_ValueMap = update_ValueMap(second);

			//get modified opcode for IR Node
			String IRopcode = getCorrectOpcode(TypeMap.get(first),op);
			Stack.push(dest);
			TypeMap.put(dest,TypeMap.get(first));
			//add dest with type
			// System.out.println(dest + " " +TypeMap.get(first));

			TypeMap.put(dest, TypeMap.get(first));
			//add to IR Node list
			if(incr_enable){
				IRNodeStore.add(getNode(IRopcode,first_ValueMap,Second_ValueMap,dest));
			}else{
				IRListAdd(IRopcode,first_ValueMap,Second_ValueMap,dest);
			}
			// System.out.println(getNode(IRopcode,first,second,dest).getIRText());
		}
	}

	@Override public void exitPrimary(MicroParser.PrimaryContext ctx) {
		String first = ctx.getText();
		if (isInt(first)){
			String Dest = getT();
			// System.out.println(Dest);
			Stack.push(Dest);
			TypeMap.put(Dest,"INT");
			TypeMap.put(first,"INT");	
			ValueMap.put(Dest, first);
			if(incr_enable){
				IRNodeStore.add(getNode("STOREI", first, null, Dest));
			}else{
				IRListAdd("STOREI", first, Dest);
			}
		}else if(isFloat(first)){
			//create new dest
			String Dest = getT();
			//add dest with type
			// System.out.println(Dest);
			Stack.push(Dest);
			TypeMap.put(Dest,"FLOAT");
			TypeMap.put(first,"FLOAT");
			ValueMap.put(Dest, first);
			//add node
			if(incr_enable){
				IRNodeStore.add(getNode("STOREF", first, null, Dest));
			}else{
				IRListAdd("STOREF", first, Dest);
			}
		}else if(isString(first)){ //UNKNOWN
		// String Dest = getT();
		// System.out.println(Dest);
		// Stack.push(Dest);
		// TypeMap.put(Dest,"String"); //gaibian
			// System.out.println(first);
		}
	}

	@Override public void exitId(MicroParser.IdContext ctx) {
		Stack.push(ctx.getText());
	}
	@Override public void exitAddop(MicroParser.AddopContext ctx) {
		Stack.push(ctx.getText());
	}
	@Override public void exitMulop(MicroParser.MulopContext ctx) {
		Stack.push(ctx.getText());
	}

	@Override public void exitWrite_stmt(MicroParser.Write_stmtContext ctx) {
		// String dest = Stack.pop();
		// System.out.println("exitRead_stmt: "+dest + " "+ TypeMap.get(dest));
		String[] id_list = ctx.getChild(2).getText().trim().split(",");
		String item_ValueMap;
		for(String item: id_list){
			// System.out.println("exitRead_stmt: "+item + " "+ TypeMap.get(item));
			String type = TypeMap.get(item);
			item_ValueMap = ValueMap.get(item);
			// IRListAdd(item, type, null, null));

			//Put items in id_list to IR Node list depends on items' types
			if(type.contains("INT")) {
				IRListAdd("WRITEI", item_ValueMap);
			} else if (type.contains("FLOAT")) {
				IRListAdd("WRITEF", item_ValueMap);
			} else if (type.contains("STRING")){
				// System.out.println(item + " " + item_ValueMap);
				TinyNodeList.add(getNode("str",item,null,item_ValueMap));

				// if (item.contains("newline")){
				// 	if(newline == true){
				// 		TinyNodeList.add(getNode("str","newline",null,"\"\\n\""));
				// 		newline = false;
				// 	}
				// }else if(item.contains("eol")){
				// 	TinyNodeList.add(getNode("str","eol",null,"\"\\n\""));
				// }else if(item.contains("space")){
				// 	TinyNodeList.add(getNode("str","space",null," \" \""));
				// }

				IRListAdd("WRITES", item);
			} else {
				System.out.println("Error: Write error, "+ item +" " +type);
			}
		}

	}
	@Override public void exitRead_stmt(MicroParser.Read_stmtContext ctx) {
		String[] id_list = ctx.getChild(2).getText().trim().split(",");
		String item_ValueMap;
		for(String item: id_list){
			String type = TypeMap.get(item);
			item_ValueMap = ValueMap.get(item);
			String IRopcode = "READ" + ((type.contains("INT")) ? "I" : "F");
			IRListAdd(IRopcode, item_ValueMap);
		}
	}

	// var string assign
	@Override public void exitVar_decl(MicroParser.Var_declContext ctx) {
		String[] id_list = ctx.getChild(1).getText().trim().split(",");
		//Update Tiny Node list from id_list
		for(String item: id_list){
			var_count++;
			
			ValueMap.put(item, "$" + --var_num);
			// System.out.println(item + " " + "$" + var_num + " "+ ctx.getChild(0).getText());
			TypeMap.put(item, ctx.getChild(0).getText());
			TypeMap.put("$" + var_num, ctx.getChild(0).getText());
			// TinyNodeList.add(getNode("var",item,null,null));
		}

	}
	@Override public void exitString_decl(MicroParser.String_declContext ctx) {
		//string_decl       : 'STRING' id ':=' str ';' 
		//                        0      1  2   3   4
		String string = ctx.getChild(0).getText();
		String id = ctx.getChild(1).getText();
		String str = ctx.getChild(3).getText();
		Stack.push(ctx.getText());

		TypeMap.put(id, "STRING");
		ValueMap.put(id,str);
		// TinyNodeList.add(getNode("str", id, str));
	 }

	@Override public void exitAssign_expr(MicroParser.Assign_exprContext ctx) {
		String first = Stack.pop();
		String dest = Stack.pop();
		String type = TypeMap.get(dest);
		// System.out.println(first+dest+type);
		String IRopcode = "STORE" + ((type.contains("INT")) ? "I" : "F");
		// Counter--;
		String dest_ValueMap = ValueMap.get(ctx.getChild(0).getText());
		// String IRopcode = "STORE?";
		// System.out.println(dest_ValueMap);

		if(call_enable){
	       	pop_callcount();
	       	IRListAdd("POPREG");
	       	// Counter++;
	       	String T = getT();
	       	IRListAdd("POP", T);
	       	// System.out.println(T);

	       	call_enable = false;
	       	// IRListAdd("popdone");
		}

		// IRListAdd("back");
		
		String first_Update = getT(false);
		// System.out.println(first_Update);
		if(incr_enable){
			IRNodeStore.add(getNode(IRopcode, first_Update, null, dest_ValueMap));
		}else{
			IRListAdd(IRopcode, first_Update, dest_ValueMap);
		}
	}

	//IF else cond

	@Override public void enterIf_stmt(MicroParser.If_stmtContext ctx) {
		// IRListAdd("enterIf",null,null,null));
		if_enable = true;
	}

	@Override public void exitIf_stmt(MicroParser.If_stmtContext ctx) {
		link_enable = false;
		// IRListAdd("exitIf",null,null,null));
		IRListAdd("LABEL", LabelStack.pop());
		// IfElseCounter++;
		// IRListAdd("LABEL1",null,null,null));
	}

	@Override public void enterElse_part(MicroParser.Else_partContext ctx) {
		// IRListAdd("enterElse",null,null,null));
		String tmp_Label = LabelStack.pop();
		IRListAdd("JUMP", tmp_Label);
		
		IRListAdd("LABEL", LabelStack.pop());
		LabelStack.push(tmp_Label);
		// IRListAdd("JUMP", null, null, null));
		// IRListAdd("LABEL", null, null, null));
	}

	@Override public void enterFor_stmt(MicroParser.For_stmtContext ctx) {
		for_enable = true;
	}

	@Override public void exitInit_stmt(MicroParser.Init_stmtContext ctx){

		String Label = "label" + Integer.toString(++label_count);
		LabelStack.push(Label);
		IRListAdd("LABEL", Label);
		Label =  "label" + Integer.toString(++label_count);
		LabelStack.push(Label);
	}


	@Override public void enterIncr_stmt(MicroParser.Incr_stmtContext ctx){
		incr_enable = true;
	}

	@Override public void exitIncr_stmt(MicroParser.Incr_stmtContext ctx){
		incr_enable = false;
	}

	@Override public void exitFor_stmt(MicroParser.For_stmtContext ctx) {
		String Label1 = LabelStack.pop();
		String Label2 = LabelStack.pop();
		String Label3 = LabelStack.pop();
		IRListAdd("LABEL", Label2);
        // add note in storage (delayed add)
        for (i = 0; i < IRNodeStore.size(); i++){
        	IRNodeList.add(IRNodeStore.get(i));
		}
		IRListAdd("JUMP", Label3);
		IRListAdd("LABEL", Label1);
	}

	@Override public void exitCond(MicroParser.CondContext ctx) {
		String Label,Label2;
		label_count++;
		if(if_enable){
			// Label = getIfElseLabel(2);
			Label = "label" + Integer.toString(label_count++);
			LabelStack.push(Label);
			// label_count++;
			Label2 =  "label" + Integer.toString(label_count);
			LabelStack.push(Label2);
			// LabelStack.push(getIfElseLabel(1));
			if_enable = false;
		}else{
			// Label = "Error_exitCond";
			Label = "label" + Integer.toString(label_count);
			LabelStack.push(Label);
		}
		String second = Stack.pop();
		String first = Stack.pop();

		// first_update
		first = update_ValueMap(first);
		second = update_ValueMap(second);

		IRListAdd(getCorrectOpcode(null,ctx.getChild(1).getText()),first,second,Label);

	}


// STEP 6 NEW
	private String func_name;
	private int IfElseCounter = 0;
	private int var_count= 0;
	private int fp_count = 0;
	private int param_num = 1, l_num = 0, var_num = 0;
	private boolean link_enable = false;

/* notes
	
	STOREI/F (exitReturn_stmt) ok
	store?  (exitAssign_expr) ok
	LE,NE (exitCond) ok
	SUBI ok

	
	exitReturn_stmt $? ok
	label number ok
	link number ok
	push reg? ok

*/

	private String update_ValueMap(String str){
		if (str.contains("!T")){
			return str;
		}
		return ValueMap.get(str);
	}

	@Override public void enterProgram(MicroParser.ProgramContext c) {
		IRListAdd("PUSH");
		IRListAdd("PUSH");
		IRListAdd("PUSH");
		IRListAdd("PUSH");
		IRListAdd("PUSH");
		IRListAdd("JSR", getFunc("main"));
		IRListAdd("HALT");
	}

	@Override public void exitParam_decl(MicroParser.Param_declContext ctx) {
   		param_num++;
   		// System.out.println(ctx.getChild(1).getText());
		ValueMap.put(ctx.getChild(1).getText(), "$"+ param_num);	
   		TypeMap.put(ctx.getChild(1).getText(), ctx.getChild(0).getText());
 
	}


	@Override public void enterFunc_decl(MicroParser.Func_declContext ctx) { 
		func_name = ctx.getChild(2).getText();
		String type = ctx.getChild(1).getText();
		TypeMap.put(func_name, type);
		IRListAdd("LABEL", getFunc(func_name));
		if (!func_name.contains("main")){
			fp_count++; //for LINK fp_count
			// System.out.println(fp_count);
		}
		IRListAdd("LANK"+func_name);
		String func_type = 	ctx.getChild(1).getText();

	}

	@Override public void exitFunc_decl(MicroParser.Func_declContext ctx) { 
		param_num = 1;
		var_num = 0;
		ValueMap.put("LANK"+func_name, Integer.toString(var_count+1));
		// System.out.println("LANK"+func_name);
		var_count = 0;
	}

	@Override public void exitFunc_body(MicroParser.Func_bodyContext ctx) { 
		if (link_enable){
			link_enable = false;
		}else{
			IRListAdd("UNLINK");
			IRListAdd("RET");
		}
	}

	@Override public void exitReturn_stmt(MicroParser.Return_stmtContext ctx) { 
	    link_enable = true;	
	    String opcode="ERR", dest;
	    String reg = update_ValueMap(Stack.pop());
	    String type = TypeMap.get(reg);
	    // IRListAdd("---SomethingWrong");
	    // System.out.println(reg + " " + type);
	    if (type == null) {
	    	IRListAdd("---SomethingWrong"); 
			IRListAdd("UNLINK");
			IRListAdd("RET");
	    	return;
	    }

	    if (type.contains("INT")){
	    	opcode = "STOREI";
	    } else if (type.contains("FLOAT")){
	    	opcode = "STOREF";
	    }  else if (type.contains("STRING")){
	    	opcode = "STORES";
	    }

	    if (func_name.contains("main")){ 
			dest = "$6"; //fixed pop register
	    } else {
	    	dest = "normal"; // tag it, change it to $num in exitprogra
	    }
	    IRListAdd(opcode, reg, dest);	 
		IRListAdd("UNLINK");
		IRListAdd("RET");
	}

	@Override public void enterCall_expr(MicroParser.Call_exprContext ctx) { 
		// reset call counter
		call_count = 0;
		// enable call
		call_enable = true;
	}


	@Override public void exitCall_expr(MicroParser.Call_exprContext ctx) {  //DAMN
		IRListAdd("PUSH");
		IRListAdd("PUSHREG");

		String func = ctx.getChild(0).getText();
		String tmp, tmp_updated;
		ArrayList<String> pop_list= new ArrayList<String> ();
		ArrayList<String> push_list = new ArrayList<String> ();


		// System.out.println(call_count);
		for (int i=0; i < call_count;i++) {
			pop_list.add(Call_Stack.pop());
		}

		Collections.reverse(pop_list);
		
		for (String item : pop_list) {
			if (item.contains("!T")){
				IRListAdd("PUSH", item);
			} else	{
				tmp_updated = ValueMap.get(item);
				// System.out.println(tmp + " " + tmp_updated);
				// IRListAdd(tmp);
				IRListAdd("PUSH", tmp_updated);				
			}	
		}

		IRListAdd("JSR", getFunc(func));		
	}

	private String getIfElseLabel(int else_or_end){
		switch(else_or_end){
			// case 1: return "IF_END_L" + label_count;
			// case 2: return "IF_ELSE_L" + label_count;
			case 1: return "" +label_count;
			case 2: return "" +label_count;
		}
		return "WRONG";
	}

	private String getFunc(String func){
		// return "FUNC_id_" + func + "_L";
		return func;
	}

	private void pop_callcount (){
		// pop according to counter
		for (int i=0;i<call_count - 1;i++ ){
       		IRListAdd("POP");
       	}

	}

	private void push (){
		
	}


	// single add
	private void IRListAdd (String opcode){
		IRNodeList.add(getNode(opcode,null,null,null));
	}
	// double add
	private void IRListAdd (String opcode, String dest){
		IRNodeList.add(getNode(opcode,null,null,dest));
	}
	// triple add
	private void IRListAdd (String opcode, String second, String dest ){
		IRNodeList.add(getNode(opcode,null,second,dest));
	}
	// quadra add
	private void IRListAdd (String opcode, String first, String second, String dest){
		IRNodeList.add(getNode(opcode,first,second,dest));
	}

	// single add
	private void TinyListAdd (String opcode){
		TinyNodeList.add(getNode(opcode,null,null,null));
	}
	// double add
	private void TinyListAdd (String opcode, String dest){
		TinyNodeList.add(getNode(opcode,null,null,dest));
	}
	// triple add
	private void TinyListAdd (String opcode, String second, String dest ){
		TinyNodeList.add(getNode(opcode,null,second,dest));
	}
	// quadra add
	private void TinyListAdd (String opcode, String first, String second, String dest){
		TinyNodeList.add(getNode(opcode,first,second,dest));
	}







	private boolean isLocal (String str){
		if(str == null){
			return false;
		}
		if (str.contains("$-")){
			return true;
		}
		return false;
	}
	private boolean isTemp (String str){
		if(str == null){
			return false;
		}
		if (str.matches("!T[0-9]*")){
			return true;
		}
		return false;
	}
	private boolean isGlobal (String str){
		if(str == null){
			return false;
		}
		if (str.matches("$[0-9]*")){
			return true;
		}
		return false;
	}
	private boolean isVariable (String str){
		if(str == null){
			return false;
		}
		if (str.contains("$")){
			return true;
		}
		return false;
	}
	private boolean isBoth (String str){
		if(str == null){
			return false;
		}
		if (isTemp(str)){
			return true;
		} else if (isVariable(str)){
			return true;
		} else{
			return false;
		}

	}


	@Override public void exitProgram(MicroParser.ProgramContext ctx) {
		i = 0;
		fp_count += 6;
		System.out.println(";IR code");
		for (Node IRNode: IRNodeList) {
			if(IRNode.getIRText().contains("LANK")){
				System.out.println(";LINK " + ValueMap.get(IRNode.getFuncName()));
				if(!print){
					getTinyCode(getNode("LINK", null, null, ValueMap.get(IRNode.getFuncName())));
				}
			} else if (IRNode.getIRText().contains("normal")){
				System.out.println(IRNode.getNeedUpdateText()+" $"+fp_count);
				if(!print){
					getTinyCode(getNode(IRNode.opcode, null,IRNode.second,"$"+fp_count));
				}
				// System.out.println("------"+" " + IRNode.opcode +" "+IRNode.second + " " +"$"+fp_count);
			} else{
				System.out.println(IRNode.getIRText());
				if(!print){
					getTinyCode(IRNode);
				}
			}
		}
		if(print){
			for (String str: PrintList){
				System.out.println(str);
			}

		}else{
			System.out.println(";tiny code");
			for (Node TinyNode: TinyNodeList){
				System.out.println(TinyNode.getTinyText());
			}
			System.out.println("sys halt");
		}
	}

	private int reg_modifier = 0;

	public void getTinyCode(Node IRNode ){
		String opcode = IRNode.opcode;
		String realcode = getCorrectOpcode(null,opcode);
		String first = IRNode.first;
		String second = IRNode.second;
		String dest = IRNode.dest;
		String reg, type;

		String first_reg = updateRegister(first);
		String second_reg = updateRegister(second);
		String dest_reg = updateRegister(dest);

		// System.out.println(opcode + " " + first + " " + second+ " " +dest);

		if(opcode.equals("WRITEI") || opcode.equals("WRITEF") || opcode.equals("WRITES")) {
			TinyListAdd(realcode, dest_reg);
		} else if(opcode.equals("READI") || opcode.equals("READF")){
			TinyListAdd(realcode, dest_reg);
		} else if(opcode == "ADDI" || opcode == "ADDF" || opcode == "SUBI" || opcode == "SUBF" || opcode == "MULTF" || opcode == "MULTI" || opcode == "DIVI" || opcode == "DIVF"){

			/*
				;SUBI $2 !T2 !T3  move $2 r13
				 				  subi r12 r13
				;MULTI $-2 $2 !T5 move r1 r15
			  					  muli $2 r15
		  	*/
			reg = "$n";
			if(isBoth(first) && isBoth(second)){
				TinyListAdd("move", first_reg, dest_reg);
				TinyListAdd(realcode, second_reg , dest_reg);
			}


		} else if(opcode.equals("STOREI") || opcode.equals("STOREF")) {
			if(isTemp(dest)){
				TinyListAdd(realcode, first_reg,second_reg, dest_reg);
			} else if (isVariable(dest)) {
				if(isLocal(dest)){
					TinyListAdd(realcode, first_reg,second_reg, dest_reg);
				}else{
					TinyListAdd(realcode,first_reg, second_reg, dest_reg);
				}

			} else {
				TinyListAdd("ERROR1");
			} 

		} else if (opcode.equals("JUMP") || opcode.equals("LABEL")) {
			TinyListAdd(realcode, dest);
		} else if (opcode.equals("GT") || opcode.equals("GE") || opcode.equals("LT") || opcode.equals("LE") || opcode.equals("NE") || opcode.equals("EQ")){


			if(isLocal(first) && isLocal(second)){
				reg = newReg();
				TinyListAdd("move",second_reg, reg);
				second_reg = reg;
			}
				

			TinyListAdd("cmpi",first_reg, second_reg);
			TinyListAdd(realcode, dest);

		}  else if ((opcode.equals("POP") && dest == null)|| (opcode.equals("PUSH") && dest == null) || opcode.contains("HALT") || opcode.contains("RET") || opcode.contains("UNLINK")){
			TinyListAdd(realcode);
		} else if (opcode.equals("POPREG")){
			TinyListAdd(realcode, "r3");
			TinyListAdd(realcode, "r2");
			TinyListAdd(realcode, "r1");
			TinyListAdd(realcode, "r0");
		} else if (opcode.equals("PUSHREG")){
			TinyListAdd(realcode, "r0");
			TinyListAdd(realcode, "r1");
			TinyListAdd(realcode, "r2");
			TinyListAdd(realcode, "r3");
		} else if (opcode.equals("POP") && dest != null) {
			TinyListAdd(realcode, dest_reg);
		} else if (opcode.equals("PUSH") && dest != null) {
			TinyListAdd(realcode, dest_reg);
		} else if (first == null && second == null && dest == null) {
			TinyListAdd(realcode);
		} else if (opcode.equals("JSR")){
			TinyListAdd(realcode, dest_reg);
		} else if (opcode.equals("LINK")){
			TinyListAdd(realcode, dest_reg);
		} 
	}

	private String updateRegister (String str){
		String reg;
		int num;
		if(isLocal(str)){
			num = Integer.valueOf(str.replace("$-", "")) - 1;
			reg = "r" + String.valueOf(num);
			return reg;
		}else if (isGlobal(str)){
			return str;
		}else if (isTemp(str)){
			num = Integer.valueOf(str.replace("!T", "")) + reg_modifier;
			reg_count = num;
			reg = "r" + String.valueOf(10 + num);
			return reg;
		}
		return str;
	}

	private String newReg(){
		String s = "r" + ++reg_count;
		reg_modifier++;
		return s;
	}

	private String getCmp (String item){
		if(item.contains("INT")){
			return "cmpi";
		}else if(item.contains("FLOAT")){
			return "cmpr";
		}else if(item.contains("String")){
			return "cmpSTRING";
		}
		return "ERROR";
	}

	private String getCorrectOpcode (String Int_or_Float, String opcode){
		switch(opcode) {
			case "+": return (Int_or_Float.contains("FLOAT")) ? "ADDF" : "ADDI";
			case "-": return (Int_or_Float.contains("FLOAT")) ? "SUBF" : "SUBI";
			case "/": return (Int_or_Float.contains("FLOAT")) ? "DIVF" : "DIVI";
			case "*": return (Int_or_Float.contains("FLOAT")) ? "MULTF" : "MULTI";
			case "ADDI": return "addi";
			case "ADDF": return "addr";
			case "SUBI": return "subi";
			case "SUBF": return "subr";
			case "READI": return "sys readi";
			case "READF": return "sys readr";
			case "MULTI": return "muli";
			case "MULTF": return "mulr";
			case "DIVI": return "divi";
			case "DIVF": return "divr";
			case "WRITEI": return "sys writei";
			case "WRITEF": return "sys writer";
			case "WRITES": return "sys writes";
			case ">": return "LE";
			case ">=": return "LT";
			case "<": return "GE";
			case "<=": return "GT";
			case "!=": return "EQ";
			case "=": return "NE";
			case "GT": return "jgt";
			case "GE": return "jge";
			case "LT": return "jlt";
			case "LE": return "jle";
			case "NE": return "jne";
			case "EQ": return "jeq";
			case "jgt": return "cmpi";
			case "jge": return "addi";
			case "jlt": return "subi";
			case "jle": return "cmpr";
			case "jne": return "muli";
			case "jeq": return "cmpi";
			case "JUMP": return "jmp";
			case "LABEL": return "label";
			case "UNLINK": return "unlnk";
			case "LINK": return "link";
			case "POP": return "pop";
			case "PUSH": return "push";
			case "RET": return "ret";
			case "PUSHREG": return "push";
			case "POPREG": return "pop";
			case "JSR": return "jsr";
			case "HALT": return "sys halt";
			case "STOREI": return "move";
			case "STOREF": return "move";
			case "STORES": return "move";
			default:  return opcode;
		}
	}

}