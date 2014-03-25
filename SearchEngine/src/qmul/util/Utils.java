package qmul.util;

import java.util.Set;

import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.util.Version;

/**
 * @author Felipe Amaral, Mateus Gondim and Vanessa Gomes
 * 
 * Class to organize constants on the project
 * 
 *
 */
public class Utils {
	
	public static final String INDEX_DIRECTORY = "index/ramdisk/";
	public static int writeLimit = -1;
	public final static Version LUCENE_VERSION = Version.LUCENE_40;
	public static final String START_MESSAGE = "Welcome! Type your subject and we will search in our Computer Science articles.";
	
}
