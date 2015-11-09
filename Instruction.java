import java.util.HashMap;
import java.util.ArrayList;

public class Instruction {
	
	HashMap<String, Integer> tableOpa = new HashMap<String, Integer>();
	HashMap<String, Integer> tableOp = new HashMap<String, Integer>();
	HashMap<String, String> tableLength = new HashMap<String, String>();
	ArrayList<String> registers = new ArrayList<String>();
	
	public Instruction() {
		
		tableOp.put("ADDR", 0x90);
		tableOp.put("COMPR", 0xA0);
		tableOp.put("SUBR", 0x94);
		tableOp.put("MULR", 0x98);
		tableOp.put("DIVR", 0x9C);
		tableOp.put("ADD", 0x18);
		tableOp.put("SUB", 0x1C);
		tableOp.put("MUL", 0x20);
		tableOp.put("DIV", 0x24);
		tableOp.put("COMP", 0x28);
		tableOp.put("J", 0x3C);
		tableOp.put("JEQ", 0x30);
		tableOp.put("JGT", 0x34);
		tableOp.put("JLT", 0x38);
		tableOp.put("JSUB", 0x48);
		tableOp.put("LDCH", 0x50);
		tableOp.put("RSUB", 0x76);
		tableOp.put("TIX", 0x44);
		tableOp.put("TIXR", 0xB8);
		tableOp.put("RD", 0xD8); 
		tableOp.put("TD", 0xE0); 
		tableOp.put("WD", 0xDC); 
		tableOp.put("STCH", 0x54);
		tableOp.put("CLEAR", 0xB4);
		tableOp.put("LD", 0x00);
		tableOp.put("ST", 0x00);	
		
		tableLength.put("ADDR", "2");
		tableLength.put("COMPR", "2");
		tableLength.put("SUBR", "2");
		tableLength.put("MULR", "2");
		tableLength.put("DIVR", "2");
		tableLength.put("ADD", "3");
		tableLength.put("SUB", "3");
		tableLength.put("MUL", "3");
		tableLength.put("DIV", "3");
		tableLength.put("COMP", "3");
		tableLength.put("J", "3");
		tableLength.put("JEQ", "3");
		tableLength.put("JGT", "3");
		tableLength.put("JLT", "3");
		tableLength.put("JSUB", "3");
		tableLength.put("LDCH", "3");
		tableLength.put("RSUB", "3");
		tableLength.put("TIX", "3");
		tableLength.put("TIXR", "2");
		tableLength.put("RD", "3");
		tableLength.put("TD", "3");
		tableLength.put("WD", "3");
		tableLength.put("STCH", "3");
		tableLength.put("CLEAR", "2");
		tableLength.put("LD", "3");
		tableLength.put("ST", "3");
		
		registers.add("AX");
		registers.add("BX");
		registers.add("LX");
		registers.add("SX");
		registers.add("TX");
		registers.add("XX");
	}
}
