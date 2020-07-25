package it.unive.tarsis.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.unive.tarsis.AutomatonString;

public class AutomatonStringTest {

	@Test
	public void testLength001() {
		AutomatonString s = new AutomatonString("a");
		assertEquals(new AutomatonString.Interval(1, 1, false), s.length());
	}

	@Test
	public void testLength002() {
		AutomatonString s1 = new AutomatonString("aa");
		AutomatonString s2 = new AutomatonString("abfaadfgr");
		assertEquals(new AutomatonString.Interval(2, 9, false), s1.lub(s2).length());
	}

	@Test
	public void testLength003() {
		AutomatonString s1 = new AutomatonString();
		assertEquals(new AutomatonString.Interval(0, 0, true), s1.length());
	}

	@Test
	public void testLength004() {
		AutomatonString s = new AutomatonString("a");
		s = s.concat(s).concat(s).concat(s).concat(s).concat(s).concat(s).widen(s);
		assertEquals(new AutomatonString.Interval(0, 0, true), s.length());
	}

	@Test
	public void testLength005() {
		AutomatonString s1 = new AutomatonString("a");
		AutomatonString s2 = new AutomatonString("b");
		AutomatonString s = s1.concat(s2).concat(s1).concat(s2).concat(s1).concat(s2).concat(s1).widen(s2);
		assertEquals(new AutomatonString.Interval(1, 7, false), s.length());
	}

	@Test
	public void testLength006() {
		AutomatonString s1 = new AutomatonString("a");
		AutomatonString s2 = new AutomatonString("b");
		AutomatonString s = s1.concat(s2).concat(s1).concat(s2).concat(s1).concat(s2).concat(s1).widen(s2, 2);
		assertEquals(new AutomatonString.Interval(1, 7, true), s.length());
	}

	@Test
	public void testContains001() {
		AutomatonString s = new AutomatonString("a");
		assertTrue(s.getRegex().contains("a"));
		assertFalse(s.getRegex().contains("b"));
	}

	@Test
	public void testContains002() {
		AutomatonString s = new AutomatonString("a").concat(new AutomatonString("b"));
		assertTrue(s.getRegex().contains("a"));
		assertTrue(s.getRegex().contains("b"));
		assertTrue(s.getRegex().contains("ab"));
		assertFalse(s.getRegex().contains("ba"));
	}

	@Test
	public void testContains003() {
		AutomatonString s = new AutomatonString("a").concat(new AutomatonString("b")).concat(new AutomatonString("a"))
				.concat(new AutomatonString("b"));
		s = s.widen(new AutomatonString("a"), 2);
		assertTrue(s.getRegex().contains("a"));
		assertFalse(s.getRegex().contains("b"));
		assertFalse(s.getRegex().contains("ab"));
		assertFalse(s.getRegex().contains("ba"));

		assertTrue(s.getRegex().mayContain("a"));
		assertTrue(s.getRegex().mayContain("b"));
		assertTrue(s.getRegex().mayContain("ab"));
		assertTrue(s.getRegex().mayContain("ba"));
	}
}
