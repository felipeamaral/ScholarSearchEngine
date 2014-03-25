package qmul.tika.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
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

/**
 * This class is responsible for parsing the files and creating the Index.
 */
public class ParseIndex {
	private static Set stopWords = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
	private long time = System.currentTimeMillis();

	public ParseIndex() throws IOException {
		// Open the directory.
		Directory dir = openDirectory();

		// Initializing StandardAnalyzer.
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40,
				(CharArraySet) stopWords);

		// Initializing IndexWriter.
		IndexWriter writer = handleIndexWriter(dir, analyzer);
		handleFiles(writer);
		writer.commit();
		writer.deleteUnusedFiles();
		long t2 = System.currentTimeMillis();

		System.out.println(writer.maxDoc() + " documents written");
		System.out.println("Time consumed(ms): " + (t2 - time));
	}

	/**
	 * Opens the Index directory.
	 * 
	 * @return
	 * @throws IOException
	 */
	private Directory openDirectory() throws IOException {
		File indexDir = new File(Utils.INDEX_DIRECTORY);
		Directory dir = FSDirectory.open(indexDir);
		return dir;
	}

	/**
	 * Creates the index writer and set its similarity function as the
	 * BM25Similarity.
	 * 
	 * @param dir
	 * @param analyzer
	 * @return
	 * @throws IOException
	 */
	private IndexWriter handleIndexWriter(Directory dir, Analyzer analyzer)
			throws IOException {
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40,
				analyzer);

		// Setting BM25Similarity.
		config.setSimilarity(new BM25Similarity());

		IndexWriter writer = new IndexWriter(dir, config);
		writer.deleteAll();
		return writer;
	}

	/**
	 * Gets the file and works with TIKA API to parse the PDF file to a
	 * document-like format.
	 * 
	 * @param file
	 * @param metadata
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private ContentHandler parseFileToPDF(File file, Metadata metadata)
			throws FileNotFoundException, IOException {
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
		return handler;
	}

	/**
	 * Parses the files to documents and adds them to the IndexWriter.
	 * 
	 * @param writer
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void handleFiles(IndexWriter writer) throws FileNotFoundException,
			IOException {
		File docs = new File("documents");
		// For each file in the list, we add a new document.
		for (File file : docs.listFiles()) {
			Metadata metadata = new Metadata();
			ContentHandler handler = parseFileToPDF(file, metadata);

			Document doc = createDocument(file, metadata, handler);
			writer.addDocument(doc);
		}
	}

	/**
	 * Creates a Lucene Document from a File.
	 * 
	 * @param file
	 * @param metadata
	 * @param handler
	 * @return
	 */
	private Document createDocument(File file, Metadata metadata,
			ContentHandler handler) {
		String text = handler.toString();
		String fileName = file.getName();

		Document doc = new Document();

		// Create field *file*.
		doc.add(new StringField("file", fileName, Store.YES));

		String values = metadata.get("Keywords");
		createFieldKeywords(doc, values);

		values = metadata.get("title");
		createFieldTitle(doc, values);

		values = metadata.get("Author");
		createFieldAuthor(doc, values);

		doc.add(new TextField("text", text, Store.NO));
		return doc;
	}

	/**
	 * Takes values of file and Document and creates the field Author (if the
	 * field) is available after parsing the file.
	 * 
	 * @param doc
	 * @param values
	 */
	private void createFieldAuthor(Document doc, String values) {
		if (values != null) {
			doc.add(new StringField("author", values, Store.YES));
		}
	}

	/**
	 * Takes values of file and Document and creates the field Title (if the
	 * field) is available after parsing the file.
	 * 
	 * @param doc
	 * @param values
	 */
	private void createFieldTitle(Document doc, String values) {
		if (values != null) {
			TextField field = new TextField("title", values, Store.YES);

			// Boosting this field with more than the normal importance.
			field.setBoost(5);
			doc.add(field);
		}
	}

	/**
	 * Takes values of file and Document and creates the field Keywords (if the
	 * field) is available after parsing the file.
	 * 
	 * @param doc
	 * @param values
	 */
	private void createFieldKeywords(Document doc, String values) {
		if (values != null) {
			for (String keyword : values.split(",?(\\s+)")) {
				TextField field = new TextField("keywords", keyword, Store.YES);

				// Boosting this field with more than the normal importance.
				field.setBoost(5);
				doc.add(field);
			}
		}
	}

	public static void main(String[] args) {
		// Create a new index.
		System.out.println("Creating a new Index.");
		try {
			new ParseIndex();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}
}