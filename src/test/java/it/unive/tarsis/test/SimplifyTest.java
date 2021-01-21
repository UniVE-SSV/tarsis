package it.unive.tarsis.test;

import static org.junit.Assert.assertEquals;

import it.unive.tarsis.regex.Atom;
import it.unive.tarsis.regex.Comp;
import it.unive.tarsis.regex.EmptySet;
import it.unive.tarsis.regex.Or;
import it.unive.tarsis.regex.RegularExpression;
import it.unive.tarsis.regex.Star;
import org.junit.Ignore;
import org.junit.Test;

public class SimplifyTest {

	@Test
	public void testOrOfEmptySetsStar() {
		RegularExpression regex = new Star(new Or(EmptySet.INSTANCE, EmptySet.INSTANCE));
		assertEquals("", regex.simplify().toString());
	}

	@Test
	public void testCompOfEpsilonAndSomething() {
		RegularExpression regex = new Comp(new Atom(""), new Atom("p"));
		assertEquals("p", regex.simplify().toString());
	}

	@Test
	public void test1() {
		RegularExpression regex = new Comp(new Atom("id="), new Comp(new Star(new Atom("[T];id=")), new Atom("[T];")));
		assertEquals("(id=[T];)*", regex.simplify().toString());
	}

	@Test
	@Ignore // this was made to test a simplification that got removed later
	public void test2() {
		RegularExpression regex = new Comp(new Star(new Atom("[T];id=")), new Atom("[T];id="));
		assertEquals("([T];id=)*", regex.simplify().toString());
	}
}
