package it.unive.tarsis.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import it.unive.tarsis.AutomatonString;
import it.unive.tarsis.automata.Automaton;

public class ContainsTest {

	@Test
	public void containsTestSelfContains() {
		AutomatonString a = new AutomatonString("abc");

		// "abc".contanins("abc") = true
		assertEquals(a.contains(a), true);
	}

	@Test
	public void containsEmptyStringConstantString() {
		AutomatonString a = new AutomatonString("abc");
		AutomatonString search = new AutomatonString("");

		// "abc".contanins("") = true
		assertEquals(a.contains(search), true);
	}

	@Test
	public void containsEmptyStringFiniteStrings() {
		AutomatonString a = new AutomatonString("a", "b", "c");
		AutomatonString search = new AutomatonString("");

		// "abc".contanins("") = true
		assertEquals(a.contains(search), true);
	}

	@Test
	public void containsEmptyStringLoops() {
		AutomatonString a = new AutomatonString(Automaton.mkAutomaton("abc").star());
		AutomatonString search = new AutomatonString("");

		// abc*.contanins("") = true
		assertEquals(a.contains(search), true);
	}

	@Test
	public void containsTestOldFa001() {
		AutomatonString a = new AutomatonString("panda", "sansa", "manga");
		AutomatonString search = new AutomatonString("an");

		// {panda, sansa, manga}.contains(an) = true
		assertEquals(a.contains(search), true);
	}

	@Test
	public void containsTestOldFa002() {
		AutomatonString a = new AutomatonString("panda", "sansa", "manga");
		AutomatonString search = new AutomatonString("an", "p");

		// {panda, sansa, manga}.contains(an, p) = false
		assertEquals(a.contains(search), false);
	}

	@Test
	public void containsTestOldFa003() {
		AutomatonString a = new AutomatonString("panda", "sansa", "manga");
		AutomatonString search = new AutomatonString("koala");

		// {"panda", "sansa", "manga"}.contains("koala") = false
		assertEquals(a.contains(search), false);
	}

	@Test
	public void containsTestOldFa004() {
		AutomatonString a = new AutomatonString("panda!mc", "mc!papanda", "polo!mc!panda");
		AutomatonString search = new AutomatonString("panda", "mc");

		// {"panda!mc", "mc!papanda", "polo!mc!panda"}.contains(panda, mc) =
		// true
		assertEquals(a.contains(search), true);
	}

	@Test
	public void containsTestOldFa005() {
		AutomatonString a = new AutomatonString("panda!mc", "mc!papanda", "polopanda");
		AutomatonString search = new AutomatonString("panda", "mc");

		// {"panda!mc", "mc!papanda", "polopanda"}.contains(panda, mc) = false
		assertEquals(a.contains(search), false);
	}

	@Test
	public void containsTestOldFa006() {
		AutomatonString a = new AutomatonString("panda", "pandone", "pandina", "pandetta");
		AutomatonString search = new AutomatonString("pa", "pan");

		// {"panda", "pandone", "pandina", "pandetta"}.contains("pa", "pan") =
		// true
		assertEquals(a.contains(search), true);
	}

	@Test
	public void containsTestOldFa007() {
		AutomatonString a = new AutomatonString("panda", "ronda", "manga", "pandetta");
		AutomatonString search = new AutomatonString("an");

		// {"panda", "ronda", "manga", "pandetta"}.contains("an") = false
		assertEquals(a.contains(search), false);
	}

	@Test
	public void containsTestOldFa008() {
		AutomatonString a = new AutomatonString("pandaat", "pandamat", "pansarat", "pansasat", "koladat", "kolabato",
				"kosalata", "kosanaat");

		AutomatonString search = new AutomatonString("at");

		// {"pandaat", "pandamat", "pansarat","pansasat",
		// "koladat", "kolabato", "kosalata", "kosanaat"}.contains("at") = true
		assertEquals(a.contains(search), true);
	}

	@Test
	public void containsTestOldFa009() {
		AutomatonString a = new AutomatonString("pandk", "panck", "panrk");
		AutomatonString search = new AutomatonString("an");

		// {"pandk", "panck", "panrk"}.contains("an") = true
		assertEquals(a.contains(search), true);
	}

	@Test
	public void containsTestOldFa010() {
		AutomatonString a = new AutomatonString("pan", "pandk", "panck", "panrk");
		AutomatonString search = new AutomatonString("k");

		// {"pan", "pandk", "panck", "panrk"}.contains("k") = false
		assertEquals(a.contains(search), false);

	}

	@Test
	public void containsTestOldFa011() {
		AutomatonString a = new AutomatonString("pan", "pandk", "panck", "panrw");
		AutomatonString search = new AutomatonString("k");

		// {"pan", "pandk", "panck", "panrw"}.contains("k") = false
		assertEquals(a.contains(search), false);
	}

	@Test
	public void containsTestOldFa012() {
		AutomatonString a = new AutomatonString("panda");
		AutomatonString search = new AutomatonString("da");

		// {"panda"}.contains("da") = true
		assertEquals(a.contains(search), true);
	}

	@Test
	public void containsTestOldFa013() {
		AutomatonString a = new AutomatonString("panda", "nda", "a");
		AutomatonString search = new AutomatonString("nda", "a");

		// {"panda", "nda", "a"}.contains("nda", "a") = false
		assertEquals(a.contains(search), false);
	}

	@Test
	public void containsTestOldFa014() {
		AutomatonString a = new AutomatonString("panda", "anda");
		AutomatonString search = new AutomatonString("nda", "a");

		// {"panda", "anda"}.contains("nda", "a") = true
		assertEquals(a.contains(search), true);
	}

