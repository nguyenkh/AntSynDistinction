package common.wordnet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class WordNetReader {
	public static HashMap<String, Synset> readSynsets(String fileName) throws IOException{
		HashMap<String, Synset> data = new HashMap<String, Synset>();
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String line = reader.readLine();
		while (line != null) {
			if (!line.startsWith(" ")) {
				Synset synset = new Synset(line);
				data.put(synset.id, synset);
			}
			line = reader.readLine();
		}
		reader.close();
		return data;
	} 
	
	public static HashMap<String, ArrayList<String>> getWord2SynsetIds(HashMap<String, Synset> synsetMap) {
		HashMap<String, ArrayList<String>> word2SynsetIds = new HashMap<String, ArrayList<String>>();
		for (String id: synsetMap.keySet()) {
			Synset synset = synsetMap.get(id);
			for (String word: synset.words) {
				if (!word2SynsetIds.containsKey(word)) {
					word2SynsetIds.put(word, new ArrayList<String>());
				} 
				word2SynsetIds.get(word).add(id);
			}
		}
		return word2SynsetIds;
	}
	
	public static void main(String[] args) throws IOException{
		String adjFile = args[0];
		HashMap<String, Synset> synsetMap = readSynsets(adjFile);
		for (String id: synsetMap.keySet()) {
			Synset synset = synsetMap.get(id);
			System.out.print(synset.id);
			for (String word: synset.words) {
				System.out.print(" " + word);
			}
			System.out.println();
		}
		
		HashMap<String, ArrayList<String>> word2SynsetIds = getWord2SynsetIds(synsetMap);
		for (String word: word2SynsetIds.keySet()) {
		    System.out.print(word + ": ");
		    System.out.print(word2SynsetIds.get(word) + "\n");
		}
	}
}
