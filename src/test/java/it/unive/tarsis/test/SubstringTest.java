package it.unive.tarsis.test;

import static it.unive.tarsis.automata.Automaton.concat;
import static it.unive.tarsis.automata.Automaton.mkAutomaton;
import static it.unive.tarsis.automata.Automaton.union;
import static it.unive.tarsis.automata.algorithms.RegexExtractor.getMinimalRegex;
import static it.unive.tarsis.strings.ExtString.mkEmptyString;
import static it.unive.tarsis.strings.ExtString.mkString;
import static it.unive.tarsis.strings.ExtString.mkStrings;
import static it.unive.tarsis.strings.ExtString.mkTopString;
import static org.junit.Assert.assertTrue;

import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.regex.Atom;
import it.unive.tarsis.regex.Comp;
import it.unive.tarsis.regex.Or;
import it.unive.tarsis.regex.Star;
import it.unive.tarsis.regex.TopAtom;
import it.unive.tarsis.strings.ExtString;
import java.util.Collection;
import java.util.HashSet;
import org.junit.Test;

public class SubstringTest {

	@Test
	public void testBasicSubstrings() {
		checkEquality(new Atom("a").substring(0, 1), mkString("a"));
		checkEquality(new Atom("ab").substring(0, 2), mkString("ab"));

		checkEquality(new Or(new Atom("ab"), new Atom("ad")).substring(0, 1), mkString("a"));
		checkEquality(new Or(new Atom("ab"), new Atom("ad")).substring(0, 2), mkStrings("ab", "ad"));
		checkEquality(new Or(new Atom("ab"), new Or(new Atom("ac"), new Atom("ad"))).substring(0, 2),
				mkStrings("ab", "ad", "ac"));

		checkEquality(new Comp(new Atom("a"), new Atom("b")).substring(0, 1), mkString("a"));
		checkEquality(new Comp(new Atom("a"), new Atom("b")).substring(0, 2), mkString("ab"));
		checkEquality(new Comp(new Atom("a"), new Atom("b")).substring(0, 2), mkString("ab"));

		checkEquality(new Star(new Atom("a")).substring(0, 0), mkEmptyString());
		checkEquality(new Star(new Atom("a")).substring(0, 1), mkString("a"));
		checkEquality(new Star(new Atom("a")).substring(0, 2), mkStrings("aa"));
		checkEquality(new Star(new Atom("a")).substring(0, 10), mkStrings("aaaaaaaaaa"));

		checkEquality(TopAtom.INSTANCE.substring(4, 6), mkTopString(2));
	}

	@Test
	public void testEmptyString001() {

		Automaton a = concat(mkAutomaton("abc"),
				union(mkAutomaton("a"), mkAutomaton("def")));

		// abc(a | def)[0,0] = {epsilon}
		checkEquality(getMinimalRegex(a).substring(0, 0), mkEmptyString());
	}

	@Test
	public void testEmptyString002() {

		Automaton a = concat(mkAutomaton("abc"),
				union(mkAutomaton("a"), mkAutomaton("def")));

		// abc(a | def)[5,5] = {epsilon}
		checkEquality(getMinimalRegex(a).substring(5, 5), mkEmptyString());
	}

	@Test
	public void testComp001() {
		// (abc + d)(defg + h)[2,4] = {cd, ch, ef}
		checkEquality(new Comp(new Or(new Atom("abc"), new Atom("d")), new Or(new Atom("defg"), new Atom("h")))
				.substring(2, 4), mkStrings("cd", "ch", "ef"));
	}

	@Test
	public void testComp002() throws Exception {

		Automaton a = concat(mkAutomaton("a"), mkAutomaton("b"),
				mkAutomaton("c"),
				mkAutomaton("d"));

		// abcd[1,2] = {b}
		checkEquality(getMinimalRegex(a).substring(1, 2), mkString("b"));
	}

	@Test
	public void testComp003() throws Exception {

		Automaton a = concat(mkAutomaton("ab"), mkAutomaton("cd"));

		// abcd[1,3] = {bc}
		checkEquality(getMinimalRegex(a).substring(1, 3), mkString("bc"));
	}

	@Test
	public void testComp004() throws Exception {

		Automaton a = concat(mkAutomaton("ab"), mkAutomaton("cd"));

		// abcd[0,3] = {abc}
		checkEquality(getMinimalRegex(a).substring(0, 3), mkString("abc"));
	}

