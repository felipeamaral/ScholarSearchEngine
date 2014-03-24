package qmul.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.lucene.queryparser.classic.ParseException;

import qmul.lucene.search.SimpleSearcher;

public class UserInterface {
	public static final String START_MESSAGE = "Welcome! Type your subject and we will search in our Computer Science articles.";

	public static void main(String[] args) throws IOException, ParseException {
		System.out.println(START_MESSAGE);

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		System.out.println("Type your subject and press Enter/Key to search:");
		String userQuery = reader.readLine();

		System.out.println("Results:");
		// TO DO: SEARCH HERE

		SimpleSearcher searcher = new SimpleSearcher();
		String results = searcher.search(userQuery);

		System.out.println(results);
	}
}
