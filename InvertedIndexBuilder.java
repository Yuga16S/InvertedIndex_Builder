
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import external.TrieST;

/*
 * @author yugapriya
 */
public class InvertedIndexBuilder {
	
	private static final String WEBPAGES_DIR =  System.getProperty("user.dir") 
			+ File.separator + "resources" + File.separator + "webpages" + File.separator;
	
	private static final String TEXTFILES_DIR =  System.getProperty("user.dir") 
			+ File.separator + "resources" + File.separator + "textfiles" + File.separator;
	
	
	

	public static void main(String[] args) throws IOException {
		File webpagesDir = new File(WEBPAGES_DIR);
		File textfilesDir = new File(TEXTFILES_DIR);

		// Constructing Trie
		TrieST<Integer> trie = new TrieST<>();
		int uniqueWordIndex = 0;
		
		File[] htmlFiles = webpagesDir.listFiles();
		
		deleteFilesInDirectory(textfilesDir); // deleting old text files 
		
		for (File htmlFile : htmlFiles) {
			String textContent = null;
			try {
				// Parsing the HTML file and extracting the text from it.
				Document document = Jsoup.parse(htmlFile);
				textContent = document.body().text();
			} catch (IOException e) {
				System.out.println("Could not parse " + htmlFile.getName());
			}
			
			// regex to ignore all whitespace and special characters (-,.* etc)
			String[] words = textContent.split("(?:[^a-zA-Z]+|\\s+)");
			
			// converting all the words to lower case
			for (int i = 0; i < words.length; i++) {
				words[i] = words[i].toLowerCase();
			}
			String cleanTextContent = String.join(" ", words);
			
			String textfileName = htmlFile.getName().replace("html", "txt");
			FileWriter fileWriter = new FileWriter(TEXTFILES_DIR + textfileName);
			// writing into a text file as a single line having all the words separated by a single space
			fileWriter.write(cleanTextContent);
			fileWriter.close();
			
			for (String word : words) {
				if (! trie.contains(word)) {
					trie.put(word, uniqueWordIndex++);
				}
			}
			
		}
		
		
		// Constructing 2d Array
		File[] textfiles = textfilesDir.listFiles();
		
		@SuppressWarnings("unchecked") // we are initialzing with ArrayList below
		ArrayList<Integer>[][] occurrencesTable = new ArrayList[textfiles.length][uniqueWordIndex];  
		for (int i = 0; i < textfiles.length; i++) {
			for (int j = 0; j < uniqueWordIndex; j++) { // initializing the occurrences table with empty arraylist
				occurrencesTable[i][j] = new ArrayList<Integer>();
			}
		}
		
		for (int i = 0; i < textfiles.length; i++) { // i would be the row of the 2d array
			int textFileCursorPosition = 1; // assuming the first character in the file is at position 1 ( not zero based)
			
			File textfile = textfiles[i];
			
			Scanner scanner = new Scanner(textfile);
			String textfileContent = scanner.nextLine(); // As mentioned earlier, the text file will only have one line
			scanner.close();
			
			String[] words = textfileContent.split(" ");
			for (String word : words) {
				Integer wordIndex = trie.get(word); // column of the 2d array
				
				ArrayList<Integer> occurrencesList = occurrencesTable[i][wordIndex];
				occurrencesList.add(textFileCursorPosition);
				
				textFileCursorPosition += (word.length() + 1); // adding 1 to account for the whitespace separating words in the text file.
			}
		}
		
		
		// Testing code:
		
		String wordToSearch = "cdic";
		int wordIndex = trie.get(wordToSearch); // column of occurences table (2d array)
		for (int i = 0; i < textfiles.length; i++) {
			File textfile = textfiles[i];
			ArrayList<Integer> occurrencesList = occurrencesTable[i][wordIndex];
			if (! occurrencesList.isEmpty()) {
				System.out.println("The word \"" + wordToSearch + "\" was found in " + textfile.getName() + " text file at the following positions: " + occurrencesList.toString());
			}
		}
		
	}
	
	/**
	 * Deletes all the file in the directory
	 * @param directoryFile
	 */
	private static void deleteFilesInDirectory(File directoryFile) {
		if (!directoryFile.isDirectory()) {
			throw new RuntimeException("Not a directory: " + directoryFile.getName());
		}
		for (File fileInsideDirectory : directoryFile.listFiles()) {
			fileInsideDirectory.delete();
		}
	}

}
