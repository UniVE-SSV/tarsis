package it.unive.tarsis.test;

import static org.junit.Assert.assertTrue;

import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.regex.Atom;
import it.unive.tarsis.regex.Comp;
import it.unive.tarsis.regex.Or;
import it.unive.tarsis.regex.TopAtom;
import java.util.Collection;
import java.util.HashSet;
import org.junit.Test;

public class ReplaceTest {

	@Test
	public void replaceTestOldFa001() {

		Automaton a = Automaton.mkAutomaton("a");
		Automaton search = Automaton.mkAutomaton("i");
		Automaton replacer = Automaton.mkAutomaton("a");

		// "a".replace("i", "a") = {"a"}
		checkEquality(a.replace(search, replacer).getLanguage(), "a");
	}

	@Test
	public void replaceTestOldFa002() {

		Automaton a = Automaton.mkAutomaton("abc");
		Automaton search = Automaton.mkAutomaton("b");
		Automaton replacer = Automaton.mkAutomaton("d");

		// "abc".replace("b", "d") = {"adc"}
		checkEquality(a.replace(search, replacer).getLanguage(), "adc");
	}

	@Test
	public void replaceTestOldFa003() {

		Automaton a = Automaton.mkAutomaton("abbc");
		Automaton search = Automaton.mkAutomaton("bb");
		Automaton replacer = Automaton.mkAutomaton("dd");

		// "abbc".replace("bb", "dd") = {"addc"}
		checkEquality(a.replace(search, replacer).getLanguage(), "addc");
	}

	@Test
	public void replaceTestOldFa004() {

		Automaton a = Automaton.mkAutomaton("abbc");
		Automaton search = Automaton.mkAutomaton("bb");
		Automaton replacer = Automaton.mkAutomaton("");

		// "abbc".replace("bb", "") = {"ac"}
		checkEquality(a.replace(search, replacer).getLanguage(), "ac");
	}

	@Test
	public void replaceTestOldFa005() {

		Automaton a = Automaton.mkAutomaton("abbc");
		Automaton search = Automaton.union(Automaton.mkAutomaton("bb"), Automaton.mkAutomaton("cc"));

		Automaton replacer = Automaton.mkAutomaton("");

		// "abbc".replace({"bb", "cc"}, "") = {"ac", "abcc"}
		checkEquality(a.replace(search, replacer).getLanguage(), "ac", "abbc");
	}

	@Test
	public void replaceTestOldFa006() {

		Automaton a = Automaton.concat(Automaton.mkAutomaton("ab"), Automaton.mkAutomaton("ba"));

		Automaton search = Automaton.mkAutomaton("bb");
		Automaton replacer = Automaton.mkAutomaton("a");

		// "abba".replace("bb", "a") = {"aaa"}
		checkEquality(a.replace(search, replacer).getLanguage(), "aaa");
	}

	@Test
	public void replaceTestOldFa007() {

		Automaton a = Automaton.mkAutomaton("bbd");

		Automaton search = Automaton.mkAutomaton("bb");
		Automaton replacer = Automaton.mkAutomaton("cc");

		// "bbd".replace("bb", "cc") = {"ccd"}
		checkEquality(a.replace(search, replacer).getLanguage(), "ccd");
	}

	@Test
	public void replaceTestOldFa008() {

		Automaton a = Automaton.union(Automaton.mkAutomaton("abc"), Automaton.mkAutomaton("def"));

		Automaton search = Automaton.mkAutomaton("abc");
		Automaton replacer = Automaton.mkAutomaton("h");

		// {"abc", "def"}.replace("abc", "h") = {"h", "def"}
		checkEquality(a.replace(search, replacer).getLanguage(), "h", "def");
	}

	@Test
	public void replaceTestOldFa009() {

		Automaton a = Automaton.union(Automaton.mkAutomaton("a"), Automaton.mkAutomaton("b"));

		Automaton search = Automaton.mkAutomaton("a");
		Automaton replacer = Automaton.mkAutomaton("c");

		// {"a", "b"}.replace("a", "c") = {"b", "c"}
		checkEquality(a.replace(search, replacer).getLanguage(), "b", "c");
	}

	@Test
	public void replaceTestOldFa010() {

		Automaton a = Automaton.union(Automaton.mkAutomaton("ab"), Automaton.mkAutomaton("dc"));

		Automaton search = Automaton.mkAutomaton("a");
		Automaton replacer = Automaton.mkAutomaton("c");

		// {"ab", "dc"}.replace("a", "c") = {"cb", "dc"}
		checkEquality(a.replace(search, replacer).getLanguage(), "cb", "dc");
	}

