import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.io.IOException;
import java.util.ArrayList;

public class Assembler {
    
    public static LinkedHashMap<String, Integer> symTable = new LinkedHashMap<String, Integer>();
    public static ArrayList<String> lineList = new ArrayList<String>();
    public static ArrayList<String> objCode = new ArrayList<String>();
    private static String filepath = "/";
    public static String programName;
    public static String line;
    public static File file = new File("Assemble.lst");
    public static File objectFile = new File("Assemble.obj");
    public static FileWriter obj;
    public static FileWriter w;
    public static int[] locCount = new int[100];
    
    //************************************   PASS 1   **************************************************
    public static void read() throws IOException {
        BufferedReader buf;
        String hold;
        int locIncreaser = 0;
        int increase = 0;
        
        if (file.exists() == false) {
        	file.createNewFile();
        }
        
        if (objectFile.exists() == false) {
        	objectFile.createNewFile();
        }
        
        try{
            buf = new BufferedReader(new FileReader(filepath));
            ArrayList<String> code = new ArrayList<String>();
            Directives d = new Directives();
            Instruction i = new Instruction();
            String prevRes = null;
            String[] split;
            int firstWord;
            int firstLine = 0;
            
            
            //While the line read isn't null, loop through each word.
            while ((line = buf.readLine()) != null) {
            	lineList.add(String.format("%04X", locIncreaser) + "\t" + line);
            	firstWord = 0;
            	locCount[increase] = locIncreaser;
            	increase++;
            	
            	//split all white spaces and save the word into the variable word.
                for (String word: line.trim().split("\\s+")) {
                	
                	if (firstLine == 0) {
                		firstLine++;
                		programName = word;
                		break;
                	}else{
                		/*If the first character of word is '+' then split that from the rest of the string
                		 * and save it to plusHold[1]. Check if plusHold[1] is a directive or an instruction.
                		 * Increase locIncreaser by an additional byte due to the + for extended mode.
                		 */
                		if (word.indexOf('+') == 0) {
                            String[] plusHold = word.split("\\+");
                            
                            if (d.dir.containsKey(plusHold[1])) {
                                hold = d.dir.get(plusHold[1]);
                                locIncreaser += Integer.parseInt(hold) + 1;
                                firstWord++;
                                continue;
                            }else if (i.tableLength.containsKey(plusHold[1])) {
                                hold = i.tableLength.get(plusHold[1]);
                                locIncreaser += Integer.parseInt(hold) + 1;
                                firstWord++;
                                continue;
                            }
                            //Check for certain characters in the current word. If it contains any then skip this word.
                        }else if (word.indexOf('#') == 0 || word.contains(",") || word.contains("@")|| i.registers.contains(word)) {
                        	firstWord++;
                            continue;
                        }else if (word.contains("'") ) {
                        	if (prevRes.equals("BYTE")) {
                        		split = word.split("\\'");
                        		
                        		if (split[0].equals("C")) {
                        			locIncreaser += split[1].length();
                            		continue;
                        		}else if (split[0].equals("X")) {
                        			locIncreaser += split[1].length() / 2;
                        			continue;
                        		}
                        	}else{
                        		continue;
                        	}
                        }else if (word.matches("[0-9]+")) {
                        	if (prevRes.equals("RESW")){
                          		int num = Integer.parseInt(word) * 3;
                          		locIncreaser += num;
                          		prevRes = "";
                        	}else{
                        		int number = Integer.parseInt(word);
                      			locIncreaser += number;
                        	}
                        }else if (d.dir.containsKey(word)) {
                        	if (word.equals("RESW")) {
                        		prevRes = word;
                        	}else if (word.equals("BYTE")) {
                        		prevRes = word;
                        	}
                            hold = d.dir.get(word); 
                            firstWord++;
                            continue;
                        }else if (i.tableLength.containsKey(word)) {
                            hold = i.tableLength.get(word);
                            locIncreaser += Integer.parseInt(hold);
                            firstWord++;
                            continue;
                        }else{
                            if (!code.contains(word) && firstWord == 0) { //if it already contains the symbol then don't add it again.
                                code.add(word);
                                symTable.put(word, locIncreaser);
                                firstWord++;
                                continue;
                            }
                        }
                    }
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    //************************************   PASS 2   **************************************************
    public static void pass() {
    	BufferedReader buf;
    	String hold;
    	
    	try{
    		buf = new BufferedReader(new FileReader(filepath));
    		Directives d = new Directives();
    		Instruction i = new Instruction();
    		String symHold;
    		String prevWord = null;
    		String instruction = null;
    		String h = null;
    		String[] hashSplit;
    		String[] atSplit;
    		String[] pSplit = null;
    		String[] apSplit = null;
    		Integer loc = 0;
    		Integer ni = 0x00;
    		int t = 0;
    		int clearCheck;
    		int plusCheck;
    		Integer sub = 0;
    		int last = 0;
    		int xbpe = 0;
    		int firstWord;
    		int firstLine= 0;
    		int increase = 0;
    		int baseHold;
    		String[] wordSeparate;
    		
    		while ((line = buf.readLine()) != null) {
    			increase++; 
             		firstWord = 0;
             		clearCheck = 0;
             		plusCheck = 0;	
             		t = 0;
             		baseHold = 0;
             	for (String word: line.trim().split("\\s+")) {
               		if (firstLine == 0) {
                		firstLine++;
                		break;
                	}else{
                		if (word.equals("RSUB")){
                			objCode.add("4F0000");
                		}
                		if (symTable.containsKey(word)) {
                			if (baseHold == 1) {
                    			sub = symTable.get(word);
                			}
                			if (plusCheck == 1) {
                				last = symTable.get(word);
                				objCode.add(String.format("%02X", ni) + String.format("%01X", xbpe) + String.format("%03X", last));
                			}else if (t == 1){
                				ni += 0x03;
                				xbpe = 0x02;
                				last = symTable.get(word) - locCount[increase];
            					objCode.add(String.format("%02X", ni) + String.format("%01X", xbpe) + String.format("%03X", last));
                			}else{
                				symHold = word;
                				continue;
                			}
                		}else if (word.indexOf('+') == 0) {
                            		String[] plusHold = word.split("\\+"); //separate the plus sign and the word into an array
                            		plusCheck = 1;
                            
                            if (d.dir.containsKey(plusHold[1])) {
                            	System.out.println("Syntax error.");
                                firstWord++;
                                continue;
                            }else if (i.tableOp.containsKey(plusHold[1])) {
                            	if (plusHold[1].equals("LD") || plusHold[1].equals("ST")) {
                            		instruction = plusHold[1];
                            		continue;
                            	}else{
                            		ni = i.tableOp.get(plusHold[1]) + 0x03;
                            		xbpe = 0x01;
                            		last = loc - locCount[increase];
                            		firstWord++;
                            		continue;
                            	}
                            }
                		}else if (word.equals("BASE")) {	
                			objCode.add("------");
                			baseHold = 1;
                			continue;
                		}else if (word.equals("NOBASE")) {
                			objCode.add("");
                			continue;
                		}else if (word.contains(",")) {
                			int check = 0;
                			
                			if (word.contains("#")) {
                				pSplit = word.split("#");
                				check = 1;
                			}
                				wordSeparate = word.split("\\,");
                				
                				if (i.registers.contains(wordSeparate[0]) && i.registers.contains(wordSeparate[1])) {
                					
                					if (wordSeparate[0].equals("AX")) {
                						h = "0";
                					}else if (wordSeparate[1].equals("XX")) {
                						h = "1";
                					}else if (wordSeparate[1].equals("LX")) {
                						h = "2";
                					}else if (wordSeparate[1].equals("BX")) {
                						h = "3";
                					}else if (wordSeparate[1].equals("SX")) {
                						h = "4";
                					}else if (wordSeparate[1].equals("TX")) {
                						h = "5";
                					}else if (wordSeparate[1].equals("FX")) {
                						h = "6";
                					}
                					
                					if (wordSeparate[1].equals("AX")) {
                						objCode.add(String.format("%02X", ni) + h + "0");
                					}else if (wordSeparate[1].equals("XX")) {
                						objCode.add(String.format("%02X", ni) + h + "1");
                					}else if (wordSeparate[1].equals("LX")) {
                						objCode.add(String.format("%02X", ni) + h + "2");
                					}else if (wordSeparate[1].equals("BX")) {
                						objCode.add(String.format("%02X", ni) + h + "3");
                					}else if (wordSeparate[1].equals("SX")) {
                						objCode.add(String.format("%02X", ni) + h + "4");
                					}else if (wordSeparate[1].equals("TX")) {
                						objCode.add(String.format("%02X", ni) + h + "5");
                					}else if (wordSeparate[1].equals("FX")) {
                						objCode.add(String.format("%02X", ni) + h + "6");
                					}
                						continue;
                				}else if (instruction.equals("STCH") || instruction.equals("LDCH")) {
                					Integer b = loc - locCount[increase - 1];
                					
                					if (b > 2047 || b < -2048) {
                						ni += 0x03;
                						xbpe = 0x0C;
                						last = symTable.get(wordSeparate[0]) - sub;
                						objCode.add(String.format("%02X", ni) + String.format("%01X", xbpe) + String.format("%03X", last));
                						continue;	
                					}else if (t == 1) {
                						ni = i.tableOp.get(instruction) + 0x03;
                						xbpe = 0x0A;
                					}
                					objCode.add(String.format("%02X", ni) + String.format("%01X", xbpe) + String.format("%03X", last));
                				}else if (instruction.equals("LD")) {
                					
                					if (wordSeparate[0].equals("LX")) {
                						if (check == 1) {
                							ni = 0x08 + 0x01;
                							xbpe = 0x01;
                						}else{
                							ni = 0x08 + 0x03;
                							xbpe = 0x02;
                						}
                						last = loc - locCount[increase];
                						objCode.add(String.format("%02X", ni) + String.format("%01X", xbpe) + String.format("%03X", last));
                						continue;
                					}else if (wordSeparate[0].equals("AX")) {
                						loc = symTable.get(wordSeparate[1]);
                						if (check == 1) {
                							if (pSplit[1].matches("[0-9]+")) {
                								ni = 0x00 + 0x01;
                								xbpe = 0x00;
                								last = Integer.parseInt(pSplit[1]);
                								objCode.add(String.format("%02X", ni) + String.format("%01X", xbpe) + String.format("%03X", last));
                								continue;
                							}else{
                								ni = i.tableOp.get(instruction) + 0x01;
                								xbpe = 0x01;
                							}
                						}else{
                							ni = i.tableOp.get(instruction) + 0x03;
                							xbpe = 0x02;
                						}
                						last = loc - locCount[increase];
                						objCode.add(String.format("%02X", ni) + String.format("%01X", xbpe) + String.format("%03X", last));
                						continue;
                					}else if (wordSeparate[0].equals("BX")) {
                						loc = symTable.get(pSplit[1]);
                						
                						if (check == 1) {
                							ni = 0x68 + 0x01;
                							xbpe = 0x02;
                						}else{
                							ni = 0x68 + 0x03;
                							xbpe = 0x02;
                						}
                						last = loc - locCount[increase];
                						objCode.add(String.format("%02X", ni) + String.format("%01X", xbpe) + String.format("%03X", last));
                						continue;
                					}else if (wordSeparate[0].equals("SX")) {
                						if (check == 1) {
                							ni = 0x6C + 0x01;
                							xbpe = 1;
                						}else{
                							ni = 0x6C + 0x03;
                							xbpe = 0x02;
                						}
                						last = loc - locCount[increase];
                						objCode.add(String.format("%02X", ni) + String.format("%01X", xbpe) + String.format("%03X", last));
                						continue;
                					}else if (wordSeparate[0].equals("TX")) {
                						Integer b = loc - locCount[increase - 1];
                						
                						if (check == 1) {
                							ni = 0x74 + 0x01;
                							xbpe = 0x10;
                						}else if (b > 2047 || b < -2048) {
                        					ni = 0x74 + 0x03;
                        					xbpe = 0x04;
                        					last = symTable.get(wordSeparate[1]) - sub;
                        					objCode.add(String.format("%02X", ni) + String.format("%01X", xbpe) + String.format("%03X", last));
                       						continue;	
                						}else{
                							ni = 0x74 + 0x03;
                							xbpe = 0x02;
                						}
                						last = Integer.parseInt(pSplit[1]);
                						objCode.add(String.format("%02X", ni) + String.format("%01X", xbpe) + String.format("%03X", last));
                						continue;
                					}else if (wordSeparate[0].equals("XX")) {
                						if (check == 1) {
                							ni = 0x04 + 0x01;
                							xbpe = 0x01;
                						}else{
                							ni = 0x04 + 0x03;
                							xbpe = 0x08;
                						}
                						last = loc - locCount[increase];
                						objCode.add(String.format("%02X", ni) + String.format("%01X", xbpe) + String.format("%03X", last));
                						continue;
                					}
                				}else if (instruction.equals("ST")) {
                					loc = symTable.get(wordSeparate[0]);
                				
                					if (wordSeparate[1].equals("LX")) {
                						if (check == 1) {
                							ni = 0x14 + 0x01;
                							xbpe = 0x01;
                						}else{
                							ni = 0x14 + 0x03;
                							xbpe = 0x02;
                						}
                						last = loc - locCount[increase];
                						objCode.add(String.format("%02X", ni) + String.format("%01X", xbpe) + String.format("%03X", last));
                						continue;
                					}else if (wordSeparate[1].equals("AX")) {
                						if (check == 1) {
                							ni = 0x0C + 0x01;
                							xbpe = 0x01;
                						}else{
                							ni = 0x0C + 0x03;
                							xbpe = 0x02;
                						}
                						last = loc - locCount[increase];
                						objCode.add(String.format("%02X", ni) + String.format("%01X", xbpe) + String.format("%03X", last));
                						continue;
                					}else if (wordSeparate[1].equals("BX")) {
                						if (check == 1) {
                							ni = 0x78 + 0x01;
                							xbpe = 0x01;
                						}else{
                							ni = 0x78 + 0x03;
                							xbpe = 0x02;
                						}
                						last = loc - locCount[increase];
                						objCode.add(String.format("%02X", ni) + String.format("%01X", xbpe) + String.format("%03X", last));
                						continue;
                					}else if (wordSeparate[1].equals("SX")) {
                						if (check == 1) {
                							ni = 0x7C + 0x01;
                							xbpe = 0x01;
                						}else{
                							ni = 0x7C + 0x03;
                							xbpe = 0x02;
                						}
                						last = loc - locCount[increase];
                						objCode.add(String.format("%02X", ni) + String.format("%01X", xbpe) + String.format("%03X", last));
                						continue;
                					}else if (wordSeparate[1].equals("TX")) {
                						if (check == 1) {
                							ni = 0x84 + 0x01;
                							xbpe = 0x01;
                						}else{
                							ni = 0x84 + 0x03;
                							xbpe = 0x02;
                						}
                						last = loc - locCount[increase];
                						objCode.add(String.format("%02X", ni) + String.format("%01X", xbpe) + String.format("%03X", last));
                						continue;
                					}else if (wordSeparate[1].equals("XX")) {
                						Integer b = loc - locCount[increase - 1];
                						if (check == 1) {
                							ni = 0x10 + 0x01;
                							xbpe = 0x01;
                						}else if (b > 2047 || b < -2048) {
                							ni = 0x10 + 0x03;
                        					xbpe = 0x04;
                        					last = symTable.get(wordSeparate[0]) - sub;
                        					objCode.add(String.format("%02X", ni) + String.format("%01X", xbpe) + String.format("%03X", last));
                       						continue;
                						}else{
                							ni = 0x10 + 0x03;
                							xbpe = 0x08;
                						}
                							last = loc - locCount[increase];
                							objCode.add(String.format("%02X", ni) + String.format("%01X", xbpe) + String.format("%03X", last));
                							continue;
                					}	
                				}
                			}
                		} if (word.indexOf('#') == 0) {
                			
                			//split the # from the word.
                			hashSplit = word.split("\\#");
                			ni = i.tableOp.get(instruction) + 0x01;
                			xbpe = 0x00;
                			if (hashSplit[1].matches("[0-9]+")) {
                				last = Integer.parseInt(hashSplit[1]);
                				objCode.add(String.format("%02X", ni) + String.format("%01X", xbpe) + String.format("%03X", last));
                				continue;
                			}
                			continue;
                		}else if (word.indexOf('@') == 0) {
                			atSplit = word.split("\\@");  
                			loc = symTable.get(atSplit[1]);
                			if (plusCheck == 1) {
                				ni += 0x02;
                				xbpe = 0x01;
                			}else{
                				ni += 0x02;
                				xbpe = 0x02;
                			}
                			last = loc - locCount[increase];
                			objCode.add(String.format("%02X", ni) + String.format("%01X", xbpe) + String.format("%03X", last));
                			continue;
                		}else if (word.contains("'")) {
                			apSplit = word.split("\\'");
                    		
                    		if (apSplit[0].equals("C")) {
                    			StringBuilder sb = new StringBuilder();
                    			for (int k = 0; k < apSplit[1].length(); k++) {
                    				sb.append(Integer.toHexString((char)apSplit[1].charAt(k)));
                    			}
                    			objCode.add(sb.toString().toUpperCase());
                    		}else if (apSplit[0].equals("X")) {
                    			objCode.add(apSplit[1]);
                    		}
                		}else if (word.matches("[0-9]+")) {
                			last = Integer.parseInt(word);
                			if (word.equals("RESB") || word.equals("RESW")) {
                				objCode.add("------");
                			}
                			continue;
                		}else if (d.dir.containsKey(word)){
                			if (word.equals("RESB") || word.equals("RESW")) {
                				hold = word;
                				objCode.add("------");
                			}else{
                				continue;
                			}
                		}else if (word.equals("CLEAR")) {
                			clearCheck = 1;
                			ni = 0xB4;
                			continue;
                		}else if (clearCheck == 1) {
                			if (word.equals("AX")) {
                				objCode.add(String.format("%02X", ni) + "00");
                			}else if (word.equals("XX")) {
                				objCode.add(String.format("%02X", ni) + "10");
                			}else if (word.equals("LX")) {
                				objCode.add(String.format("%02X", ni) + "20");
                			}else if (word.equals("BX")) {
                				objCode.add(String.format("%02X", ni) + "30");
                			}else if (word.equals("SX")) {
                				objCode.add(String.format("%02X", ni) + "40");
                			}else if (word.equals("TX")) {
                				objCode.add(String.format("%02X", ni) + "50");
                			}else if (word.equals("FX")) {
                				objCode.add(String.format("%02X", ni) + "60");
                			}
                		}else if (i.registers.contains(word)) {
                			ni = i.tableOp.get(instruction);
                			
            				if (word.equals("AX")) {
            					objCode.add(String.format("%02X", ni) + "00");
                			}else if (word.equals("XX")) {
                				objCode.add(String.format("%02X", ni) + "10");
                			}else if (word.equals("LX")) {
                				objCode.add(String.format("%02X", ni) + "20");
                			}else if (word.equals("BX")) {
                				objCode.add(String.format("%02X", ni) + "30");
                			}else if (word.equals("SX")) {
                				objCode.add(String.format("%02X", ni) + "40");
                			}else if (word.equals("TX")) {
                				objCode.add(String.format("%02X", ni) + "50");
                			}else if (word.equals("FX")) {
                				objCode.add(String.format("%02X", ni) + "60");
                			}
                		}else if (i.tableOp.containsKey(word)) {
                			if (word.equals("ST") || word.equals("LD")) {
                				instruction = word;
                				continue;
                			}else{
                				t = 1;
                				ni = i.tableOp.get(word);
                				instruction = word;
                				prevWord = word;
                				continue;
                			}
                		}
                	}
             	}
    		objCode.add("------");

    		for (int p = 0; p < lineList.size(); p++) {
    			if (firstLine == 1) {
    				w.write(lineList.get(p) + "\n");
    				w.flush();
    				firstLine++;
    			}else{
    				obj.write(programName);
    				w.write(String.format("%-35.50s %-30.80s", lineList.get(p) , objCode.get(p - 1)) + "\n");
    				w.flush();
    				obj.flush();
    			}
    		}
    		
    		}catch (IOException e) {
    			e.printStackTrace();
    	}
    }

    public static void main(String[] args) throws Exception {
    
        //Takes argument and appends it to "filepath"
        if (args.length == 0) {
           System.out.println("Need an argument.");
           System.exit(0);
        }else{
            StringBuilder b = new StringBuilder(filepath);
            b.append(args[0]);
            filepath = b.toString();
            read();//Pass 1
        }

    	w = new FileWriter("Assemble.lst");	//Make new .lst file
    	obj = new FileWriter("Assemble.obj");   //Make new .obj file
        pass();	//Pass 2
    }
}
