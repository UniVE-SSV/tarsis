package it.unive.tarsis.regex;

import it.unive.tarsis.automata.Automata;
import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.strings.ExtString;
import java.util.HashSet;
import java.util.Set;

/**
 * A regular expression representing the empty set of strings.
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class EmptySet extends RegularExpression {

	/**
	 * The singleton instance.
	 */
	public static final EmptySet INSTANCE = new EmptySet();

	private EmptySet() {
	}

	@Override
	public RegularExpression simplify() {
		return new EmptySet();
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof EmptySet;
	}

	@Override
	public String toString() {
		return "∅";
	}

	@Override
	public Automaton toAutomaton() {
		return Automata.mkEmptyLanguage();
	}

	@Override
	protected Set<PartialSubstring> substringAux(int charsToSkip, int missingChars) {
		Set<PartialSubstring> result = new HashSet<>();
		result.add(new PartialSubstring(ExtString.mkEmptyString(), charsToSkip, missingChars - charsToSkip));
		return result;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public boolean is(String str) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int maxLength() {
		return 0;
	}

	@Override
	public int minLength() {
		return 0;
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

	@Override
	protected RegularExpression unrollStarToFixedLength(int length) {
		return this;
	}

	@Override
	protected RegularExpression reverse() {
		return this;
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
		throw new UnsupportedOperationException();
	}
}
