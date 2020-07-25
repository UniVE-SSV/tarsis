package it.unive.tarsis.test;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Test;

import it.unive.tarsis.automata.Automata;
import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.regex.Atom;
import it.unive.tarsis.regex.Comp;
import it.unive.tarsis.regex.Or;
import it.unive.tarsis.regex.TopAtom;

public class ReplaceTest {

	@Test
	public void replaceTestOldFa001() {

		Automaton a = Automata.mkAutomaton("a");
		Automaton search = Automata.mkAutomaton("i");
		Automaton replacer = Automata.mkAutomaton("a");

		// "a".replace("i", "a") = {"a"}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "a");
	}

	@Test
	public void replaceTestOldFa002() {

		Automaton a = Automata.mkAutomaton("abc");
		Automaton search = Automata.mkAutomaton("b");
		Automaton replacer = Automata.mkAutomaton("d");

		// "abc".replace("b", "d") = {"adc"}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "adc");
	}

	@Test
	public void replaceTestOldFa003() {

		Automaton a = Automata.mkAutomaton("abbc");
		Automaton search = Automata.mkAutomaton("bb");
		Automaton replacer = Automata.mkAutomaton("dd");

		// "abbc".replace("bb", "dd") = {"addc"}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "addc");
	}

	@Test
	public void replaceTestOldFa004() {

		Automaton a = Automata.mkAutomaton("abbc");
		Automaton search = Automata.mkAutomaton("bb");
		Automaton replacer = Automata.mkAutomaton("");

		// "abbc".replace("bb", "") = {"ac"}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "ac");
	}

	@Test
	public void replaceTestOldFa005() {

		Automaton a = Automata.mkAutomaton("abbc");
		Automaton search = Automata.union(Automata.mkAutomaton("bb"), Automata.mkAutomaton("cc"));

		Automaton replacer = Automata.mkAutomaton("");

		// "abbc".replace({"bb", "cc"}, "") = {"ac", "abcc"}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "ac", "abbc");
	}

	@Test
	public void replaceTestOldFa006() {

		Automaton a = Automata.concat(Automata.mkAutomaton("ab"), Automata.mkAutomaton("ba"));

		Automaton search = Automata.mkAutomaton("bb");
		Automaton replacer = Automata.mkAutomaton("a");

		// "abba".replace("bb", "a") = {"aaa"}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "aaa");
	}

	@Test
	public void replaceTestOldFa007() {

		Automaton a = Automata.mkAutomaton("bbd");

		Automaton search = Automata.mkAutomaton("bb");
		Automaton replacer = Automata.mkAutomaton("cc");

		// "bbd".replace("bb", "cc") = {"ccd"}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "ccd");
	}

	@Test
	public void replaceTestOldFa008() {

		Automaton a = Automata.union(Automata.mkAutomaton("abc"), Automata.mkAutomaton("def"));

		Automaton search = Automata.mkAutomaton("abc");
		Automaton replacer = Automata.mkAutomaton("h");

		// {"abc", "def"}.replace("abc", "h") = {"h", "def"}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "h", "def");
	}

	@Test
	public void replaceTestOldFa009() {

		Automaton a = Automata.union(Automata.mkAutomaton("a"), Automata.mkAutomaton("b"));

		Automaton search = Automata.mkAutomaton("a");
		Automaton replacer = Automata.mkAutomaton("c");

		// {"a", "b"}.replace("a", "c") = {"b", "c"}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "b", "c");
	}

	@Test
	public void replaceTestOldFa010() {

		Automaton a = Automata.union(Automata.mkAutomaton("ab"), Automata.mkAutomaton("dc"));

		Automaton search = Automata.mkAutomaton("a");
		Automaton replacer = Automata.mkAutomaton("c");

		// {"ab", "dc"}.replace("a", "c") = {"cb", "dc"}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "cb", "dc");
	}

