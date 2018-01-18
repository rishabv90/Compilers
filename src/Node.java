public class Node { 
	public String opcode, first, second, dest;
	public String Hello;
	public Node(String opcode, String first, String second, String dest){
		this.opcode = opcode;
		this.first = first;
		this.second = second;
		this.dest = dest;
	}
	public String getIRText() {
		Hello = ";" + this.opcode;
		if(this.first != null){
			Hello += " " + this.first;
		}
		if(this.second != null){
			Hello += " " + this.second;
		}
		if(this.dest != null){
			Hello += " " + this.dest;
		}
		return Hello;
	}
	public String getNeedUpdateText() {
		Hello = ";" + this.opcode;
		if(this.first != null){
			Hello += " " + this.first;
		}
		if(this.second != null){
			Hello += " " + this.second;
		}
		return Hello;
	}
	public String getFuncName() {
		return this.opcode;
	}

	public String getTinyText() {
		Hello = this.opcode;
		if(this.first != null){
			Hello += " " + this.first;
		}
		if(this.second != null){
			Hello += " " + this.second;
		}
		if(this.dest != null){
			Hello += " " + this.dest;
		}
		return Hello;
	}
	//Remove null in string
	public String removeNull (String item){
		if (item != null){
			if(item.contains(" ")){
				return item;
			}else{
				item = " " + item;
				return item;
			}
		}else{
			return "";
		}
	}
}

