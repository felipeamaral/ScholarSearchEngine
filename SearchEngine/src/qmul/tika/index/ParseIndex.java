package qmul.tika.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.LMSimilarity.CollectionModel;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.CollectionUtil;
import org.apache.lucene.util.Version;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import qmul.util.Utils;

public class ParseIndex {

	
	private static Set stopWords = StopAnalyzer.ENGLISH_STOP_WORDS_SET;

	
	public static void main(String[] args) throws IOException {
		long t1 = System.currentTimeMillis();
		File docs = new File("documents");
		File indexDir = new File(Utils.INDEX_DIRECTORY);

		Directory dir = FSDirectory.open(indexDir);

		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40, (CharArraySet) stopWords);
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40,
				analyzer);
		IndexWriter writer = new IndexWriter(dir, config);
		writer.deleteAll();

		for (File file : docs.listFiles()) {
			Metadata metadata = new Metadata();
			ContentHandler handler = new BodyContentHandler(Utils.writeLimit);
			ParseContext context = new ParseContext();
			Parser parser = new PDFParser();

			InputStream inputStream = new FileInputStream(file);

			try {
				parser.parse(inputStream, handler, metadata, context);
			} catch (TikaException te) {
				te.printStackTrace();
			} catch (SAXException se) {
				se.printStackTrace();
			} finally {
				inputStream.close();
			}

			String text = handler.toString();
			String fileName = file.getName();

			Document doc = new Document();
			doc.add(new StringField("file", fileName, Store.YES));

			System.out.println("Filename:    " + fileName);
			String values = metadata.get("Keywords");
			if (values != null) {
				System.out.println("Keywords values:    " + values);
				for (String keyword : values.split(",?(\\s+)")) {
					doc.add(new TextField("keywords", keyword, Store.YES));
				}
			}

			values = metadata.get("title");
			if (values != null) {
				System.out.println(" Title value:    " + values);
				doc.add(new TextField("title", values, Store.YES));
			}

			values = metadata.get("Author");
			if (values != null) {
				System.out.println("Author value:    " + values);
				doc.add(new StringField("author", values, Store.YES));
			}

			doc.add(new TextField("text", text, Store.NO));
			writer.addDocument(doc);
			System.out.println();

		}

		writer.commit();
		writer.deleteUnusedFiles();
		long t2 = System.currentTimeMillis();

		System.out.println(writer.maxDoc() + " documents written");
		System.out.println("Time consumed(ms): " + (t2 - t1));
	}
}