	@Test
	public void replaceTestOldFa011() {

		Automaton a = Automata.union(Automata.mkAutomaton("ab"), Automata.mkAutomaton("cb"));

		Automaton search = Automata.mkAutomaton("b");
		Automaton replacer = Automata.mkAutomaton("d");

		// {"ab", "cb"}.replace("b", "d") = {"ad", "cd"}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "ad", "cd");
	}

	@Test
	public void replaceTestOldFa012() {

		Automaton a = Automata.union(Automata.mkAutomaton("ab"), Automata.mkAutomaton("cb"));

		Automaton search = Automata.mkAutomaton("b");
		Automaton replacer = Automata.mkAutomaton("");

		// {"ab", "cb"}.replace("b", "") = {"a", "c"}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "a", "c");
	}

	@Test
	public void replaceTestOldFa013() {

		Automaton a = Automata.union(Automata.mkAutomaton("abc"), Automata.mkAutomaton("cbd"));

		Automaton search = Automata.mkAutomaton("b");
		Automaton replacer = Automata.mkAutomaton("e");

		// {"abc", "cbd"}.replace("b", "e") = {"aec", "ced"}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "aec", "ced");
	}

	@Test
	public void replaceTestOldFa014() {

		Automaton a = Automata.mkAutomaton("abcdef");

		Automaton search = Automata.mkAutomaton("bcde");
		Automaton replacer = Automata.mkAutomaton("");

		// {"abcdef"}.replace("bcde", "") = {"af"}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "af");
	}

	@Test
	public void replaceTestOldFa015() {

		Automaton a = Automata.mkAutomaton("marco");

		Automaton search = Automata.mkAutomaton("rc");
		Automaton replacer = Automata.mkAutomaton("lt");

		// {"marco"}.replace("rc", "lt") = {"malto"}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "malto");
	}

