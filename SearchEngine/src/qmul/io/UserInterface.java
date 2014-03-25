package qmul.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.lucene.queryparser.classic.ParseException;

import qmul.lucene.search.SimpleSearcher;
import qmul.util.Utils;

/**
 * @author Felipe Amaral, Mateus Gondim and Vanessa Gomes
 *
 * Project Interface
 *
 */
public class UserInterface {
	
	public static void main(String[] args) throws IOException, ParseException {
		System.out.println(Utils.START_MESSAGE);
		while (true) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					System.in));
			System.out
					.println("\nType your subject and press Enter/Key to search. Type \"Quit\" to exit.");
			String userQuery = reader.readLine();

				System.out.println("Results:");

				SimpleSearcher searcher = new SimpleSearcher();
				String results = searcher.search(userQuery);

				System.out.println(results);
			
		}
	}
}
