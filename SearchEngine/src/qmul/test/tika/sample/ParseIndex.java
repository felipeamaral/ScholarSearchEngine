package qmul.test.tika.sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
 
public class ParseIndex {
 
	public static final String INDEX_DIRECTORY = "index/ramdisk/";
	public static int writeLimit = -1;
	
    public static void main(String[] args) throws IOException{
    	
    	File docs = new File("documents");
    	File indexDir = new File(INDEX_DIRECTORY);
    	
    	Directory dir = FSDirectory.open(indexDir);
    	

    	Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
    	IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);
    	IndexWriter writer  = new IndexWriter(dir, config);
    	writer.deleteAll();
    	
    	for(File file : docs.listFiles()){
    		Metadata metadata = new Metadata();
    		ContentHandler handler = new BodyContentHandler(writeLimit);
    		ParseContext context = new ParseContext();
    		Parser parser = new AutoDetectParser();
    		
    		InputStream inputStream = new FileInputStream(file);
    		
    		try{
    			parser.parse(inputStream, handler, metadata, context);
    		} catch(TikaException te){
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
    		
    		for(String key : metadata.names()){
    			String name = key.toLowerCase();
    			String values = metadata.get(key);
    			
    			System.out.println("name: " + name);
    			System.out.println("key       " + key);
    			System.out.println("values    " + values);
    			
    			if("keywords".equalsIgnoreCase(key)){
    				for(String keyword : values.split(",?(\\s+)")){
    					doc.add(new TextField(name, keyword, Store.YES));
    				}
    			} else if("title".equalsIgnoreCase(key)){
    				doc.add(new TextField(name, values, Store.YES));
    			} else if("author".equalsIgnoreCase(key)){
    				doc.add(new StringField(name, values, Store.YES));
    			}
    		}
    		
    		doc.add(new TextField("text", text, Store.NO));
    		writer.addDocument(doc);
    		
    		
    	}
    	
    	writer.commit();
    	writer.deleteUnusedFiles();
    	System.out.println(writer.maxDoc() + " documents written");
    }
}