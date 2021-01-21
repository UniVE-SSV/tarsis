package it.unive.tarsis.test;

import static it.unive.tarsis.test.TestUtil.generateAutomaton;
import static it.unive.tarsis.test.TestUtil.randomChar;
import static org.junit.Assert.assertEquals;

import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.automata.State;
import it.unive.tarsis.automata.Transition;
import it.unive.tarsis.automata.algorithms.RegexExtractor;
import it.unive.tarsis.regex.RegularExpression;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class RandomToRegexTest {

	private final Set<State> states = new HashSet<>();
	private final Map<Integer, State> mapping = new HashMap<>();

	@Before
	public void initialize() {
		State q0 = new State("q0", true, false);
		states.add(q0);
		mapping.put(0, q0);

		State q1 = new State("q1", false, false);
		states.add(q1);
		mapping.put(1, q1);

		State q2 = new State("q2", false, true);
		states.add(q2);
		mapping.put(2, q2);

		State q3 = new State("q3", false, false);
		states.add(q3);
		mapping.put(3, q3);

		State q4 = new State("q4", false, true);
		states.add(q4);
		mapping.put(4, q4);
	}

	private static void check(Automaton a) {
		RegularExpression fromRegex = RegexExtractor.getMinimalBrzozowskiRegex(a);
		assertEquals(a, fromRegex.toAutomaton());
	}

	/**
	 * #states: 5 #final-states: 2 #transitions: 1 for each state #automata: 100
	 * #sizeofchar: 2
	 */
	@Test
	public void toRegexTest001() {
		int numberOfTransitionsForEachState = 1;
		int numberOfGeneratedAutomata = 100;
		int sizeOfChar = 2;

		for (int k = 0; k < numberOfGeneratedAutomata; k++)
			check(generateAutomaton(states, mapping, numberOfTransitionsForEachState, sizeOfChar));
	}

	/**
	 * #states: 5 #final-states: 2 #transitions: 2 for each state #automata: 50
	 * #sizeofchar: 2
	 */
	@Test
	public void toRegexTest002() {
		int numberOfTransitionsForEachState = 2;
		int numberOfGeneratedAutomata = 50;
		int sizeOfChar = 2;

		for (int k = 0; k < numberOfGeneratedAutomata; k++)
			check(generateAutomaton(states, mapping, numberOfTransitionsForEachState, sizeOfChar));
	}

	/**
	 * #states: 5 #final-states: 2 #transitions: 2 for each state and 1
	 * self-loop #automata: 50 #sizeofchar: 2
	 */
	@Test
	public void toRegexTest003() {
		int numberOfTransitionsForEachState = 2;
		int numberOfGeneratedAutomata = 50;
		int sizeOfChar = 2;

		for (int k = 0; k < numberOfGeneratedAutomata; k++) {
			Automaton gen = generateAutomaton(states, mapping, numberOfTransitionsForEachState, sizeOfChar);
			for (State s : states)
				gen.addTransition(new Transition(s, s, randomChar(sizeOfChar)));
			gen.recomputeOutgoingAdjacencyList();
			check(gen);
		}
	}

	/**
	 * #states: 5 #final-states: 2 #transitions: 3 for each state #automata: 50
	 * #sizeofchar: 2
	 */
	@Test
	public void toRegexTest004() {
		int numberOfTransitionsForEachState = 3;
		int numberOfGeneratedAutomata = 50;
		int sizeOfChar = 2;

		for (int k = 0; k < numberOfGeneratedAutomata; k++)
			check(generateAutomaton(states, mapping, numberOfTransitionsForEachState, sizeOfChar));
	}
}
