import java.io.File;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class SimpleSearcher {
	public final static Version LUCENE_VERSION = Version.LUCENE_40;

	private void searchIndex(File indexDir, String queryStr, int maxHits)
			throws Exception {
		// Creating the Directory and Reader.
		Directory directory = FSDirectory.open(indexDir);
		IndexReader reader = DirectoryReader.open(directory);

		IndexSearcher searcher = new IndexSearcher(reader);
		
		Query query = handleQuery(queryStr);
		
		//Searching in the index with the query.
		TopDocs topDocs = searcher.search(query, maxHits);

		ScoreDoc[] hits = topDocs.scoreDocs;
		for (int i = 0; i < hits.length; i++) {
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);
			System.out.println(d.get("file"));
		}

		System.out.println("Found " + hits.length);

	}

	/**
	 * @param queryStr
	 * @return
	 * @throws ParseException
	 */
	private Query handleQuery(String queryStr) throws ParseException {
		// Parser receives Version and FieldName to look for.
		QueryParser parser = new QueryParser(LUCENE_VERSION, "title",
				new StandardAnalyzer(LUCENE_VERSION));
		Query query = parser.parse(queryStr);
		return query;
	}

	public static void main(String[] args) throws Exception {

		File indexDir = new File(
				"index/ramdisk");
		String query = "video games";
		int hits = 100;

		SimpleSearcher searcher = new SimpleSearcher();
		searcher.searchIndex(indexDir, query, hits);

	}

}