	@Test
	public void replaceTestOldFa011() {

		Automaton a = Automaton.union(Automaton.mkAutomaton("ab"), Automaton.mkAutomaton("cb"));

		Automaton search = Automaton.mkAutomaton("b");
		Automaton replacer = Automaton.mkAutomaton("d");

		// {"ab", "cb"}.replace("b", "d") = {"ad", "cd"}
		checkEquality(a.replace(search, replacer).getLanguage(), "ad", "cd");
	}

	@Test
	public void replaceTestOldFa012() {

		Automaton a = Automaton.union(Automaton.mkAutomaton("ab"), Automaton.mkAutomaton("cb"));

		Automaton search = Automaton.mkAutomaton("b");
		Automaton replacer = Automaton.mkAutomaton("");

		// {"ab", "cb"}.replace("b", "") = {"a", "c"}
		checkEquality(a.replace(search, replacer).getLanguage(), "a", "c");
	}

	@Test
	public void replaceTestOldFa013() {

		Automaton a = Automaton.union(Automaton.mkAutomaton("abc"), Automaton.mkAutomaton("cbd"));

		Automaton search = Automaton.mkAutomaton("b");
		Automaton replacer = Automaton.mkAutomaton("e");

		// {"abc", "cbd"}.replace("b", "e") = {"aec", "ced"}
		checkEquality(a.replace(search, replacer).getLanguage(), "aec", "ced");
	}

	@Test
	public void replaceTestOldFa014() {

		Automaton a = Automaton.mkAutomaton("abcdef");

		Automaton search = Automaton.mkAutomaton("bcde");
		Automaton replacer = Automaton.mkAutomaton("");

		// {"abcdef"}.replace("bcde", "") = {"af"}
		checkEquality(a.replace(search, replacer).getLanguage(), "af");
	}

	@Test
	public void replaceTestOldFa015() {

		Automaton a = Automaton.mkAutomaton("marco");

		Automaton search = Automaton.mkAutomaton("rc");
		Automaton replacer = Automaton.mkAutomaton("lt");

		// {"marco"}.replace("rc", "lt") = {"malto"}
		checkEquality(a.replace(search, replacer).getLanguage(), "malto");
	}

	@Test
	public void replaceTestOldFa016() {

		Automaton a = Automaton.union(Automaton.mkAutomaton("abc"), Automaton.mkAutomaton("def"));

		Automaton search = Automaton.union(Automaton.mkAutomaton("a"), Automaton.mkAutomaton("c"),
				Automaton.mkAutomaton("st"), Automaton.mkAutomaton("ef"));

		Automaton replacer = Automaton.union(Automaton.mkAutomaton("g"), Automaton.mkAutomaton("hi"));

		// {"abc", "def"}.replace({"a", "c", "st", "ef"}, {"g", "hi"})
		// = {"abg", "abhi", "gbc", "hibc", "abc", "def", "dg", "dhi"}
		checkEquality(a.replace(search, replacer).getLanguage(), "abg", "abhi", "gbc", "hibc", "abc", "def",
				"dg", "dhi");
	}

	@Test
	public void replaceTestOldFa017() {

		Automaton a = Automaton.union(Automaton.mkAutomaton("pearl"), Automaton.mkAutomaton("garnet"));

		Automaton search = Automaton.union(Automaton.mkAutomaton(""), // apaeaaarala,
																		// agaaaranaeata
				Automaton.mkAutomaton("ar"), // peal, ganet
				Automaton.mkAutomaton("rose"), // pearl, garnet
				Automaton.mkAutomaton("et")); // pearl, garna

		Automaton replacer = Automaton.mkAutomaton("a");

		checkEquality(a.replace(search, replacer).getLanguage(), "pearl", "garnet", "apaeaaarala",
				"agaaaranaeata", "peal", "ganet", "garna");
	}

	@Test
	public void replaceTestOldFa018() {

		Automaton a = Automaton.mkAutomaton("charliebrownalpha");

		Automaton search = Automaton.union(Automaton.mkAutomaton("charlie"), Automaton.mkAutomaton("brown"));

		Automaton replacer = Automaton.mkAutomaton("");

		// {"charliebrownalpha"}.replace({"charlie", "brown"}, "")
		// = {"brownalpha", "charliealpha", "charliebrownalpha"}
		checkEquality(a.replace(search, replacer).getLanguage(), "brownalpha", "charliealpha",
				"charliebrownalpha");
	}

	@Test
	public void replaceTestOldFa019() {

		Automaton a = Automaton.union(Automaton.mkAutomaton("ab"), Automaton.mkAutomaton("ba"));

		Automaton search = Automaton.mkAutomaton("b");
		Automaton replacer = Automaton.mkAutomaton("a");

		// {"ba", "ab"}.replace("b", "a") = {"aa"}
		checkEquality(a.replace(search, replacer).getLanguage(), "aa");
	}

	@Test
	public void replaceTestOldFa020() {

		Automaton a = TopAtom.INSTANCE.toAutomaton();

		Automaton search = Automaton.mkAutomaton("b");
		Automaton replacer = Automaton.mkAutomaton("a");

		// T.replace("b", "a") = {T}
		checkEquality(a.replace(search, replacer).getLanguage(), TopAtom.INSTANCE.toString());
	}

