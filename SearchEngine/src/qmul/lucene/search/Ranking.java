package qmul.lucene.search;

import java.util.ArrayList;

import org.apache.lucene.document.Document;

/**
 * @author Felipe Amaral, Mateus Gondim and Vanessa Gomes
 * 
 * Used to print the results on the class UserInterface
 *
 */
public class Ranking {
	
	private ArrayList<Document> ranking;
	
	public Ranking() {
		ranking = new ArrayList<Document>();
	}
	
	/**
	 * Add a document to the end of the list.
	 * @param doc
	 */
	public void addDocument(Document doc){
		ranking.add(doc);
	}
	
	/**
	 * Gets the ranking.
	 * @return the ranking
	 */
	public ArrayList<Document> getRanking() {
		return ranking;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String rankingText = "";
		
		for (Document doc : ranking) {
			rankingText = rankingText + doc.get("file");
			rankingText = rankingText + "\nTitle: " + doc.get("title");
			rankingText = rankingText + "\n\n";
		}
		return rankingText;
	}
}
