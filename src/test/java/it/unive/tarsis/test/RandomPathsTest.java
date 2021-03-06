package it.unive.tarsis.test;

import static it.unive.tarsis.automata.Automata.isContained;
import static it.unive.tarsis.automata.algorithms.RegexExtractor.getRegexesFromPaths;
import static it.unive.tarsis.test.TestUtil.generateAutomaton;
import static it.unive.tarsis.test.TestUtil.randomChar;
import static org.junit.Assert.assertTrue;

import it.unive.tarsis.automata.Automata;
import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.automata.State;
import it.unive.tarsis.automata.Transition;
import it.unive.tarsis.regex.RegularExpression;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;

public class RandomPathsTest {

	private static final Set<State> states = new HashSet<>();
	private static final Map<Integer, State> mapping = new HashMap<>();

	@BeforeClass
	public static void initialize() {
		State q0 = new State("q0", true, false);
		states.add(q0);
		mapping.put(0, q0);

		State q1 = new State("q1", false, false);
		states.add(q1);
		mapping.put(1, q1);

		State q2 = new State("q2", false, true);
		states.add(q2);
		mapping.put(2, q2);
	}

	private static void check(Automaton a) {
		assertTrue(isContained(a, regexesToAutomaton(getRegexesFromPaths(a))));
	}

	/**
	 * #states: 3 #transitions: 1 for each state #automata: 100 #sizeofchar: 2
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
	 * #states: 3 #transitions: 2 for each state #automata: 50 #sizeofchar: 2
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
	 * #states: 3 #transitions: 2 for each state plus one self-loop #automata:
	 * 20 #sizeofchar: 2
	 */
	@Test
	public void toRegexTest003() {
		int numberOfTransitionsForEachState = 2;
		int numberOfGeneratedAutomata = 30;
		int sizeOfChar = 2;

		for (int k = 0; k < numberOfGeneratedAutomata; k++) {
			Automaton gen = generateAutomaton(states, mapping, numberOfTransitionsForEachState, sizeOfChar);
			for (State s : states)
				gen.addTransition(new Transition(s, s, randomChar(sizeOfChar)));
			gen.recomputeOutgoingAdjacencyList();
			check(gen);
		}
	}

	private static Automaton regexesToAutomaton(Set<RegularExpression> regexes) {
		Automaton result = Automata.mkEmptyLanguage();

		for (RegularExpression r : regexes)
			result = Automata.union(result, r.toAutomaton());

		return Automata.minimize(result);
	}
}