	@Test
	public void replaceTestOldFa021() {

		Automaton a = Automaton.mkAutomaton("ban");

		Automaton search = Automaton.mkAutomaton("a");
		Automaton replacer = Automaton.mkAutomaton("k");

		// "ban".replace("b", "k") = {"bkn"}
		checkEquality(a.replace(search, replacer).getLanguage(), "bkn");
	}

	@Test
	public void replaceTestOldFa022() {

		Automaton a = Automaton.mkAutomaton("bana");

		Automaton search = Automaton.mkAutomaton("a");
		Automaton replacer = Automaton.mkAutomaton("k");

		// "bana".replace("a", "k") = {"bknk"}
		checkEquality(a.replace(search, replacer).getLanguage(), "bknk");
	}

	@Test
	public void replaceTestOldFa023() {

		Automaton a = Automaton.mkAutomaton("bananana");

		Automaton search = Automaton.mkAutomaton("a");
		Automaton replacer = Automaton.mkAutomaton("k");

		// "bananana".replace("a", "k") = {"bknknknk"}
		checkEquality(a.replace(search, replacer).getLanguage(), "bknknknk");
	}

	@Test
	public void replaceTestWithTop001() {

		Automaton a = TopAtom.INSTANCE.toAutomaton();
		Automaton search = Automaton.mkAutomaton("a");
		Automaton replacer = Automaton.mkAutomaton("k");

		// T.replace("a", "k") = {T}
		checkEquality(a.replace(search, replacer).getLanguage(), TopAtom.INSTANCE.toString());
	}

	@Test
	public void replaceTestWithTop002() {

		Automaton a = new Comp(new Atom("abc"), TopAtom.INSTANCE).toAutomaton();
		Automaton search = Automaton.mkAutomaton("a");
		Automaton replacer = Automaton.mkAutomaton("k");

		// abcT.replace("a", "k") = {kbcT}
		checkEquality(a.replace(search, replacer).getLanguage(), "kbc" + TopAtom.INSTANCE.toString());
	}

	@Test
	public void replaceTestWithTop003() {

		Automaton a = new Comp(new Comp(new Atom("abc"), TopAtom.INSTANCE), new Atom("cba")).toAutomaton();
		Automaton search = Automaton.mkAutomaton("a");
		Automaton replacer = Automaton.mkAutomaton("k");

		// abcTcba.replace("a", "k") = {kbcTcbk}
		checkEquality(a.replace(search, replacer).getLanguage(), "kbc" + TopAtom.INSTANCE.toString() + "cbk");
	}

	@Test
	public void replaceTestWithTop004() {

		Automaton a = new Comp(new Comp(new Atom("abc"), TopAtom.INSTANCE), new Atom("cba")).toAutomaton();
		Automaton search = Automaton.mkAutomaton("abc");
		Automaton replacer = Automaton.mkAutomaton("");

		// abcTcba.replace("abc", "") = {Tcba}
		checkEquality(a.replace(search, replacer).getLanguage(), TopAtom.INSTANCE.toString() + "cba");
	}

	@Test
	public void replaceTestWithTop005() {

		Automaton a = new Comp(new Or(new Atom("a"), new Atom("b")), TopAtom.INSTANCE).toAutomaton();
		Automaton search = Automaton.mkAutomaton("a");
		Automaton replacer = Automaton.mkAutomaton("");

		// (a | b)T.replace("abc", "") = {T, bT}
		checkEquality(a.replace(search, replacer).getLanguage(), TopAtom.INSTANCE.toString(),
				"b" + TopAtom.INSTANCE.toString());
	}

	@Test
	public void replaceTestWithTop006() {

		Automaton a = new Comp(new Or(new Atom("a"), new Atom("b")), TopAtom.INSTANCE).toAutomaton();
		Automaton search = Automaton.mkAutomaton("b");
		Automaton replacer = Automaton.mkAutomaton("");

		// (a | b)T.replace("b", "") = {aT, T}
		checkEquality(a.replace(search, replacer).getLanguage(), TopAtom.INSTANCE.toString(),
				"a" + TopAtom.INSTANCE.toString());
	}

	@Test
	public void replaceTestWithBranch001() {

		Automaton a = new Or(new Atom("a"), new Comp(new Atom("a"), new Or(new Atom("b"), new Atom("c"))))
				.toAutomaton();
		Automaton search = Automaton.mkAutomaton("");
		Automaton replacer = Automaton.mkAutomaton("x");

		// (a | a(b|c)).replace("", "x") = {xax, xaxbx, xaxcx}
		checkEquality(a.replace(search, replacer).getLanguage(), "xax", "xaxbx", "xaxcx");
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
