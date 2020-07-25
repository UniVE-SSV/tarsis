package it.unive.tarsis.test;

import static it.unive.tarsis.test.TestUtil.addEdges;
import static it.unive.tarsis.test.TestUtil.build;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.automata.State;
import it.unive.tarsis.automata.Transition;
import it.unive.tarsis.automata.algorithms.RegexExtractor;
import it.unive.tarsis.regex.Atom;

public class SingleToRegexTest {

	@Test
	public void dfaToRegexTest001() {
		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, true);
		State q2 = new State("q2", false, false);

		Automaton a = addEdges(build(q0, q1, q2), new Transition(q0, q1, new Atom("fp")),
				new Transition(q0, q2, new Atom("pv")), new Transition(q1, q1, new Atom("fp")),
				new Transition(q1, q2, new Atom("kk")), new Transition(q2, q1, new Atom("fp")),
				new Transition(q2, q2, new Atom("pv")));

		Automaton expected = RegexExtractor.getMinimalBrzozowskiRegex(a).toAutomaton();
		assertEquals(a, expected);
	}

	@Test
	public void dfaToRegexTest002() {
		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, true);

		Automaton a = addEdges(build(q0, q1), new Transition(q0, q1, new Atom("mm")),
				new Transition(q1, q1, new Atom("mm")));

		Automaton expected = RegexExtractor.getMinimalBrzozowskiRegex(a).toAutomaton();
		assertEquals(a, expected);
	}

	@Test
	public void dfaToRegexTest003() {
		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, false);
		State q2 = new State("q2", false, true);

		Automaton a = addEdges(build(q0, q1, q2), new Transition(q0, q1, new Atom("ek")),
				new Transition(q1, q2, new Atom("ek")), new Transition(q2, q2, new Atom("ek")));

		Automaton expected = RegexExtractor.getMinimalBrzozowskiRegex(a).toAutomaton();
		assertEquals(a, expected);
	}
}
