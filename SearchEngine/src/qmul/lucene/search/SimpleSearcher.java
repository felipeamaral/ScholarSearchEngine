package qmul.lucene.search;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import qmul.util.Utils;

/**
 * @author Felipe Amaral, Mateus Gondim and Vanessa Gomes
 *
 *Define the methods used to execute a search with Lucene
 *
 *
 */
public class SimpleSearcher {
	
	private Ranking ranking;
	
	/**
	 * 
	 * Execute the search itself
	 * 
	 * @param indexDir - Indexes directory
	 * @param queryStr - Query to be searched
	 * @param maxHits  - Max of hits
	 * @throws IOException
	 * @throws ParseException
	 */
	private void searchIndex(File indexDir, String queryStr, int maxHits)
			throws IOException, ParseException {
		
		
		/* Create the directory and the index reader*/
		Directory directory = FSDirectory.open(indexDir);
		IndexReader reader = DirectoryReader.open(directory);

		/* Create the searcher object and set the similarity with the BM25 algorithm */
		IndexSearcher searcher = new IndexSearcher(reader);
		searcher.setSimilarity(new BM25Similarity());
		
		/* Create a query object */
		Query query = handleQuery(queryStr);
		
		/* Search with the given query */
		TopDocs topDocs = searcher.search(query, maxHits);

		/* Score the documents and rank them */
		ScoreDoc[] hits = topDocs.scoreDocs;
		for (int i = 0; i < hits.length; i++) {
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);
			ranking.addDocument(d);
		}

		System.out.println("Found " + hits.length);
	}

	/**
	 * Parse the query in multi fields. 
	 * The fields are, sorted by relevance: Title, Keywords, Text
	 * 
	 * @param queryStr - Query to be searched
	 * @return
	 * @throws ParseException
	 */
	private Query handleQuery(String queryStr) throws ParseException {
		
		/* Parser looks for terms in fields title, keywords and text */
		MultiFieldQueryParser parser = new MultiFieldQueryParser(
				Utils.LUCENE_VERSION, new String[] { "title", "keywords",
						"text" }, new StandardAnalyzer(Utils.LUCENE_VERSION));
		Query query = parser.parse(queryStr);
		return query;
	}

	
	
	/**
	 * 
	 * Execute the method searchIndex and return a rank with the results
	 * 
	 * @param userQuery - Query given by the user
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public String search(String userQuery) throws IOException, ParseException {
		this.ranking = new Ranking();

		File indexDir = new File(Utils.INDEX_DIRECTORY);
		int hits = 10;
		searchIndex(indexDir, userQuery, hits);
		return ranking.toString();
	}

}
