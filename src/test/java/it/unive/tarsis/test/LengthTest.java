package it.unive.tarsis.test;

import static org.junit.Assert.assertEquals;

import it.unive.tarsis.AutomatonString;
import it.unive.tarsis.AutomatonString.Interval;
import it.unive.tarsis.automata.Automaton;
import org.junit.Test;

public class LengthTest {

	@Test
	public void lengthTestOldFa001() {
		AutomatonString a = new AutomatonString("abc");

		// "abc".length = [3,3]
		assertEquals(a.length(), new Interval(3, 3, false));
	}

	@Test
	public void lengthTestOldFa002() {
		AutomatonString a = new AutomatonString("");

		// "".length = [0,0]
		assertEquals(a.length(), new Interval(0, 0, false));
	}

	@Test
	public void lengthTestOldFa003() {
		AutomatonString a = new AutomatonString("a", "bc", "abc");

		// {"a", "bc", "abc"}.length = [1,3]
		assertEquals(a.length(), new Interval(1, 3, false));
	}

	@Test
	public void lengthTestOldFa004() {
		AutomatonString a = new AutomatonString("aaa", "bcc", "abc");

		// {"aaa", "bcc", "abc"}.length = [3,3]
		assertEquals(a.length(), new Interval(3, 3, false));
	}

	@Test
	public void lengthTestOldFa005() {
		AutomatonString a = new AutomatonString("", "bcc", "abcde");

		// {"", "bcc", "abcde"}.length = [0,3]
		assertEquals(a.length(), new Interval(0, 5, false));
	}

	@Test
	public void lengthTestCycles001() {
		AutomatonString a = new AutomatonString(Automaton.mkAutomaton("a").star());

		// {a*}.length = [0,+Inf]
		assertEquals(a.length(), new Interval(0, 0, true));
	}

	@Test
	public void lengthTestCyclesPostConcat002() {
		AutomatonString a = new AutomatonString(Automaton.mkAutomaton("a").star().concat(Automaton.mkAutomaton("aaa")));

		// {a*aaa}.length = [3,+Inf]
		assertEquals(a.length(), new Interval(3, 3, true));
	}

	@Test
	public void lengthTestCyclesPreConcat003() {
		AutomatonString a = new AutomatonString(Automaton.mkAutomaton("aaa").concat(Automaton.mkAutomaton("a").star()));

		// {aaaa*}.length = [3,+Inf]
		assertEquals(a.length(), new Interval(3, 3, true));
	}

	@Test
	public void lengthTestCyclesUnion() {
		AutomatonString a = new AutomatonString(Automaton.mkAutomaton("aaa").union(Automaton.mkAutomaton("a").star()));

		// {a*, aaa}.length = [0,+Inf]
		assertEquals(a.length(), new Interval(0, 0, true));
	}

}
