package it.unive.tarsis.test;

import static it.unive.tarsis.test.TestUtil.generateAutomaton;
import static it.unive.tarsis.test.TestUtil.randomChar;
import static org.junit.Assert.assertTrue;

import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.automata.State;
import it.unive.tarsis.automata.Transition;
import it.unive.tarsis.automata.algorithms.RegexExtractor;
import it.unive.tarsis.regex.RegularExpression;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
		assertTrue(a.isContained(regexesToAutomaton(RegexExtractor.getRegexesFromPaths(a))));
	}

	/**
	 * #states: 3 #transitions: 1 for each state #Automaton: 100 #sizeofchar: 2
	 */
	@Test
	public void toRegexTest001() {
		int numberOfTransitionsForEachState = 1;
		int numberOfGeneratedAutomaton = 100;
		int sizeOfChar = 2;

		for (int k = 0; k < numberOfGeneratedAutomaton; k++)
			check(generateAutomaton(states, mapping, numberOfTransitionsForEachState, sizeOfChar));
	}

	/**
	 * #states: 3 #transitions: 2 for each state #Automaton: 50 #sizeofchar: 2
	 */
	@Test
	public void toRegexTest002() {
		int numberOfTransitionsForEachState = 2;
		int numberOfGeneratedAutomaton = 50;
		int sizeOfChar = 2;

		for (int k = 0; k < numberOfGeneratedAutomaton; k++)
			check(generateAutomaton(states, mapping, numberOfTransitionsForEachState, sizeOfChar));
	}

	/**
	 * #states: 3 #transitions: 2 for each state plus one self-loop #Automaton:
	 * 30 #sizeofchar: 2
	 */
	@Test
	public void toRegexTest003() {
		int numberOfTransitionsForEachState = 2;
		int numberOfGeneratedAutomaton = 30;
		int sizeOfChar = 2;

		for (int k = 0; k < numberOfGeneratedAutomaton; k++) {
			Automaton gen = generateAutomaton(states, mapping, numberOfTransitionsForEachState, sizeOfChar);
			for (State s : states)
				gen.addTransition(new Transition(s, s, randomChar(sizeOfChar)));
			gen.recomputeOutgoingAdjacencyList();
			check(gen);
		}
	}

	/**
	 * #states: 500 #transitions: 5 for each state plus one self-loop
	 * #Automaton: 2000 #sizeofchar: 5 P-initial: 0.001 P-final: 0.2
	 */
	@Test
	@Ignore
	public void toRegexTest004() {
		int numberOfStates = 500;
		int numberOfTransitionsForEachState = 10;
		int numberOfGeneratedAutomata = 2000;
		int sizeOfChar = 5;
		double probabilityOfInitialState = 0.001;
		double probabilityOfFinalState = 0.02;

		Set<State> states = new HashSet<>();
		Map<Integer, State> mapping = new HashMap<>();

		State init = new State("q0", true, false);
		states.add(init);
		mapping.put(0, init);

		State end = new State("q499", false, true);
		states.add(end);
		mapping.put(499, end);

		Function<Double, Boolean> prob = p -> Math.random() < p;
		int initials = 0, finals = 0;
		for (int i = 1; i < numberOfStates - 1; i++) {
			State q = new State("q" + i, prob.apply(probabilityOfInitialState), prob.apply(probabilityOfFinalState));
			states.add(q);
			mapping.put(i, q);
			if (q.isInitialState())
				initials++;
			if (q.isFinalState())
				finals++;
		}

		for (int k = 0; k < numberOfGeneratedAutomata; k++) {
			System.out.print("Generating automaton " + (k + 1) + "/" + numberOfGeneratedAutomata + " ("
					+ (1 + initials) + " initials, " + (1 + finals) + " finals)... ");
			long start = System.nanoTime();

			Automaton gen = generateAutomaton(states, mapping, numberOfTransitionsForEachState, sizeOfChar);
			for (State s : states)
				gen.addTransition(new Transition(s, s, randomChar(sizeOfChar)));
			gen.recomputeOutgoingAdjacencyList();

			long elapsed = System.nanoTime() - start;
			System.out.println("Done in " + formatInterval(elapsed));

			System.out.print("Checking automaton " + (k + 1) + "/" + numberOfGeneratedAutomata + "... ");
			start = System.nanoTime();

			check(gen);

			elapsed = System.nanoTime() - start;
			System.out.println("Done in " + formatInterval(elapsed));
		}
	}

	private static String formatInterval(final long l) {
		final long hr = TimeUnit.NANOSECONDS.toHours(l);
		final long min = TimeUnit.NANOSECONDS.toMinutes(l - TimeUnit.HOURS.toNanos(hr));
		final long sec = TimeUnit.NANOSECONDS.toSeconds(l - TimeUnit.HOURS.toNanos(hr) - TimeUnit.MINUTES.toNanos(min));
		final long ms = TimeUnit.NANOSECONDS.toMillis(
				l - TimeUnit.HOURS.toNanos(hr) - TimeUnit.MINUTES.toNanos(min) - TimeUnit.SECONDS.toNanos(sec));
		return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
	}

	private static Automaton regexesToAutomaton(Set<RegularExpression> regexes) {
		Automaton result = Automaton.mkEmptyLanguage();

		for (RegularExpression r : regexes)
			result = Automaton.union(result, r.toAutomaton());

		return result.minimize();
	}
}
