import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import qmul.test.tika.sample.ParseIndex;


public class Main {

	public static void main(String[] args) throws IOException {
		File indexDir = new File(ParseIndex.INDEX_DIRECTORY);
		
		Directory index = FSDirectory.open(indexDir);
	
		QueryParser parser = new QueryParser(Version.LUCENE_40, "contents", new StandardAnalyzer(Version.LUCENE_40));
		Query query;
		try {
			query = parser.parse("a1-sachdeva");
		} catch (org.apache.lucene.queryparser.classic.ParseException e) {
			e.printStackTrace();
			return;
		}
		
		int hitsPerPage = 10;
		IndexReader reader = IndexReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
		searcher.search(query, collector);

		System.out.println("total hits: " + collector.getTotalHits());		

		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		for (ScoreDoc hit : hits) {
			Document doc = reader.document(hit.doc);
			System.out.println(doc.get("file") + "  (" + hit.score + ")");
		}
	}

}
