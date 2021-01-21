package it.unive.tarsis.test;

import static org.junit.Assert.assertEquals;

import it.unive.tarsis.automata.Automata;
import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.automata.algorithms.RegexExtractor;
import it.unive.tarsis.strings.ExtString;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.Test;

public class RandomSubstringTest {

	private final Random random = new Random();

	private final int MAX_STRINGS = 5;
	private final int MAX_STRING_LENGTH = 10;
	private final int MAX_INIT = MAX_STRING_LENGTH - 1;
	private final int MAX_END = MAX_INIT;

	@Test
	public void randomSubstringTests() {
		int init, end;
		int automataToTest = 5000;

		for (int i = 0; i < automataToTest; i++) {
			init = (int) random.nextInt(MAX_INIT);
			end = (int) random.nextInt(MAX_END);

			checkEquality(randomAutomaton(MAX_STRINGS), Math.min(init, end), Math.max(init, end));
		}
	}

	private void checkEquality(Automaton automaton, int i, int j) {
		Set<String> expectedSubstrings = new HashSet<>();

		if (i == j)
			expectedSubstrings.add("");
		else {
			for (String str : automaton.getLanguage()) {

				try {
					expectedSubstrings.add(str.substring(i, j));
				} catch (IndexOutOfBoundsException e) {
					continue;
				}
			}
		}

		Set<String> actualSubstrings = ExtString.toStrings(RegexExtractor.getMinimalRegex(automaton).substring(i, j));
		assertEquals(expectedSubstrings, actualSubstrings);
	}

	private Automaton randomAutomaton(int languageSize) {
		Automaton result = Automata.mkAutomaton(randomString((int) random.nextInt(MAX_STRING_LENGTH)));

		for (int i = 1; i < languageSize; i++)
			result = Automata.union(result,
					Automata.mkAutomaton(randomString((int) random.nextInt(MAX_STRING_LENGTH))));

		return result;
	}

	private String randomString(int count) {
		String ALPHA_NUMERIC_STRING = "0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}

		return builder.toString();
	}
}
