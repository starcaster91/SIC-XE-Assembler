import java.util.HashMap;

public class Directives {
	
	HashMap<String, String> dir = new HashMap<String,String>();
	
	public Directives() {
		
		dir.put("START", "0");
		dir.put("BYTE", "1");
		dir.put("WORD", "3");
		dir.put("RESB", "0"); //0 or 1?
		dir.put("RESW", "0");
		dir.put("END", "0");
		dir.put("BASE", "0");
		dir.put("NOBASE", "0");
	}
	
	public String getHash(String directive) {
		return dir.get(directive);
	}
}
