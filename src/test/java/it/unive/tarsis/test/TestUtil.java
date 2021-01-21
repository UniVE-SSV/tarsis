package it.unive.tarsis.test;

import it.unive.tarsis.automata.Automata;
import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.automata.State;
import it.unive.tarsis.automata.Transition;
import it.unive.tarsis.regex.Atom;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class TestUtil {
	private static final Random random = new Random();

	public static Automaton build(State... states) {
		Set<State> ss = new HashSet<>();
		for (State q : states)
			ss.add(q);

		return new Automaton(new HashSet<>(), ss);
	}

	public static Automaton addEdges(Automaton a, Transition... transitions) {
		for (Transition t : transitions)
			a.addTransition(t);

		a.recomputeOutgoingAdjacencyList();
		return a;
	}

	public static Atom randomChar(int count) {
		String ALPHA_NUMERIC_STRING = "0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}

		return new Atom(builder.toString());
	}

	public static Automaton generateAutomaton(Set<State> states, Map<Integer, State> mapping,
			int numberOfTransitionsForEachState, int charLen) {
		Automaton a = null;

		do {
			Set<Transition> delta = new HashSet<>();

			for (State s : states)
				for (int i = 0; i < numberOfTransitionsForEachState; i++)
					delta.add(new Transition(s, mapping.get(random.nextInt(states.size())), randomChar(charLen)));

			a = new Automaton(delta, states);
		} while (Automata.isEmptyLanguageAccepted(a));

		return a;
	}
}
