package it.unive.tarsis.regex;

import java.util.HashSet;
import java.util.Set;

import it.unive.tarsis.automata.Automata;
import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.strings.ExtString;

/**
 * A regular expression representing a loop, repeated an arbitrary number of
 * times, over an inner regular expression.
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class Star extends RegularExpression {

	private RegularExpression op;

	/**
	 * Builds the star.
	 * 
	 * @param op the inner regular expression
	 */
	public Star(RegularExpression op) {
		this.op = op;
	}

	@Override
	public int hashCode() {
		return op.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Star && ((Star) other).getOperand().equals(op);
	}

	@Override
	public String toString() {
		return "(" + op.toString() + ")*";
	}

	/**
	 * Yields the inner regular expression.
	 * 
	 * @return the inner regular expression
	 */
	public RegularExpression getOperand() {
		return op;
	}

	@Override
	public RegularExpression simplify() {

		RegularExpression op = this.op.simplify();
		RegularExpression result;

		if (op instanceof Atom && op.asAtom().isEmpty())
			result = new Atom("");
		else if (op instanceof EmptySet)
			result = new Atom("");
		else if (op instanceof Star)
			result = op;
		else
			result = new Star(op);

		return result;
	}

	@Override
	public Automaton toAutomaton() {
		return Automata.star(op.toAutomaton());
	}

	@Override
	public Set<PartialSubstring> substringAux(int charsToSkip, int missingChars) {
		Set<PartialSubstring> result = new HashSet<>(), toAdd = new HashSet<>();

		// construction by fixpoint:
		// substring(a*) =
		// fixpoint(substring(emptyset) U substring(a) U substring(aa) U substring(aaa)
		// U ...)

		result.addAll(EmptySet.INSTANCE.substringAux(charsToSkip, missingChars));

		do {
			if (!toAdd.isEmpty()) {
				result.addAll(toAdd);
				toAdd.clear();
			}

			PartialSubstring tmp;
			for (PartialSubstring base : result)
				for (PartialSubstring suffix : op.substringAux(base.getCharsToStart(),
						base.getCharsToStart() + base.getMissingChars())) {
					if (!result.contains(tmp = base.concat(suffix)))
						toAdd.add(tmp);
				}
		} while (!toAdd.isEmpty());

		return result;
	}

	@Override
	public boolean isEmpty() {
		return op.isEmpty();
	}

	@Override
	public boolean is(String str) {
		throw new UnsupportedOperationException();
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
		if (op.mayContain(s))
			return true;

		int repetitions = (s.length() / op.maxLength()) + 2;
		Set<ExtString> substrings = substring(0, repetitions * op.maxLength() + 1);
		for (ExtString str : substrings)
			if (str.contains(s))
				return true;

		return false;
	}

	@Override
	public boolean contains(String s) {
		return false;
	}

	@Override
	public boolean mayStartWith(String s) {
		if (op.mayStartWith(s))
			return true;

		int repetitions = (s.length() / op.maxLength()) + 2;
		Set<ExtString> substrings = substring(0, repetitions * op.maxLength() + 1);
		for (ExtString str : substrings)
			if (str.startsWith(s))
				return true;

		return false;
	}

	@Override
	public boolean startsWith(String s) {
		return false;
	}

	@Override
	public boolean mayEndWith(String s) {
		if (op.mayEndWith(s))
			return true;

		int repetitions = (s.length() / op.maxLength()) + 2;
		Set<ExtString> substrings = substring(0, repetitions * op.maxLength() + 1);
		for (ExtString str : substrings)
			if (str.endsWith(s))
				return true;

		return false;
	}

	@Override
	public boolean endsWith(String s) {
		return false;
	}

	@Override
	protected RegularExpression unrollStarToFixedLength(int length) {
		if (length == 0)
			return new Atom("");

		int repetitions = (length / op.maxLength()) + 2;
		RegularExpression result = null;

		while (repetitions > 0) {
			result = result == null ? op : result.concat(op);
			repetitions--;
		}

		return result;
	}

	@Override
	protected RegularExpression reverse() {
		return new Star(op.reverse());
	}

	@Override
	protected RegularExpression topAsEmptyString() {
		return new Star(op.topAsEmptyString());
	}

	@Override
	protected RegularExpression topAsSingleChar() {
		return new Star(op.topAsSingleChar());
	}

	@Override
	public RegularExpression[] explode() {
		throw new UnsupportedOperationException();
	}
}
