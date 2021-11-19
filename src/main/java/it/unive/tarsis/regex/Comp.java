package it.unive.tarsis.regex;

import java.util.HashSet;
import java.util.Set;

import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.strings.ExtString;

/**
 * A regular expression representing the sequential composition of two regular
 * expressions.
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class Comp extends RegularExpression {

	/**
	 * The first regular expression
	 */
	private RegularExpression first;

	/**
	 * The second regular expression
	 */
	private RegularExpression second;

	/**
	 * Builds the comp.
	 * 
	 * @param first  the first regular expression
	 * @param second the second regular expression
	 */
	public Comp(RegularExpression first, RegularExpression second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Yields the first regular expression.
	 * 
	 * @return the first regular expression
	 */
	public RegularExpression getFirst() {
		return first;
	}

	/**
	 * Yields the second regular expression.
	 * 
	 * @return the second regular expression
	 */
	public RegularExpression getSecond() {
		return second;
	}

	@Override
	public String toString() {
		return first.toString() + second.toString();
	}

	@Override
	public int hashCode() {
		return first.hashCode() * second.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Comp) {
			return first.equals(((Comp) other).first) && second.equals(((Comp) other).second);
		}

		return false;
	}

	@Override
	public RegularExpression simplify() {

		RegularExpression first = this.first.simplify();
		RegularExpression second = this.second.simplify();

		RegularExpression result = new Comp(first, second);

		if (first instanceof EmptySet || second instanceof EmptySet)
			result = EmptySet.INSTANCE;
//		else if (first instanceof Atom && second instanceof Atom && !first.asAtom().isEmpty() && !second.asAtom().isEmpty())
//			result = new Atom(first.toString() + second.toString());
		else if (second instanceof Or)
			result = new Or(new Comp(first, second.asOr().getFirst()), new Comp(first, second.asOr().getSecond()));
		else if (first instanceof Atom && second instanceof Or && second.asOr().isAtomic())
			result = new Or(new Atom(first.toString() + second.asOr().getFirst().toString()),
					new Atom(this.first.toString() + second.asOr().getSecond().toString()));
		else if (second instanceof Atom && second.asAtom().isEmpty())
			result = first;
		else if (first instanceof Atom && first.asAtom().isEmpty())
			result = second;
		else if (first instanceof Star && second instanceof Star && second.asStar().getOperand() instanceof Comp
				&& second.asStar().getOperand().asComp().second instanceof Star
				&& second.asStar().getOperand().asComp().second.asStar().getOperand()
						.equals(first.asStar().getOperand()))
			result = new Star(new Or(first.asStar().getOperand(), second.asStar().getOperand().asComp().first));

//		// id=([T];id=)*[T]; => (id=[T];)*
		else if (first instanceof Atom && second instanceof Comp && second.asComp().first instanceof Star
				&& second.asComp().second instanceof Atom
				&& new Atom(second.asComp().second.asAtom().toString() + first.asAtom().toString())
						.equals(second.asComp().first.asStar().getOperand()))
			result = new Star(new Atom(first.asAtom().toString() + second.asComp().second.asAtom().toString()));

//		else if (first instanceof Atom && !first.asAtom().isEmpty() && second instanceof Star && second.asStar().getOperand().equals(first))
//			result = second;
//		else if (second instanceof Atom && !second.asAtom().isEmpty() && first instanceof Star && first.asStar().getOperand().equals(second))
//			result = first;

		else if (first instanceof Star && second instanceof Comp && second.asComp().first instanceof Star
				&& first.asStar().getOperand().equals(second.asComp().first.asStar().getOperand()))
			result = new Comp(first, second.asComp().second);
//
		// (r)*(r)* -> (r)*
		else if (first instanceof Star && second instanceof Star
				&& first.asStar().getOperand().equals(second.asStar().getOperand()))
			result = first;

		return result;
	}

	@Override
	public Automaton toAutomaton() {
		return first.toAutomaton().concat(second.toAutomaton());
	}

	@Override
	public Set<PartialSubstring> substringAux(int charsToSkip, int missingChars) {
		Set<PartialSubstring> result = new HashSet<>();

		Set<PartialSubstring> firstSS = first.substringAux(charsToSkip, missingChars);
		for (PartialSubstring s : firstSS)
			if (s.getMissingChars() == 0)
				result.add(s);
			else {
				Set<PartialSubstring> secondSS = second.substringAux(s.getCharsToStart(),
						s.getCharsToStart() + s.getMissingChars());
				for (PartialSubstring ss : secondSS)
					result.add(s.concat(ss));
			}

		return result;
	}

	@Override
	public boolean isEmpty() {
		return first.isEmpty() && second.isEmpty();
	}

	@Override
	public boolean is(String str) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int maxLength() {
		int first, second;
		if ((first = this.first.maxLength()) == Integer.MAX_VALUE
				|| (second = this.second.maxLength()) == Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		return first + second;
	}

	@Override
	public int minLength() {
		return first.minLength() + second.minLength();
	}

	@Override
	public boolean mayContain(String s) {
		if (first.mayContain(s) || second.mayContain(s))
			return true;

		RegularExpression tmp = topAsEmptyString().unrollStarToFixedLength(s.length());
		Set<ExtString> substrings = tmp.substring(0, tmp.maxLength());
		for (ExtString str : substrings)
			if (str.contains(s))
				return true;

		return false;
	}

	@Override
	public boolean contains(String s) {
		if (first.contains(s) || second.contains(s))
			return true;

		RegularExpression tmp = topAsSingleChar().unrollStarToFixedLength(0);
		Set<ExtString> substrings = tmp.substring(0, tmp.maxLength());
		for (ExtString str : substrings)
			if (!str.contains(s))
				return false;

		return true;
	}

	@Override
	public boolean mayStartWith(String s) {
		if (first.mayStartWith(s))
			return true;

		Set<ExtString> substrings = substring(0, s.length());
		for (ExtString str : substrings)
			if (str.startsWith(s))
				return true;

		return false;
	}

	@Override
	public boolean startsWith(String s) {
		if (first.startsWith(s))
			return true;

		Set<ExtString> substrings = substring(0, s.length());
		for (ExtString str : substrings)
			if (!str.startsWith(s))
				return false;

		return true;
	}

	@Override
	public boolean mayEndWith(String s) {
		if (second.mayEndWith(s))
			return true;

		return reverse().mayStartWith(new StringBuilder(s).reverse().toString());
	}

	@Override
	public boolean endsWith(String s) {
		if (second.endsWith(s))
			return true;

		return reverse().startsWith(new StringBuilder(s).reverse().toString());
	}

	@Override
	protected RegularExpression unrollStarToFixedLength(int length) {
		return new Comp(first.unrollStarToFixedLength(length), second.unrollStarToFixedLength(length));
	}

	@Override
	protected RegularExpression reverse() {
		return new Comp(second.reverse(), first.reverse());
	}

	@Override
	protected RegularExpression topAsEmptyString() {
		return new Comp(first.topAsEmptyString(), second.topAsEmptyString());
	}

	@Override
	protected RegularExpression topAsSingleChar() {
		return new Comp(first.topAsSingleChar(), second.topAsSingleChar());
	}

	@Override
	public RegularExpression[] explode() {
		throw new UnsupportedOperationException();
	}
}
