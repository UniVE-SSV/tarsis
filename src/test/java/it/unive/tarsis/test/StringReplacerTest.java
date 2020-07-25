package it.unive.tarsis.test;

import static org.junit.Assert.assertEquals;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.Test;

import it.unive.tarsis.automata.Automata;
import it.unive.tarsis.automata.Automaton;

public class StringReplacerTest {

	private void performReplace(Automaton original, String toReplace, String str) {
		Set<String> originalLanguage = original.getLanguage();
		Set<String> manualReplace = originalLanguage.stream().map(s -> s.replace(toReplace, str))
				.collect(Collectors.toSet());
		// manualReplace.addAll(originalLanguage); THESE ARE ALL MUST-REPLACES
		Automaton replaced = Automata.replace(original, Automata.mkAutomaton(toReplace), Automata.mkAutomaton(str));
		Set<String> replacedLanguage = replaced.getLanguage();

		if (!manualReplace.equals(replacedLanguage))
			System.err.println(original + " [" + toReplace + "," + str + "] -> " + replaced + "\n"
					+ "\toriginal language: " + new TreeSet<>(originalLanguage) + "\n"
					+ "\tmanually replaced language: " + new TreeSet<>(manualReplace) + "\n" + "\treplaced language: "
					+ new TreeSet<>(replacedLanguage));

		assertEquals("Wrong replace", manualReplace, replacedLanguage);
	}

	@Test
	public void testReplaceNoOr() {
		Automaton a = Automata.mkAutomaton("abcbcd");
		performReplace(a, "bcd", "");
		performReplace(a, "bcd", "h");
		performReplace(a, "bcd", "hk");
		performReplace(a, "bcd", "hkw");
		performReplace(a, "bcd", "hkwy");
	}

	@Test
	public void testReplaceOrInTheMiddle() {
		Automaton a = Automata.concat(Automata.mkAutomaton("abcb"),
				Automata.union(Automata.mkAutomaton("cd"), Automata.mkAutomaton("f"), Automata.mkAutomaton("z")));
		performReplace(a, "bcd", "");
		performReplace(a, "bcd", "h");
		performReplace(a, "bcd", "hk");
		performReplace(a, "bcd", "hkw");
		performReplace(a, "bcd", "hkwy");
	}

	@Test
	public void testReplaceOrAtTheBeginning() {
		Automaton a = Automata.concat(Automata.mkAutomaton("abc"),
				Automata.union(Automata.mkAutomaton("bcd"), Automata.mkAutomaton("bf"), Automata.mkAutomaton("bz")));
		performReplace(a, "bcd", "");
		performReplace(a, "bcd", "h");
		performReplace(a, "bcd", "hk");
		performReplace(a, "bcd", "hkw");
		performReplace(a, "bcd", "hkwy");
	}

	@Test
	public void testReplaceOrAtTheEnd() {
		Automaton a = Automata.concat(Automata.mkAutomaton("abcbc"),
				Automata.union(Automata.mkAutomaton("df"), Automata.mkAutomaton("dz"), Automata.mkAutomaton("u")));
		performReplace(a, "bcd", "");
		performReplace(a, "bcd", "h");
		performReplace(a, "bcd", "hk");
		performReplace(a, "bcd", "hkw");
		performReplace(a, "bcd", "hkwy");
	}

	@Test
	public void testReplaceWithTop() {
		Automaton a = Automata.concat(Automata.mkAutomaton("abcbc"),
				Automata.union(Automata.mkAutomaton("df"), Automata.mkTopAutomaton(), Automata.mkAutomaton("u")));
		performReplace(a, "bcd", "");
		performReplace(a, "bcd", "h");
		performReplace(a, "bcd", "hk");
		performReplace(a, "bcd", "hkw");
		performReplace(a, "bcd", "hkwy");
	}
}