	@Test
	public void testOr001() {

		Automaton a = union(mkAutomaton("abc"), mkAutomaton("def"));

		// (abc | def)[1,2] = {b,e}
		checkEquality(getMinimalRegex(a).substring(1, 2), mkStrings("b", "e"));
	}

	@Test
	public void testOr002() {

		Automaton a = union(mkAutomaton("abc"), mkAutomaton("dbf"));

		// (abc | dbf)[1,2] = {b}
		checkEquality(getMinimalRegex(a).substring(1, 2), mkString("b"));
	}

	@Test
	public void testOr003() {

		Automaton a = union(mkAutomaton("abc"), mkAutomaton("def"),
				mkAutomaton("ghi"));

		// (abc | def | ghi)[1,2] = {b, e, h}
		checkEquality(getMinimalRegex(a).substring(1, 2), mkStrings("b", "e", "h"));
	}

	@Test
	public void testOr004() {

		Automaton a = concat(mkAutomaton("abc"),
				union(mkAutomaton("abc"), mkAutomaton("def")));

		// abc(abc | def)[1,2] = {b}
		checkEquality(getMinimalRegex(a).substring(1, 2), mkString("b"));
	}

	@Test
	public void testOr005() {

		Automaton a = concat(mkAutomaton("abc"),
				union(mkAutomaton("abc"), mkAutomaton("def")));

		// abc(abc | def)[2,4] = {ca, cd}
		checkEquality(getMinimalRegex(a).substring(2, 4), mkStrings("ca", "cd"));
	}

	@Test
	public void testStar001() {

		Automaton a = concat(mkAutomaton("abc").star(), mkAutomaton("def"));

		// (abc)*def[1,2] = {b}
		checkEquality(getMinimalRegex(a).substring(1, 2), mkStrings("e", "b"));
	}

	@Test
	public void testStar002() {

		Automaton a = concat(mkAutomaton("abc").star(), mkAutomaton("def"));

		// (abc)*[1,3] = {"", bc}
		checkEquality(getMinimalRegex(a).substring(1, 3), mkStrings("ef", "bc"));
	}

	@Test
	public void testStar003() {
		Automaton a = mkAutomaton("abcd").star();

		// (abcd)*[3,5] = {da}
		checkEquality(getMinimalRegex(a).substring(3, 5), mkStrings("da"));
	}

	@Test
	public void testStar004() {
		Automaton a = concat(mkAutomaton("abcd").star(), mkAutomaton("e"));

		// (abcd)*e[3,5] = {de, da}
		checkEquality(getMinimalRegex(a).substring(3, 5), mkStrings("de", "da"));
	}

	@Test
	public void testStar005() {
		// (y=5;)*x=7;[2,9] = {7;, 5;x=7;, 5;y=5;x, 5;y=5;y}
		checkEquality(new Comp(new Star(new Atom("y=5;")), new Atom("x=7;")).substring(2, 9),
				mkStrings("5;y=5;x", "5;y=5;y"));
	}

	@Test
	public void testTop001() {
		// (y=TOP;)*x=7;[2,9] = {7;, 5;x=7;, 5;y=5;x, 5;y=5;y}
		checkEquality(
				new Comp(new Star(new Comp(new Comp(new Atom("y="), TopAtom.INSTANCE), new Atom(";"))),
						new Atom("x=7;")).substring(2, 9),
				// mkString("7;"),
				// mkString(";x=7;"),
				// mkTopString(1).concat(mkString(";x=7;")), THESE ARE ALL
				// INCOMPLETE SUBSTRINGS
				mkString(";y=;x=7"), mkString(";y=;y=;"), mkString(";y=;y=").concat(mkTopString(1)),
				mkString(";y=").concat(mkTopString(1)).concat(mkString(";x=")),
				mkString(";y=").concat(mkTopString(1)).concat(mkString(";y=")),
				mkString(";y=").concat(mkTopString(2)).concat(mkString(";x")),
				mkString(";y=").concat(mkTopString(2)).concat(mkString(";y")),
				mkString(";y=").concat(mkTopString(3)).concat(mkString(";")), mkString(";y=").concat(mkTopString(4)),
				mkTopString(1).concat(mkString(";y=;x=")), mkTopString(1).concat(mkString(";y=;y=")),
				mkTopString(1).concat(mkString(";y=").concat(mkTopString(1)).concat(mkString(";x"))),
				mkTopString(1).concat(mkString(";y=").concat(mkTopString(1)).concat(mkString(";y"))),
				mkTopString(1).concat(mkString(";y=")).concat(mkTopString(2)).concat(mkString(";")),
				mkTopString(1).concat(mkString(";y=")).concat(mkTopString(3)), mkTopString(2).concat(mkString(";x=7;")),
				mkTopString(2).concat(mkString(";y=;x")), mkTopString(2).concat(mkString(";y=;y")),
				mkTopString(2).concat(mkString(";y=")).concat(mkTopString(1)).concat(mkString(";")),
				mkTopString(2).concat(mkString(";y=")).concat(mkTopString(2)), mkTopString(3).concat(mkString(";x=7")),
				mkTopString(3).concat(mkString(";y=;")), mkTopString(3).concat(mkString(";y=")).concat(mkTopString(1)),
				mkTopString(4).concat(mkString(";x=")), mkTopString(4).concat(mkString(";y=")),
				mkTopString(5).concat(mkString(";x")), mkTopString(5).concat(mkString(";y")),
				mkTopString(6).concat(mkString(";")), mkTopString(7));
	}

