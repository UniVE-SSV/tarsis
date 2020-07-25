package it.unive.tarsis.test;

import static it.unive.tarsis.strings.ExtString.mkString;
import static it.unive.tarsis.strings.ExtString.mkTopString;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ExtStringTest {

	@Test
	public void testCollapseAtBeginning() {
		assertEquals(mkTopString(5).concat(mkString("a")).collapseTopChars(), mkTopString(1).concat(mkString("a")));
	}

	@Test
	public void testCollapseAtEnding() {
		assertEquals(mkString("a").concat(mkTopString(5)).collapseTopChars(), mkString("a").concat(mkTopString(1)));
	}

	@Test
	public void testCollapseInside() {
		assertEquals(mkString("a").concat(mkTopString(5)).concat(mkString("b")).collapseTopChars(),
				mkString("a").concat(mkTopString(1)).concat(mkString("b")));

		assertEquals(
				mkString("a").concat(mkTopString(5)).concat(mkString("b")).concat(mkTopString(9)).concat(mkString("c"))
						.collapseTopChars(),
				mkString("a").concat(mkTopString(1)).concat(mkString("b")).concat(mkTopString(1))
						.concat(mkString("c")));
	}

	@Test
	public void testCollapseAll() {
		assertEquals(
				mkTopString(5).concat(mkString("a")).concat(mkTopString(5)).concat(mkString("b")).collapseTopChars(),
				mkTopString(1).concat(mkString("a")).concat(mkTopString(1)).concat(mkString("b")));

		assertEquals(
				mkString("a").concat(mkTopString(5)).concat(mkString("b")).concat(mkTopString(9)).concat(mkString("c"))
						.concat(mkTopString(5)).collapseTopChars(),
				mkString("a").concat(mkTopString(1)).concat(mkString("b")).concat(mkTopString(1)).concat(mkString("c"))
						.concat(mkTopString(1)));
	}
}