	@Test
	public void containsTestOldFa015() {
		AutomatonString a = new AutomatonString("panda", "anda", "orda");
		AutomatonString search = new AutomatonString("nda", "a");

		// {"panda", "anda", "orda"}.contains("nda", "a") = false
		assertEquals(a.contains(search), false);
	}

	@Test
	public void containsTestOldFa016() {
		AutomatonString a = new AutomatonString("panda", "koala");
		AutomatonString search = new AutomatonString("nda", "ala");

		// {"panda", "koala"}.contains("nda", "ala") = false
		assertEquals(a.contains(search), false);
	}

	@Test
	public void containsTestOldFa017() {
		AutomatonString a = new AutomatonString("panda", "anda", "nda");
		AutomatonString search = new AutomatonString("nda");

		// {"panda", "anda", "nda"}.contains("nda") = true
		assertEquals(a.contains(search), true);
	}

	@Test
	public void containsTestOldFa019() {
		AutomatonString a = new AutomatonString("panda", "pand", "nd");
		AutomatonString search = new AutomatonString("panda");

		// {"panda", "pand", "nd"}.contains("panda") = false
		assertEquals(a.contains(search), false);
	}

	@Test
	public void containsTestOldFa020() {
		AutomatonString a = new AutomatonString("panda", "pand", "nd");
		AutomatonString search = new AutomatonString("panda", "anda", "da");

		// {"panda", "pand", "nd"}.contains("panda", "anda", "da") = false
		assertEquals(a.contains(search), false);
	}

	@Test
	public void containsTestOldFa021() {
		AutomatonString a = new AutomatonString("panda", "pand", "nd");
		AutomatonString search = new AutomatonString("panda", "anda", "da", "d");

		// {"panda", "pand", "nd"}.contains("panda", "anda", "da", "da") = false
		assertEquals(a.contains(search), false);
	}

	@Test
	public void containsTestOldFa022() {
		AutomatonString a = new AutomatonString("panda", "panda", "panda");
		AutomatonString search = new AutomatonString("panda");

		// {"panda"}.contains("panda") = true
		assertEquals(a.contains(search), true);
	}

	@Test
	public void containsTestOldFa023() {
		AutomatonString a = new AutomatonString("panda", "pandapanda");
		AutomatonString search = new AutomatonString("panda");

		// {"panda", "pandapanda"}.contains("panda") = true
		assertEquals(a.contains(search), true);
	}

	@Test
	public void containsTestOldFa024() {
		AutomatonString a = new AutomatonString("panda", "pandapanda");
		AutomatonString search = new AutomatonString("pandapanda");

		// {"panda", "pandapanda"}.contains("pandapanda") = false
		assertEquals(a.contains(search), false);
	}

	@Test
	public void containsTestOldFa025() {
		AutomatonString a = new AutomatonString("ordine");
		AutomatonString search = new AutomatonString("ine", "dine");

		// {"ordine"}.contains("ine", "dine") = true
		assertEquals(a.contains(search), true);
	}

	@Test
	public void containsTestOldFa026() {
		AutomatonString a = new AutomatonString("ordine", "sordine");
		AutomatonString search = new AutomatonString("ine", "dine");

		// {"ordine", "sordine"}.contains("ine", "dine") = true
		assertEquals(a.contains(search), true);
	}

	@Test
	public void containsTestOldFa027() {
		AutomatonString a = new AutomatonString("ordine", "sordine");
		AutomatonString search = new AutomatonString("r");

		// {"ordine", "sordine"}.contains("r") = true
		assertEquals(a.contains(search), true);
	}

	@Test
	public void containsTestOldFa028() {
		AutomatonString a = new AutomatonString(Automaton.mkAutomaton("a").star());
		AutomatonString search = new AutomatonString("a");

		// {a*}.contains("a") = false
		assertEquals(a.contains(search), false);
	}

	@Test
	public void containsTestOldFa029() {
		AutomatonString a = new AutomatonString(Automaton.mkAutomaton("a").star());

		// {a*}.contains(a*) = false
		assertEquals(a.contains(a), false);
	}

	@Test
	public void containsTestOldFa030() {
		AutomatonString a = new AutomatonString("");
		AutomatonString search = new AutomatonString("e");

		// {""}.contains("e") = false
		assertEquals(a.contains(search), false);
	}

	@Test
	public void containsTestOldFa031() {
		AutomatonString a = new AutomatonString("idea");
		AutomatonString search = new AutomatonString("idea");

		// {"idea"}.contains("idea") = true
		assertEquals(a.contains(search), true);
	}

	@Test
	public void containsTestOldFa033() {
		AutomatonString a = new AutomatonString("idea2");
		AutomatonString search = new AutomatonString("idea");

		// {"idea2"}.contains("idea") = true
		assertEquals(a.contains(search), true);
	}

	@Test
	public void containsTestOldFa034() {
		AutomatonString a = new AutomatonString("idea", "riveda", "intrinseca");
		AutomatonString search = new AutomatonString("ea", "va", "ca");

		// {"idea", "riveda", "intrinseca"}.contains("ea", "va", "ca") = false
		assertEquals(a.contains(search), false);
	}

	@Test
	public void containsTestOldFa035() {
		AutomatonString a = new AutomatonString("pandapanda");
		AutomatonString search = new AutomatonString("da", "nda");

		// {"pandapanda"}.contains("da", "nda") = true
		assertEquals(a.contains(search), true);
	}

	@Test
	public void containsTestOldFa036() {
		AutomatonString a = new AutomatonString("pandapanda");
		AutomatonString search = new AutomatonString("ap", "p");

		// {"pandapanda"}.contains("p", "ap") = true
		assertEquals(a.contains(search), true);
	}
}
