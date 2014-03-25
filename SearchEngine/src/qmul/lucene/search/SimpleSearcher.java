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

public class SimpleSearcher {
	private Ranking ranking;

	private void searchIndex(File indexDir, String queryStr, int maxHits)
			throws IOException, ParseException {
		// Creating the Directory and Reader.
		Directory directory = FSDirectory.open(indexDir);
		IndexReader reader = DirectoryReader.open(directory);

		IndexSearcher searcher = new IndexSearcher(reader);
		searcher.setSimilarity(new BM25Similarity());

		Query query = handleQuery(queryStr);
		// Searching in the index with the query.
		TopDocs topDocs = searcher.search(query, maxHits);

		ScoreDoc[] hits = topDocs.scoreDocs;
		for (int i = 0; i < hits.length; i++) {
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);
			// System.out.println(d.get("file"));
			ranking.addDocument(d);
		}

		System.out.println("Found " + hits.length);
	}

	/**
	 * @param queryStr
	 * @return
	 * @throws ParseException
	 */
	private Query handleQuery(String queryStr) throws ParseException {
		// Parser looks for terms in fields title, keywords and text
		MultiFieldQueryParser parser = new MultiFieldQueryParser(
				Utils.LUCENE_VERSION, new String[] { "title", "keywords",
						"text" }, new StandardAnalyzer(Utils.LUCENE_VERSION));
		Query query = parser.parse(queryStr);
		return query;
	}

	public String search(String userQuery) throws IOException, ParseException {
		this.ranking = new Ranking();

		File indexDir = new File("index/ramdisk");
		int hits = 10;
		searchIndex(indexDir, userQuery, hits);

		return ranking.toString();
	}

}
