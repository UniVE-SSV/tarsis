package it.unive.tarsis.regex;

import java.util.HashSet;
import java.util.Set;

import it.unive.tarsis.automata.Automaton;

/**
 * A regular expression representing an or between two other regular
 * expressions.
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class Or extends RegularExpression {

	/**
	 * The first regular expression
	 */
	private RegularExpression first;

	/**
	 * The second regular expression
	 */
	private RegularExpression second;

	/**
	 * Builds the or.
	 * 
	 * @param first  the first regular expression
	 * @param second the second regular expression
	 */
	public Or(RegularExpression first, RegularExpression second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Yields the second regular expression.
	 * 
	 * @return the second regular expression
	 */
	public RegularExpression getSecond() {
		return second;
	}

	/**
	 * Yields the first regular expression.
	 * 
	 * @return the first regular expression
	 */
	public RegularExpression getFirst() {
		return first;
	}

	@Override
	public String toString() {
		return "(" + first.toString() + " + " + second.toString() + ")";
	}

	/**
	 * Yields {@code true} if and only if both inner regular expressions are
	 * atoms.
	 * 
	 * @return {@code true} if that condition holds
	 */
	public boolean isAtomic() {
		return first instanceof Atom && second instanceof Atom;
	}

	@Override
	public int hashCode() {
		return first.hashCode() + second.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Or) {
			return (first.equals(((Or) other).first) && second.equals(((Or) other).second))
					|| (first.equals(((Or) other).second) && second.equals(((Or) other).first));
		}
		return false;

	}

	@Override
	public RegularExpression simplify() {

		RegularExpression first = this.first.simplify();
		RegularExpression second = this.second.simplify();
		RegularExpression result = new Or(first, second);

		if (first.equals(second))
			return first;
		if (first instanceof EmptySet)
			result = second;
		else if (second instanceof EmptySet)
			result = first;
		else if (first instanceof Atom && first.asAtom().isEmpty() && second instanceof Atom
				&& second.asAtom().isEmpty())
			result = new Atom("");
		else if (first instanceof Atom && first.asAtom().isEmpty() && second instanceof Star)
			result = second;
		else if (second instanceof Atom && second.asAtom().isEmpty() && first instanceof Star)
			result = first;

		// "" + ee* => e*
		else if (first.equals(new Atom("")) && second instanceof Comp && second.asComp().getSecond() instanceof Star
				&& second.asComp().getFirst().equals(second.asComp().getSecond().asStar().getOperand()))
			result = new Star(second.asComp().getFirst());
		// "" + e*e => e*
		else if (first.equals(new Atom("")) && second instanceof Comp && second.asComp().getFirst() instanceof Star
				&& second.asComp().getSecond().equals(second.asComp().getFirst().asStar().getOperand()))
			result = new Star(second.asComp().getFirst());

//		else if (first instanceof Atom && !first.asAtom().isEmpty() && second.endsWith(first.asAtom()))
//			result = new Comp(second.removeSuffix(first.asAtom()), first);
//		else if (second instanceof Atom && !second.asAtom().isEmpty() && first.endsWith(second.asAtom()))
//			result = new Comp(first.removeSuffix(second.asAtom()), second);

		// this is a common situation
		// that yields to an ugly representation of the string
		// a(b + c)* + a((b + c)*b + (b + c)*c)(b + c)*
		// this is equivalent to a(b + c)*
		// IMPORTANT!! keep this as the last case
		else if (first instanceof Comp && first.asComp().getSecond() instanceof Star
				&& first.asComp().getSecond().asStar().getOperand() instanceof Or) {
			RegularExpression a = first.asComp().getFirst();
			Star bORcSTAR = first.asComp().getSecond().asStar();
			RegularExpression b = bORcSTAR.getOperand().asOr().getFirst();
			RegularExpression c = bORcSTAR.getOperand().asOr().getSecond();

			if (second instanceof Comp && second.asComp().getFirst().equals(a)
					&& second.asComp().getSecond() instanceof Comp
					&& second.asComp().getSecond().asComp().getSecond().equals(bORcSTAR)
					&& second.asComp().getSecond().asComp().getFirst() instanceof Or) {
				Or or = second.asComp().getSecond().asComp().getFirst().asOr();
				if (or.getFirst() instanceof Comp && or.getFirst().asComp().getFirst().equals(bORcSTAR)
						&& or.getFirst().asComp().getSecond().equals(b) && or.getSecond() instanceof Comp
						&& or.getSecond().asComp().getFirst().equals(bORcSTAR)
						&& or.getSecond().asComp().getSecond().equals(c)) {
					result = first;
				}
			}
		}

		return result;
	}

	@Override
	public Automaton toAutomaton() {
		return first.toAutomaton().union(second.toAutomaton());
	}

	@Override
	public Set<PartialSubstring> substringAux(int charsToSkip, int missingChars) {
		Set<PartialSubstring> result = new HashSet<>();
		result.addAll(first.substringAux(charsToSkip, missingChars));
		result.addAll(second.substringAux(charsToSkip, missingChars));
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
		return Math.max(first.maxLength(), second.maxLength());
	}

	@Override
	public int minLength() {
		return Math.min(first.minLength(), second.minLength());
	}

	@Override
	public boolean mayContain(String s) {
		return first.contains(s) || second.contains(s);
	}

	@Override
	public boolean contains(String s) {
		return first.contains(s) && second.contains(s);
	}

	@Override
	public boolean mayStartWith(String s) {
		return first.endsWith(s) || second.endsWith(s);
	}

	@Override
	public boolean startsWith(String s) {
		return first.endsWith(s) && second.endsWith(s);
	}

	@Override
	public boolean mayEndWith(String s) {
		return first.endsWith(s) || second.endsWith(s);
	}

	@Override
	public boolean endsWith(String s) {
		return first.endsWith(s) && second.endsWith(s);
	}

	@Override
	protected RegularExpression unrollStarToFixedLength(int length) {
		return new Or(first.unrollStarToFixedLength(length), second.unrollStarToFixedLength(length));
	}

	@Override
	protected RegularExpression reverse() {
		return new Or(first.reverse(), second.reverse());
	}

	@Override
	protected RegularExpression topAsEmptyString() {
		return new Or(first.topAsEmptyString(), second.topAsEmptyString());
	}

	@Override
	protected RegularExpression topAsSingleChar() {
		return new Or(first.topAsSingleChar(), second.topAsSingleChar());
	}

	@Override
	public RegularExpression[] explode() {
		throw new UnsupportedOperationException();
	}
}
