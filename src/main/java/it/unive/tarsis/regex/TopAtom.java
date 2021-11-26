package it.unive.tarsis.regex;

import java.util.HashSet;
import java.util.Set;

import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.strings.ExtString;

/**
 * A regular expression representing a sequence of unknown characters of
 * arbitrary length.
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class TopAtom extends Atom {

	/**
	 * The string used to represent this regular expression.
	 */
	public static final String STRING = "\u0372";

	/**
	 * The singleton instance.
	 */
	public static final TopAtom INSTANCE = new TopAtom();

	private TopAtom() {
		super(STRING);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof TopAtom;
	}

	@Override
	public Automaton toAutomaton() {
		return Automaton.mkTopAutomaton();
	}

	@Override
	public Set<PartialSubstring> substringAux(int charsToSkip, int missingChars) {
		Set<PartialSubstring> result = new HashSet<>();

		result.add(new PartialSubstring(ExtString.mkEmptyString(), charsToSkip, missingChars - charsToSkip));
		for (int i = 1; i <= missingChars - charsToSkip; i++)
			result.add(new PartialSubstring(ExtString.mkTopString(i), i > charsToSkip ? 0 : charsToSkip - i,
					missingChars - charsToSkip - i));

		return result;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean is(String str) {
		return false;
	}

	@Override
	public int maxLength() {
		return Integer.MAX_VALUE;
	}

	@Override
	public int minLength() {
		return 0;
	}

	@Override
	public boolean mayContain(String s) {
		return true;
	}

	@Override
	public boolean contains(String s) {
		return false;
	}

	@Override
	public boolean mayStartWith(String s) {
		return true;
	}

	@Override
	public boolean startsWith(String s) {
		return false;
	}

	@Override
	public boolean mayEndWith(String s) {
		return true;
	}

	@Override
	public boolean endsWith(String s) {
		return false;
	}

	@Override
	protected RegularExpression topAsSingleChar() {
		return new Atom(STRING) {

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public boolean is(String str) {
				return false;
			}

			@Override
			public int maxLength() {
				return 1;
			}

			@Override
			public int minLength() {
				return 1;
			}

			@Override
			public boolean mayContain(String s) {
				return false;
			}

			@Override
			public boolean contains(String s) {
				return false;
			}

			@Override
			public boolean mayStartWith(String s) {
				return false;
			}

			@Override
			public boolean startsWith(String s) {
				return false;
			}

			@Override
			public boolean mayEndWith(String s) {
				return false;
			}

			@Override
			public boolean endsWith(String s) {
				return false;
			}
		};
	}

	@Override
	protected RegularExpression topAsEmptyString() {
		return Atom.EPSILON;
	}

	@Override
	protected RegularExpression reverse() {
		return this;
	}

	@Override
	public RegularExpression[] explode() {
		return new RegularExpression[] { this };
	}
}
