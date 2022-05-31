package it.unive.tarsis.test;

import static it.unive.tarsis.automata.Automaton.concat;
import static it.unive.tarsis.automata.Automaton.mkAutomaton;
import static it.unive.tarsis.automata.Automaton.mkEmptyLanguage;
import static it.unive.tarsis.automata.Automaton.union;
import static it.unive.tarsis.automata.algorithms.RegexExtractor.getRegexesFromPaths;
import static it.unive.tarsis.test.TestUtil.addEdges;
import static it.unive.tarsis.test.TestUtil.build;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.automata.State;
import it.unive.tarsis.automata.Transition;
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

		assertEquals(a, regexesToAutomaton(getRegexesFromPaths(a)));
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

		assertEquals(a, regexesToAutomaton(getRegexesFromPaths(a)));
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

		assertEquals(a, regexesToAutomaton(getRegexesFromPaths(a)));

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

		assertEquals(a, regexesToAutomaton(getRegexesFromPaths(a)));
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

		assertEquals(a, regexesToAutomaton(getRegexesFromPaths(a)));
	}

	@Test
	public void pathTest006() {
		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, false);
		State q2 = new State("q2", false, true);

		Automaton a = addEdges(build(q0, q1, q2), new Transition(q0, q1, new Atom("id=T;")),
				new Transition(q1, q2, new Atom("x=5;")), new Transition(q1, q1, new Atom("y=7;")));

		assertEquals(a, regexesToAutomaton(getRegexesFromPaths(a)));
	}

	@Test
	public void pathTest007() {
		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, false);
		State q2 = new State("q2", false, true);

		Automaton a = addEdges(build(q0, q1, q2), new Transition(q0, q1, new Atom("x=1;")),
				new Transition(q1, q0, new Atom("y=2;")), new Transition(q1, q2, new Atom("x=x+1;")),
				new Transition(q2, q1, new Atom("y=y+1;")));

		assertEquals(a, regexesToAutomaton(getRegexesFromPaths(a)));
	}

	@Test
	public void pathTest008() {
		Automaton a = concat(mkAutomaton("a"), mkAutomaton("b").star(), mkAutomaton("c"));

		assertEquals(a, regexesToAutomaton(getRegexesFromPaths(a)));
	}

	@Test
	public void pathTest009() {
		Automaton a = concat(mkAutomaton("aaa"),
				union(mkAutomaton("bbb"), mkAutomaton("eeee")).star(), mkAutomaton("cccc"));

		assertEquals(a, regexesToAutomaton(getRegexesFromPaths(a)));
	}

	@Test
	public void pathTest010() {
		Automaton a = concat(mkAutomaton("zb"),
				concat(mkAutomaton("cc"), mkAutomaton("ny"), mkAutomaton("lz")).star(), mkAutomaton("cccc"));

		assertEquals(a, regexesToAutomaton(getRegexesFromPaths(a)));
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

		assertEquals(a, regexesToAutomaton(getRegexesFromPaths(a)));
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

		assertEquals(a, regexesToAutomaton(getRegexesFromPaths(a)));
	}

	@Test
	public void pathTest013() {

		Automaton a = mkAutomaton("rw");

		Automaton b = concat(mkAutomaton("rw"), mkAutomaton("hj"), mkAutomaton("ln"), mkAutomaton("vx").star());

		a = union(a, b);
		assertTrue(a.isContained(regexesToAutomaton(getRegexesFromPaths(a))));
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

		assertEquals(a, regexesToAutomaton(getRegexesFromPaths(a)));
	}

	@Test
	public void pathTest015() {
		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, false);
		State q2 = new State("q2", false, true);

		Automaton a = addEdges(build(q0, q1, q2), new Transition(q0, q1, new Atom("cd")),
				new Transition(q0, q2, new Atom("kf")), new Transition(q1, q1, new Atom("cd")),
				new Transition(q1, q2, new Atom("sp")));

		assertEquals(a, regexesToAutomaton(getRegexesFromPaths(a)));
	}

	@Test
	public void pathTest016() {
		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, false);
		State q2 = new State("q2", false, true);

		Automaton a = addEdges(build(q0, q1, q2), new Transition(q0, q1, new Atom("cd")),
				new Transition(q0, q2, new Atom("kf")), new Transition(q1, q1, new Atom("cd")),
				new Transition(q1, q2, new Atom("sp")));

		assertTrue(a.isContained(regexesToAutomaton(getRegexesFromPaths(a))));
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

		assertTrue(a.isContained(regexesToAutomaton(getRegexesFromPaths(a))));
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

		assertTrue(a.isContained(regexesToAutomaton(getRegexesFromPaths(a))));
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

		assertTrue(a.isContained(regexesToAutomaton(getRegexesFromPaths(a))));
	}

	private Automaton regexesToAutomaton(Set<RegularExpression> regexes) {

		Automaton result = mkEmptyLanguage();

		for (RegularExpression r : regexes)
			result = union(result, r.toAutomaton());

		result = result.minimize();
		return result;

	}
}
