package common.wordnet;

import java.util.ArrayList;

public class Synset {
	String id;
	String synsetType;
	String[] words;
	String antonymSSId;
	String[] simSSId;
	String[] hypoSSId;
	String[] hyperSSId;
	
	public Synset(String line) {
		String[] elements = line.split(" ");
		id = elements[0];
		synsetType = elements[2];
		readWords(elements);
	}
	
	public void readWords(String[] elements) {
		// read words
		
		int size = Integer.parseInt(elements[3],16);
		words = new String[size];
		for (int i = 0; i< size; i++) {
			words[i] = elements[4 + 2 * i].replaceAll("_", "-");
		}
		
		// read info
		int fieldNum = Integer.parseInt(elements[4 + size * 2],10);
		
		ArrayList<String> simList = new ArrayList<String>();
		ArrayList<String> hypoList = new ArrayList<String>();
		ArrayList<String> hyperList = new ArrayList<String>();
		for (int i = 0; i < fieldNum; i++) {
			String type = elements[4 + size * 2 + 1 + i * 4];
			String id = elements[4 + size * 2 + 2 + i * 4];
			String pos = elements[4 + size * 2 + 3 + i * 4];
			if (type.equals("&") || type.equals("^")) {
				if (pos.equals("a"))
					simList.add(id);
			} else if (type.equals("!")) {
				antonymSSId = id;
			} else if (type.equals("@")) {
			    hyperList.add(id);
            } else if (type.equals("~")) {
                hypoList.add(id);
            }
		}
		simSSId = new String[simList.size()];
		simSSId = simList.toArray(simSSId);
		hyperSSId = new String[simList.size()];
		hyperSSId = hyperList.toArray(hyperSSId);
        hypoSSId = new String[simList.size()];
        hypoSSId = hypoList.toArray(hypoSSId);
	}
 }
