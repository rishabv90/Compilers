import org.antlr.v4.runtime.*;
import java.util.*;

public class TinyConverter {
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
	private String list;
	public TinyConverter(String input){
		this.list = input;

	}

}