	@Test
	public void testTop002() {
		// (TOP)[5,5] = {""}
		checkEquality(TopAtom.INSTANCE.substring(5, 5), mkString(""));
	}

	@Test
	public void testTop003() {
		// abc(TOP)[0,3] = {""}
		checkEquality(new Comp(new Atom("abc"), TopAtom.INSTANCE).substring(0, 3), mkString("abc"));
	}

	@Test
	public void testTop004() {
		// abc(TOP)[0,4] = {abcTOP}
		checkEquality(new Comp(new Atom("abc"), TopAtom.INSTANCE).substring(0, 4),
				mkString("abc").concat(mkTopString(1)));
	}

	@Test
	public void testTop005() {
		// (abc | TOP)[1,2] = {b, TOP}

		checkEquality(new Or(new Atom("abc"), TopAtom.INSTANCE).substring(1, 2), mkString("b"), mkTopString(1));
	}

	@Test
	public void subsTestOldFa001() {

		Automaton a = mkAutomaton("a");

		// (a)[0,1] = {a}
		checkEquality(getMinimalRegex(a).substring(0, 1), mkStrings("a"));
	}

	@Test
	public void subsTestOldFa002() {
		Automaton a = mkAutomaton("a");

		// (a)[0,0] = {""}
		checkEquality(getMinimalRegex(a).substring(0, 0), mkStrings(""));
	}

	@Test
	public void subsTestOldFa003() {

		Automaton a = union(mkAutomaton("a"), mkAutomaton("b"));

		// (a || b)[0,1] = ("a", "b")
		checkEquality(getMinimalRegex(a).substring(0, 1), mkStrings("a", "b"));

	}

	@Test
	public void subsTestOldFa004() {
		Automaton a = union(mkAutomaton("abc"), mkAutomaton("def"));

		// (abc || def)[1,2] = {"b", "e"}
		checkEquality(getMinimalRegex(a).substring(1, 2), mkStrings("b", "e"));

	}

	@Test
	public void subsTestOldFa005() {
		Automaton a = mkAutomaton("a").star();

		// (a*)[0,3] = {"aaa"}
		checkEquality(getMinimalRegex(a).substring(0, 3), mkStrings("aaa"));
	}

	@Test
	public void substringTest6() {

		Automaton a = concat(
				union(mkAutomaton("a"), mkAutomaton("b"), mkAutomaton("c")),
				union(mkAutomaton("a"), mkAutomaton("b"), mkAutomaton("c")).star());

		checkEquality(getMinimalRegex(a).substring(0, 2),
				mkStrings("aa", "bb", "cc", "ab", "ac", "bc", "ba", "ca", "cb"));

	}

	private void checkEquality(Collection<ExtString> computed, ExtString... expected) {
		Collection<ExtString> onlyExpected = new HashSet<>();
		for (ExtString exp : expected)
			onlyExpected.add(exp);
		Collection<ExtString> onlyComputed = new HashSet<>(computed);
		onlyComputed.removeAll(onlyExpected);
		onlyExpected.removeAll(computed);

		assertTrue("OnlyExpected: " + onlyExpected + ", OnlyComputed: " + onlyComputed,
				onlyExpected.isEmpty() && onlyComputed.isEmpty());
	}
}
