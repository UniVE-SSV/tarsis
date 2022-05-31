package it.unive.tarsis.test;

import static it.unive.tarsis.automata.Automaton.concat;
import static it.unive.tarsis.automata.Automaton.mkAutomaton;
import static it.unive.tarsis.automata.Automaton.mkTopAutomaton;
import static it.unive.tarsis.automata.Automaton.union;
import static org.junit.Assert.assertEquals;

import it.unive.tarsis.automata.Automaton;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.junit.Test;

public class StringReplacerTest {

	private void performReplace(Automaton original, String toReplace, String str) {
		Set<String> originalLanguage = original.getLanguage();
		Set<String> manualReplace = originalLanguage.stream().map(s -> s.replace(toReplace, str))
				.collect(Collectors.toSet());
		// manualReplace.addAll(originalLanguage); THESE ARE ALL MUST-REPLACES
		Automaton replaced = original.replace(mkAutomaton(toReplace), mkAutomaton(str));
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
		Automaton a = mkAutomaton("abcbcd");
		performReplace(a, "bcd", "");
		performReplace(a, "bcd", "h");
		performReplace(a, "bcd", "hk");
		performReplace(a, "bcd", "hkw");
		performReplace(a, "bcd", "hkwy");
	}

	@Test
	public void testReplaceOrInTheMiddle() {
		Automaton a = concat(mkAutomaton("abcb"), union(mkAutomaton("cd"), mkAutomaton("f"), mkAutomaton("z")));
		performReplace(a, "bcd", "");
		performReplace(a, "bcd", "h");
		performReplace(a, "bcd", "hk");
		performReplace(a, "bcd", "hkw");
		performReplace(a, "bcd", "hkwy");
	}

	@Test
	public void testReplaceOrAtTheBeginning() {
		Automaton a = concat(mkAutomaton("abc"), union(mkAutomaton("bcd"), mkAutomaton("bf"), mkAutomaton("bz")));
		performReplace(a, "bcd", "");
		performReplace(a, "bcd", "h");
		performReplace(a, "bcd", "hk");
		performReplace(a, "bcd", "hkw");
		performReplace(a, "bcd", "hkwy");
	}

	@Test
	public void testReplaceOrAtTheEnd() {
		Automaton a = concat(mkAutomaton("abcbc"), union(mkAutomaton("df"), mkAutomaton("dz"), mkAutomaton("u")));
		performReplace(a, "bcd", "");
		performReplace(a, "bcd", "h");
		performReplace(a, "bcd", "hk");
		performReplace(a, "bcd", "hkw");
		performReplace(a, "bcd", "hkwy");
	}

	@Test
	public void testReplaceWithTop() {
		Automaton a = concat(mkAutomaton("abcbc"), union(mkAutomaton("df"), mkTopAutomaton(), mkAutomaton("u")));
		performReplace(a, "bcd", "");
		performReplace(a, "bcd", "h");
		performReplace(a, "bcd", "hk");
		performReplace(a, "bcd", "hkw");
		performReplace(a, "bcd", "hkwy");
	}
}
