package it.unive.tarsis.regex;

import it.unive.tarsis.automata.Automata;
import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.strings.ExtString;
import java.util.HashSet;
import java.util.Set;

/**
 * A regular expression representing a single string.
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class Atom extends RegularExpression {

	private String string;

	/**
	 * Builds the atom.
	 * 
	 * @param s the string to be represented by this atom
	 */
	public Atom(String s) {
		this.string = s;
	}

	@Override
	public String toString() {
		return string;
	}

	@Override
	public int hashCode() {
		return string.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Atom && string.equals(((Atom) other).string);
	}

	@Override
	public RegularExpression simplify() {
		return this;
	}

	@Override
	public Automaton toAutomaton() {
		return isEmpty() ? Automata.mkEmptyString() : Automata.mkAutomaton(string);
	}

	@Override
	public Set<PartialSubstring> substringAux(int charsToSkip, int missingChars) {
		Set<PartialSubstring> result = new HashSet<>();

		if (charsToSkip > string.length())
			// outside of the string
			result.add(new PartialSubstring(ExtString.mkEmptyString(), charsToSkip - string.length(),
					missingChars - charsToSkip));
		else if (missingChars > string.length())
			// partially inside the string
			result.add(new PartialSubstring(ExtString.mkString(string.substring(charsToSkip)), 0,
					missingChars - string.length()));
		else
			result.add(new PartialSubstring(ExtString.mkString(string.substring(charsToSkip, missingChars)), 0, 0));

		return result;
	}

	@Override
	public boolean isEmpty() {
		return string.isEmpty();
	}

	@Override
	public boolean is(String str) {
		return string.equals(str);
	}

	@Override
	public int maxLength() {
		return string.length();
	}

	@Override
	public int minLength() {
		return maxLength();
	}

	@Override
	public boolean mayContain(String s) {
		return contains(s);
	}

	@Override
	public boolean contains(String s) {
		return string.contains(s);
	}

	@Override
	public boolean mayStartWith(String s) {
		return startsWith(s);
	}

	@Override
	public boolean startsWith(String s) {
		return string.startsWith(s);
	}

	@Override
	public boolean mayEndWith(String s) {
		return endsWith(s);
	}

	@Override
	public boolean endsWith(String s) {
		return string.endsWith(s);
	}

	@Override
	protected RegularExpression unrollStarToFixedLength(int length) {
		return this;
	}

	@Override
	protected RegularExpression reverse() {
		return new Atom(new StringBuilder(string).reverse().toString());
	}

	@Override
	protected RegularExpression topAsEmptyString() {
		return this;
	}

	@Override
	protected RegularExpression topAsSingleChar() {
		return this;
	}

	@Override
	public RegularExpression[] explode() {
		return string.chars().mapToObj(ch -> String.valueOf((char) ch)).map(Atom::new)
				.toArray(RegularExpression[]::new);
	}
}