	@Test
	public void replaceTestOldFa016() {

		Automaton a = Automata.union(Automata.mkAutomaton("abc"), Automata.mkAutomaton("def"));

		Automaton search = Automata.union(Automata.mkAutomaton("a"), Automata.mkAutomaton("c"),
				Automata.mkAutomaton("st"), Automata.mkAutomaton("ef"));

		Automaton replacer = Automata.union(Automata.mkAutomaton("g"), Automata.mkAutomaton("hi"));

		// {"abc", "def"}.replace({"a", "c", "st", "ef"}, {"g", "hi"})
		// = {"abg", "abhi", "gbc", "hibc", "abc", "def", "dg", "dhi"}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "abg", "abhi", "gbc", "hibc", "abc", "def",
				"dg", "dhi");
	}

	@Test
	public void replaceTestOldFa017() {

		Automaton a = Automata.union(Automata.mkAutomaton("pearl"), Automata.mkAutomaton("garnet"));

		Automaton search = Automata.union(Automata.mkAutomaton(""), // apaeaaarala, agaaaranaeata
				Automata.mkAutomaton("ar"), // peal, ganet
				Automata.mkAutomaton("rose"), // pearl, garnet
				Automata.mkAutomaton("et")); // pearl, garna

		Automaton replacer = Automata.mkAutomaton("a");

		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "pearl", "garnet", "apaeaaarala",
				"agaaaranaeata", "peal", "ganet", "garna");
	}

	@Test
	public void replaceTestOldFa018() {

		Automaton a = Automata.mkAutomaton("charliebrownalpha");

		Automaton search = Automata.union(Automata.mkAutomaton("charlie"), Automata.mkAutomaton("brown"));

		Automaton replacer = Automata.mkAutomaton("");

		// {"charliebrownalpha"}.replace({"charlie", "brown"}, "")
		// = {"brownalpha", "charliealpha", "charliebrownalpha"}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "brownalpha", "charliealpha",
				"charliebrownalpha");
	}

	@Test
	public void replaceTestOldFa019() {

		Automaton a = Automata.union(Automata.mkAutomaton("ab"), Automata.mkAutomaton("ba"));

		Automaton search = Automata.mkAutomaton("b");
		Automaton replacer = Automata.mkAutomaton("a");

		// {"ba", "ab"}.replace("b", "a") = {"aa"}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "aa");
	}

	@Test
	public void replaceTestOldFa020() {

		Automaton a = TopAtom.INSTANCE.toAutomaton();

		Automaton search = Automata.mkAutomaton("b");
		Automaton replacer = Automata.mkAutomaton("a");

		// T.replace("b", "a") = {T}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), TopAtom.INSTANCE.toString());
	}

	@Test
	public void replaceTestOldFa021() {

		Automaton a = Automata.mkAutomaton("ban");

		Automaton search = Automata.mkAutomaton("a");
		Automaton replacer = Automata.mkAutomaton("k");

		// "ban".replace("b", "k") = {"bkn"}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "bkn");
	}

	@Test
	public void replaceTestOldFa022() {

		Automaton a = Automata.mkAutomaton("bana");

		Automaton search = Automata.mkAutomaton("a");
		Automaton replacer = Automata.mkAutomaton("k");

		// "bana".replace("a", "k") = {"bknk"}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "bknk");
	}

	@Test
	public void replaceTestOldFa023() {

		Automaton a = Automata.mkAutomaton("bananana");

		Automaton search = Automata.mkAutomaton("a");
		Automaton replacer = Automata.mkAutomaton("k");

		// "bananana".replace("a", "k") = {"bknknknk"}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "bknknknk");
	}

	@Test
	public void replaceTestWithTop001() {

		Automaton a = TopAtom.INSTANCE.toAutomaton();
		Automaton search = Automata.mkAutomaton("a");
		Automaton replacer = Automata.mkAutomaton("k");

		// T.replace("a", "k") = {T}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), TopAtom.INSTANCE.toString());
	}

	@Test
	public void replaceTestWithTop002() {

		Automaton a = new Comp(new Atom("abc"), TopAtom.INSTANCE).toAutomaton();
		Automaton search = Automata.mkAutomaton("a");
		Automaton replacer = Automata.mkAutomaton("k");

		// abcT.replace("a", "k") = {kbcT}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "kbc" + TopAtom.INSTANCE.toString());
	}

	@Test
	public void replaceTestWithTop003() {

		Automaton a = new Comp(new Comp(new Atom("abc"), TopAtom.INSTANCE), new Atom("cba")).toAutomaton();
		Automaton search = Automata.mkAutomaton("a");
		Automaton replacer = Automata.mkAutomaton("k");

		// abcTcba.replace("a", "k") = {kbcTcbk}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), "kbc" + TopAtom.INSTANCE.toString() + "cbk");
	}

	@Test
	public void replaceTestWithTop004() {

		Automaton a = new Comp(new Comp(new Atom("abc"), TopAtom.INSTANCE), new Atom("cba")).toAutomaton();
		Automaton search = Automata.mkAutomaton("abc");
		Automaton replacer = Automata.mkAutomaton("");

		// abcTcba.replace("abc", "") = {Tcba}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), TopAtom.INSTANCE.toString() + "cba");
	}

	@Test
	public void replaceTestWithTop005() {

		Automaton a = new Comp(new Or(new Atom("a"), new Atom("b")), TopAtom.INSTANCE).toAutomaton();
		Automaton search = Automata.mkAutomaton("a");
		Automaton replacer = Automata.mkAutomaton("");

		// (a | b)T.replace("abc", "") = {T, bT}
		checkEquality(Automata.replace(a, search, replacer).getLanguage(), TopAtom.INSTANCE.toString(),
				"b" + TopAtom.INSTANCE.toString());
	}

	private void checkEquality(Collection<String> computed, String... expected) {
		Collection<String> onlyExpected = new HashSet<>();
		for (String exp : expected)
			onlyExpected.add(exp);
		Collection<String> onlyComputed = new HashSet<>(computed);
		onlyComputed.removeAll(onlyExpected);
		onlyExpected.removeAll(computed);

		assertTrue("OnlyExpected: " + onlyExpected + ", OnlyComputed: " + onlyComputed,
				onlyExpected.isEmpty() && onlyComputed.isEmpty());
	}
}
