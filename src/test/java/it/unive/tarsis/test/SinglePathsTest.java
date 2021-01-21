package it.unive.tarsis.test;

import static it.unive.tarsis.test.TestUtil.addEdges;
import static it.unive.tarsis.test.TestUtil.build;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import it.unive.tarsis.automata.Automata;
import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.automata.State;
import it.unive.tarsis.automata.Transition;
import it.unive.tarsis.automata.algorithms.RegexExtractor;
import it.unive.tarsis.regex.Atom;
import it.unive.tarsis.regex.RegularExpression;
import it.unive.tarsis.regex.TopAtom;
import java.util.Set;
import org.junit.Test;

public class SinglePathsTest {
	@Test
	public void pathTest001() {
		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, false);
		State q2 = new State("q2", false, false);
		State q3 = new State("q3", false, true);

		Automaton a = addEdges(build(q0, q1, q2, q3), new Transition(q0, q1, new Atom("id=")),
				new Transition(q1, q2, TopAtom.INSTANCE), new Transition(q2, q0, new Atom(";")),
				new Transition(q0, q3, new Atom("y=7;")));

		assertEquals(a, regexesToAutomaton(RegexExtractor.getRegexesFromPaths(a)));
	}

	@Test
	public void pathTest002() {
		State q2 = new State("q2", true, false);
		State q0 = new State("q0", false, false);
		State q1 = new State("q1", false, false);
		State q3 = new State("q3", false, true);

		Automaton a = addEdges(build(q0, q1, q2, q3), new Transition(q2, q0, new Atom("id=T;")),
				new Transition(q0, q2, new Atom("id=T;")), new Transition(q2, q1, new Atom("x=5;")),
				new Transition(q1, q3, new Atom("y=7;")), new Transition(q0, q1, new Atom("z=5;")),
				new Transition(q0, q3, new Atom("id=5;")));

		assertEquals(a, regexesToAutomaton(RegexExtractor.getRegexesFromPaths(a)));
	}

	@Test
	public void pathTest003() {

		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, false);
		State q2 = new State("q2", false, false);
		State q3 = new State("q3", false, true);

		Automaton a = addEdges(build(q0, q1, q2, q3), new Transition(q0, q1, new Atom("id=T;")),
				new Transition(q1, q3, new Atom("id=T;")), new Transition(q1, q2, new Atom("x=5;")),
				new Transition(q2, q1, new Atom("y=7;")));

		assertEquals(a, regexesToAutomaton(RegexExtractor.getRegexesFromPaths(a)));

	}

	@Test
	public void pathTest004() {
		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, false);
		State q2 = new State("q2", false, false);
		State q3 = new State("q3", false, true);
		State q4 = new State("q4", false, false);

		Automaton a = addEdges(build(q0, q1, q2, q3, q4), new Transition(q0, q1, new Atom("id=5;")),
				new Transition(q1, q2, new Atom("id=3;")), new Transition(q2, q3, new Atom("x=5;")),
				new Transition(q1, q4, new Atom("y=4;")), new Transition(q4, q2, new Atom("y=5;")));

		assertEquals(a, regexesToAutomaton(RegexExtractor.getRegexesFromPaths(a)));
	}

	@Test
	public void pathTest005() {
		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, false);
		State q2 = new State("q2", false, false);
		State q3 = new State("q3", false, true);
		State q4 = new State("q4", false, false);
		State q5 = new State("q5", false, false);
		State q6 = new State("q6", false, false);

		Automaton a = addEdges(build(q0, q1, q2, q3, q4, q5, q6), new Transition(q0, q1, new Atom("id=1;")),
				new Transition(q1, q4, new Atom("id=2;")), new Transition(q4, q3, new Atom("id=3;")),
				new Transition(q1, q3, new Atom("id=4;")), new Transition(q1, q2, new Atom("id=5;")),
				new Transition(q2, q3, new Atom("id=6;")), new Transition(q2, q5, new Atom("id=7;")),
				new Transition(q5, q3, new Atom("x=8;")), new Transition(q5, q6, new Atom("y=9;")),
				new Transition(q6, q5, new Atom("y=11;")));

		assertEquals(a, regexesToAutomaton(RegexExtractor.getRegexesFromPaths(a)));
	}

	@Test
	public void pathTest006() {
		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, false);
		State q2 = new State("q2", false, true);

		Automaton a = addEdges(build(q0, q1, q2), new Transition(q0, q1, new Atom("id=T;")),
				new Transition(q1, q2, new Atom("x=5;")), new Transition(q1, q1, new Atom("y=7;")));

		assertEquals(a, regexesToAutomaton(RegexExtractor.getRegexesFromPaths(a)));
	}

	@Test
	public void pathTest007() {
		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, false);
		State q2 = new State("q2", false, true);

		Automaton a = addEdges(build(q0, q1, q2), new Transition(q0, q1, new Atom("x=1;")),
				new Transition(q1, q0, new Atom("y=2;")), new Transition(q1, q2, new Atom("x=x+1;")),
				new Transition(q2, q1, new Atom("y=y+1;")));

		assertEquals(a, regexesToAutomaton(RegexExtractor.getRegexesFromPaths(a)));
	}

	@Test
	public void pathTest008() {
		Automaton a = Automata.concat(Automata.mkAutomaton("a"), Automata.star(Automata.mkAutomaton("b")),
				Automata.mkAutomaton("c"));

		assertEquals(a, regexesToAutomaton(RegexExtractor.getRegexesFromPaths(a)));
	}

	@Test
	public void pathTest009() {
		Automaton a = Automata.concat(Automata.mkAutomaton("aaa"),
				Automata.star(Automata.union(Automata.mkAutomaton("bbb"), Automata.mkAutomaton("eeee"))),
				Automata.mkAutomaton("cccc"));

		assertEquals(a, regexesToAutomaton(RegexExtractor.getRegexesFromPaths(a)));
	}

	@Test
	public void pathTest010() {
		Automaton a = Automata.concat(Automata.mkAutomaton("zb"), Automata.star(
				Automata.concat(Automata.mkAutomaton("cc"), Automata.mkAutomaton("ny"), Automata.mkAutomaton("lz"))),
				Automata.mkAutomaton("cccc"));

		assertEquals(a, regexesToAutomaton(RegexExtractor.getRegexesFromPaths(a)));
	}

	@Test
	public void pathTest011() {
		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, false);
		State q2 = new State("q2", false, true);
		State q3 = new State("q3", false, false);

		Automaton a = addEdges(build(q0, q1, q2, q3), new Transition(q0, q1, new Atom("gb")),
				new Transition(q1, q2, new Atom("zt")), new Transition(q2, q3, new Atom("vg")),
				new Transition(q3, q1, new Atom("gb")));

		assertEquals(a, regexesToAutomaton(RegexExtractor.getRegexesFromPaths(a)));
	}

	@Test
	public void pathTest012() {
		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, false);
		State q2 = new State("q2", false, false);
		State q3 = new State("q3", false, false);
		State q4 = new State("q4", false, true);

		Automaton a = addEdges(build(q0, q1, q2, q3, q4), new Transition(q0, q1, new Atom("gb")),
				new Transition(q1, q2, new Atom("zt")), new Transition(q2, q3, new Atom("vg")),
				new Transition(q3, q1, new Atom("gb")), new Transition(q1, q4, new Atom("zt")));

		assertEquals(a, regexesToAutomaton(RegexExtractor.getRegexesFromPaths(a)));
	}

	@Test
	public void pathTest013() {

		Automaton a = Automata.mkAutomaton("rw");

		Automaton b = Automata.concat(Automata.mkAutomaton("rw"), Automata.mkAutomaton("hj"),
				Automata.mkAutomaton("ln"), Automata.star(Automata.mkAutomaton("vx")));

		a = Automata.union(a, b);
		assertTrue(Automata.isContained(a, regexesToAutomaton(RegexExtractor.getRegexesFromPaths(a))));
	}

	@Test
	public void pathTest014() {
		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, false);
		State q2 = new State("q2", false, true);

		Automaton a = addEdges(build(q0, q1, q2), new Transition(q0, q1, new Atom("yz")),
				new Transition(q0, q2, new Atom("lc")), new Transition(q1, q1, new Atom("yz")),
				new Transition(q1, q2, new Atom("gw")), new Transition(q2, q2, new Atom("lc")),
				new Transition(q2, q0, new Atom("yz")));

		assertEquals(a, regexesToAutomaton(RegexExtractor.getRegexesFromPaths(a)));
	}

	@Test
	public void pathTest015() {
		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, false);
		State q2 = new State("q2", false, true);

		Automaton a = addEdges(build(q0, q1, q2), new Transition(q0, q1, new Atom("cd")),
				new Transition(q0, q2, new Atom("kf")), new Transition(q1, q1, new Atom("cd")),
				new Transition(q1, q2, new Atom("sp")));

		assertEquals(a, regexesToAutomaton(RegexExtractor.getRegexesFromPaths(a)));
	}

	@Test
	public void pathTest016() {
		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, false);
		State q2 = new State("q2", false, true);

		Automaton a = addEdges(build(q0, q1, q2), new Transition(q0, q1, new Atom("cd")),
				new Transition(q0, q2, new Atom("kf")), new Transition(q1, q1, new Atom("cd")),
				new Transition(q1, q2, new Atom("sp")));

		assertTrue(Automata.isContained(a, regexesToAutomaton(RegexExtractor.getRegexesFromPaths(a))));
	}

	@Test
	public void pathTest017() {
		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, false);
		State q2 = new State("q2", false, false);
		State q3 = new State("q3", false, false);
		State q4 = new State("q4", false, true);
		State q5 = new State("q5", false, false);

		Automaton a = addEdges(build(q0, q1, q2, q3, q4, q5), new Transition(q0, q1, new Atom("a")),
				new Transition(q1, q2, new Atom("b")), new Transition(q2, q3, new Atom("c")),
				new Transition(q3, q2, new Atom("d")), new Transition(q3, q4, new Atom("e")),
				new Transition(q4, q5, new Atom("f")), new Transition(q5, q1, new Atom("h")));

		assertTrue(Automata.isContained(a, regexesToAutomaton(RegexExtractor.getRegexesFromPaths(a))));
	}

	@Test
	public void pathTest018() {
		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, false);
		State q2 = new State("q2", false, false);
		State q3 = new State("q3", false, true);

		Automaton a = addEdges(build(q0, q1, q2, q3), new Transition(q0, q1, new Atom("a")),
				new Transition(q1, q2, new Atom("b")), new Transition(q1, q3, new Atom("c")),
				new Transition(q2, q3, new Atom("d")), new Transition(q3, q1, new Atom("e")));

		assertTrue(Automata.isContained(a, regexesToAutomaton(RegexExtractor.getRegexesFromPaths(a))));
	}

	@Test
	public void pathTest019() {
		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, true);
		State q2 = new State("q2", false, false);
		State q3 = new State("q3", false, true);

		Automaton a = addEdges(build(q0, q1, q2, q3), new Transition(q0, q1, new Atom("j4")),
				new Transition(q1, q1, new Atom("j4")), new Transition(q1, q3, new Atom("vg")),
				new Transition(q1, q2, new Atom("gx")), new Transition(q2, q3, new Atom("j4")),
				new Transition(q2, q1, new Atom("j4")), new Transition(q3, q3, new Atom("vg")),
				new Transition(q3, q2, new Atom("gx")));

		assertTrue(Automata.isContained(a, regexesToAutomaton(RegexExtractor.getRegexesFromPaths(a))));
	}

	private Automaton regexesToAutomaton(Set<RegularExpression> regexes) {

		Automaton result = Automata.mkEmptyLanguage();

		for (RegularExpression r : regexes)
			result = Automata.union(result, r.toAutomaton());

		result = Automata.minimize(result);
		return result;

	}
}
