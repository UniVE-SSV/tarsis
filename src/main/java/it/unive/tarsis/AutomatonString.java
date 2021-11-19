package it.unive.tarsis;

import static it.unive.tarsis.automata.algorithms.RegexExtractor.getMinimalBrzozowskiRegex;

import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.automata.State;
import it.unive.tarsis.automata.Transition;
import it.unive.tarsis.automata.algorithms.IndexFinder;
import it.unive.tarsis.regex.RegularExpression;
import it.unive.tarsis.regex.TopAtom;
import it.unive.tarsis.strings.ExtString;

/**
 * A string modeled through the Tarsis abstract domain.
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class AutomatonString {

	/**
	 * Maximum widening threshold, or default threshold if there is no
	 * difference in the size of the two automata.
	 */
	public static final int WIDENING_CAP = 5;

	/**
	 * The automaton representing the string
	 */
	private final Automaton automaton;

	/**
	 * The regular expression corresponding to this string. This is lazily
	 * computed, thus always use {@link #getRegex()} to access it.
	 */
	private RegularExpression regex;

	/**
	 * Builds a new automaton string recognizing the top string.
	 */
	public AutomatonString() {
		this(Automaton.mkTopAutomaton());
	}

	/**
	 * Builds a new automaton string recognizing the given string.
	 * 
	 * @param literal the string
	 */
	public AutomatonString(String literal) {
		this(Automaton.mkAutomaton(literal));
	}

	/**
	 * Builds a new automaton string recognizing the given string set.
	 * 
	 * @param lits strings
	 */
	public AutomatonString(String... lits) {
		Automaton a = Automaton.mkEmptyLanguage();

		for (String s : lits)
			a = a.union(Automaton.mkAutomaton(s));

		this.automaton = a;
	}

	/**
	 * Creates a new automaton string with the given automaton.
	 * 
	 * @param automaton the automaton
	 */
	public AutomatonString(Automaton automaton) {
		this.automaton = automaton;
	}

	/**
	 * Yields the size of this string, that is, the number of states of the
	 * underlying automaton.
	 * 
	 * @return the size of this string
	 */
	public int size() {
		return automaton.getStates().size();
	}

	/**
	 * Yields the regular expression that is equivalent to the automaton
	 * underlying this string. The regular expression is computed the first time
	 * that this method is invoked.
	 * 
	 * @return the regular expression
	 */
	public RegularExpression getRegex() {
		if (regex == null)
			this.regex = getMinimalBrzozowskiRegex(automaton);
		return regex;
	}

	/**
	 * Yields the automaton underlying this string.
	 * 
	 * @return the automaton
	 */
	public Automaton getAutomaton() {
		return automaton;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((automaton == null) ? 0 : automaton.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AutomatonString other = (AutomatonString) obj;
		if (automaton == null) {
			if (other.automaton != null)
				return false;
		} else if (!automaton.equals(other.automaton))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getRegex().toString();
	}

	/**
	 * Performs the least upper bound between this string and the given one,
	 * without simplifying (i.e., determinizing and minimizing) the result.
	 * 
	 * @param other the other string
	 * 
	 * @return the least upper bound
	 */
	public AutomatonString lub(AutomatonString other) {
		return lub(other, true);
	}

	/**
	 * Performs the least upper bound between this string and the given one.
	 * 
	 * @param other    the other string
	 * @param simplify if true, the result will be simplified (i.e.,
	 *                     determinized and minimized)
	 * 
	 * @return the least upper bound
	 */
	public AutomatonString lub(AutomatonString other, boolean simplify) {
		Automaton union = automaton.union(other.automaton);

		if (simplify)
			union = union.minimize();

		return new AutomatonString(union);
	}

	/**
	 * Performs the widening between this string and the given one, without
	 * simplifying (i.e., determinizing and minimizing) the result, and by
	 * automatically determining the threshold parameter. Such parameter will be
	 * the difference in size between the two underlying automata, capped at
	 * {@link #WIDENING_CAP}. If the two automata have the same size, the
	 * threshold will be set to {@link #WIDENING_CAP}.
	 * 
	 * @param other the other string
	 * 
	 * @return the widened string
	 */
	public AutomatonString widen(AutomatonString other) {
		return widen(other, getSizeDiffCapped(other), false);
	}

	/**
	 * Performs the widening between this string and the given one, without
	 * simplifying (i.e., determinizing and minimizing) the result, and with the
	 * given widening threshold.
	 * 
	 * @param other             the other string
	 * @param wideningThreshold the threshold parameter of the widening
	 *                              operation
	 * 
	 * @return the widened string
	 */
	public AutomatonString widen(AutomatonString other, int wideningThreshold) {
		return widen(other, wideningThreshold, false);
	}

	/**
	 * Performs the widening between this string and the given one, by
	 * automatically determining the threshold parameter. Such parameter will be
	 * the difference in size between the two underlying automata, capped at
	 * {@link #WIDENING_CAP}. If the two automata have the same size, the
	 * threshold will be set to {@link #WIDENING_CAP}.
	 * 
	 * @param other    the other string
	 * @param simplify if true, the result will be simplified (i.e.,
	 *                     determinized and minimized)
	 * 
	 * @return the widened string
	 */
	public AutomatonString widen(AutomatonString other, boolean simplify) {
		return widen(other, getSizeDiffCapped(other), simplify);
	}

	private int getSizeDiffCapped(AutomatonString other) {
		int size = size();
		int otherSize = other.size();
		if (size > otherSize)
			return Math.min(size - otherSize, WIDENING_CAP);
		else if (size < otherSize)
			return Math.min(otherSize - size, WIDENING_CAP);
		else
			return WIDENING_CAP;
	}

	/**
	 * Performs the widening between this string and the given one.
	 * 
	 * @param other             the other string
	 * @param wideningThreshold the threshold parameter of the widening
	 *                              operation
	 * @param simplify          if true, the result will be simplified (i.e.,
	 *                              determinized and minimized)
	 * 
	 * @return the widened string
	 */
	public AutomatonString widen(AutomatonString other, int wideningThreshold, boolean simplify) {
		Automaton widened = automaton.union(other.automaton).widening(wideningThreshold);

		if (simplify)
			widened = widened.minimize();

		return new AutomatonString(widened);
	}

	/**
	 * An interval representing the result of a computation on an
	 * {@link AutomatonString}. The lower endpoint is <i>always</i> finite,
	 * while the upper endpoint might not be finite. If it is not finite,
	 * {@link #topIsInfinity()} will return {@code true}, and the value returned
	 * by {@link #getUpper()} can be ignored.
	 * 
	 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
	 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
	 */
	public static class Interval {

		/**
		 * The lower endpoint
		 */
		private int lower;

		/**
		 * The upper endpoint
		 */
		private int upper;

		/**
		 * True if and only if the upper endpoint is infinite
		 */
		private boolean topIsInfinity;

		/**
		 * Builds the interval.
		 * 
		 * @param lower         the lower endpoint
		 * @param upper         the upper endpoint
		 * @param topIsInfinity true if and only if the upper endpoint is
		 *                          infinite
		 */
		public Interval(int lower, int upper, boolean topIsInfinity) {
			this.lower = lower;
			this.upper = upper;
			this.topIsInfinity = topIsInfinity;
		}

		/**
		 * Yields the lower endpoint of this interval. It is always finite.
		 * 
		 * @return the lower endpoint
		 */
		public int getLower() {
			return lower;
		}

		/**
		 * Yields the upper endpoint of this interval. Since the upper endpoint
		 * might not be finite, always query {@link #topIsInfinity()} before
		 * this method to know if the value returned by this method is useless.
		 * 
		 * @return the upper endpoint
		 */
		public int getUpper() {
			return upper;
		}

		/**
		 * Yields {@code true} if and only if the upper endpoint of this
		 * interval is not finite. If this method returns {@code true}, then the
		 * value returned by {@link #getUpper()} is meaningless.
		 * 
		 * @return {@code true} if that condition holds
		 */
		public boolean topIsInfinity() {
			return topIsInfinity;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + lower;
			result = prime * result + (topIsInfinity ? 1231 : 1237);
			result = prime * result + upper;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Interval other = (Interval) obj;
			if (lower != other.lower)
				return false;
			if (topIsInfinity != other.topIsInfinity)
				return false;
			if (!topIsInfinity && upper != other.upper)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "[" + lower + ", " + (topIsInfinity ? "∞" : upper) + "]";
		}
	}

	/**
	 * Yields an {@link Interval} containing all possible lengths of the strings
	 * modeled by this automaton string. If there is at least a cycle in the
	 * underlying automaton, or if such automaton accepts the top string at
	 * least on one transition, then the upper endpoint of the returned interval
	 * will be infinite.
	 * 
	 * @return the length, as an interval
	 */
	public Interval length() {
		return new Interval(getRegex().minLength(), getRegex().maxLength(),
				automaton.hasCycle() || automaton.acceptsTopEventually());
	}

	/**
	 * Joins this string with the given one.
	 * 
	 * @param other the other string
	 * 
	 * @return the joined string
	 */
	public AutomatonString concat(AutomatonString other) {
		return new AutomatonString(automaton.concat(other.automaton));
	}

	/**
	 * Yields an automaton string modeling all possible substrings of this
	 * automaton string, starting at {@code start} (inclusive) and ending at
	 * {@code end} (exclusive).
	 * 
	 * @param start the start index of the substring
	 * @param end   the end index of the substring
	 * 
	 * @return an automaton string modeling all possible substrings
	 */
	public AutomatonString substring(int start, int end) {
		Automaton[] array = allSubstrings(start, end)
				.parallelStream()
				.map(s -> Automaton.mkAutomaton(s))
				.toArray(Automaton[]::new);
		Automaton result = Automaton.union(array);
		result = result.minimize();
		return new AutomatonString(result);
	}

	private Collection<ExtString> allSubstrings(int start, int end) {
		return getRegex().substring(start, end);
	}

	/**
	 * Replaces all occurrences of {@code toReplace} into this string with
	 * {@code str}. If {@code toReplace} is never contained into this string,
	 * then this string is returned. If the automaton underlying this string has
	 * a cycle, or if the the automaton underlying {@code toReplace} has either
	 * a cycle or a transition accepting the top string, then this method
	 * returns an automaton string recognizing the top string. Otherwise, the
	 * replaced automaton string is returned.
	 * 
	 * @param toReplace the string to replace
	 * @param str       the string to use as replacement
	 * 
	 * @return the replaced string
	 */
	public AutomatonString replace(AutomatonString toReplace, AutomatonString str) {
		if (!contains(toReplace))
			return this;

		if (automaton.hasCycle() || toReplace.automaton.hasCycle() || toReplace.automaton.acceptsTopEventually())
			return new AutomatonString();

		return new AutomatonString(automaton.replace(toReplace.automaton.explode(), str.automaton));
	}

	/**
	 * Yields {@code true} if this string is equal to the given one. Being equal
	 * means that the two underlying automata are equal, that is, that the
	 * automaton underlying this string is contained into the automaton
	 * underlying the other string, and vice versa.
	 * 
	 * @param other the other string
	 * 
	 * @return {@code true} if that condition holds
	 */
	public boolean isEqualTo(AutomatonString other) {
		return automaton.equals(other.automaton);
	}

	/**
	 * Yields the {@link Interval} containing all possible indexes where
	 * {@code string} first appears into this string. If {@code string} is not
	 * contained into this string, then {@code [-1,-1]} is returned. Moreover,
	 * if the automaton underlying this string has a cycle, or if the automaton
	 * underlying {@code string} has a cycle or has at least one transition
	 * accepting the top string, then {@code [-1,infinity]} is returned.
	 * Otherwise, an interval {@code [i,j]} is returned, where {@code i} is the
	 * smallest (and {@code j} is the biggest} index such that there exist a
	 * concrete string {@code s} modeled by this automaton string and a concrete
	 * string {@code ss} modeled by {@code string} such that
	 * {@code s.indexOf(ss) == i} (and {@code s.indexOf(ss) == j},
	 * respectively).
	 * 
	 * @param string the other string
	 * 
	 * @return an interval containing the indexes of the first occurrences of
	 *             {@code string} into this string
	 */
	public Interval indexOf(AutomatonString string) {
		if (!contains(string))
			return new Interval(-1, -1, false);

		if (automaton.hasCycle() || string.automaton.hasCycle() || string.automaton.acceptsTopEventually())
			return new Interval(-1, Integer.MAX_VALUE, true);

		Pair<Integer, Integer> interval = IndexFinder.findIndexesOf(automaton, string.automaton);
		boolean inf = interval.getRight() == null;
		return new Interval(interval.getLeft(), inf ? Integer.MAX_VALUE : interval.getRight(), inf);
	}

	// ------------------------------------------------------------------------------------------------------------
	// MUST OPERATIONS
	// --------------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Yields {@code true} if and only if {@code other} is <b>surely</b>
	 * contained into this string. This means that:
	 * <ul>
	 * <li>if both strings are finite (i.e., no loops or top transitions in the
	 * underlying automata), every concrete string modeled by {@code other} is
	 * contained into every concrete string modeled by this string, according to
	 * the semantic of {@link String#contains(CharSequence)}</li>
	 * <li>otherwise, if {@code other} has only one path, according to
	 * {@link Automaton#hasOnlyOnePath()}, then the longest concrete string
	 * recognized by {@code other} is contained into all paths of the automaton
	 * underlying this string, excluding SCCs</li>
	 * </ul>
	 * If none of the above conditions hold, this method returns {@code false}.
	 * 
	 * @param other the other string
	 * 
	 * @return {@code true} if that condition holds
	 */
	public boolean contains(AutomatonString other) {
		return mustOperation(other, String::contains,
				(a1, a2) -> other.automaton.explode().mustBeContained(automaton));
	}

	/**
	 * Yields {@code true} if and only if {@code other} is <b>surely</b> a
	 * prefix of this string. This means that:
	 * <ul>
	 * <li>if both strings are finite (i.e., no loops or top transitions in the
	 * underlying automata), every concrete string modeled by {@code other} is a
	 * prefix of every concrete string modeled by this string, according to the
	 * semantic of {@link String#startsWith(String)}</li>
	 * <li>otherwise, if {@code other} has only one path, according to
	 * {@link Automaton#hasOnlyOnePath()}, then the automaton recognizing the
	 * longest concrete string recognized by {@code other} is equal to the
	 * automaton recognizing all possible substrings of this automaton, from
	 * {@code 0} to the length of the longest string</li>
	 * </ul>
	 * If none of the above conditions hold, this method returns {@code false}.
	 * 
	 * @param other the other string
	 * 
	 * @return {@code true} if that condition holds
	 */
	public boolean startsWith(AutomatonString other) {
		return mustOperation(other, String::startsWith, AutomatonString::automatonStartsWith);
	}

	/**
	 * Yields {@code true} if and only if {@code other} is <b>surely</b> a
	 * suffix of this string. This means that:
	 * <ul>
	 * <li>if both strings are finite (i.e., no loops or top transitions in the
	 * underlying automata), every concrete string modeled by {@code other} is a
	 * suffix of every concrete string modeled by this string, according to the
	 * semantic of {@link String#startsWith(String)}</li>
	 * <li>otherwise, this string and {@code other} must satisfy the second
	 * condition of {@link #startsWith(AutomatonString)} after being
	 * reversed</li>
	 * </ul>
	 * If none of the above conditions hold, this method returns {@code false}.
	 * 
	 * @param other the other string
	 * 
	 * @return {@code true} if that condition holds
	 */
	public boolean endsWith(AutomatonString other) {
		return mustOperation(other, String::endsWith, (a1, a2) -> {
			AutomatonString thisReversed = new AutomatonString(a1.automaton.reverse());
			AutomatonString otherReversed = new AutomatonString(a2.automaton.reverse());
			return thisReversed.automatonStartsWith(otherReversed);
		});
	}

	private boolean automatonStartsWith(AutomatonString other) {
		if (!automaton.hasCycle())
			return automaton.mustLanguageCheck(other.automaton, String::startsWith);

		Automaton explode = other.automaton.explode();
		if (explode.hasOnlyOnePath()) {
			Automaton C = explode.extractLongestString();
			Automaton B = substring(0, explode.maxLengthString()).automaton;
			B = B.minimize();

			if (B.equals(C))
				return true;
		}

		return false;
	}

	private boolean mustOperation(AutomatonString other, BiPredicate<String, String> languageComparer,
			BiPredicate<AutomatonString, AutomatonString> automataComparer) {
		if (other.automaton.hasCycle())
			// either this does not have a cycle
			// or they both have a cycle but we cannot enforce
			// that those are iterated the same number of times
			return false;

		if (other.automaton.getLanguage().isEmpty())
			// the empty string is always contained
			return true;

		if (other.automaton.acceptsTopEventually())
			return false;

		if (!automaton.hasCycle() && !automaton.acceptsTopEventually())
			return automaton.mustLanguageCheck(other.automaton, languageComparer);

		return automataComparer.test(this, other);
	}

	// ------------------------------------------------------------------------------------------------------------
	// MAY OPERATIONS
	// ---------------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Yields {@code true} if this string <b>might</b> be equal to the given
	 * one. This means that:
	 * <ul>
	 * <li>if both strings are finite (i.e., no loops or top transitions in the
	 * underlying automata), at least one concrete string modeled by
	 * {@code other} is equal to at least one concrete string modeled by this
	 * string, according to the semantic of {@link String#equals(Object)}</li>
	 * <li>otherwise, the intersection between the automaton underlying this
	 * string and the one underlying {@code other} must not be empty</li>
	 * </ul>
	 * If none of the above conditions hold, this method returns {@code false}.
	 * 
	 * @param other the other string
	 * 
	 * @return {@code true} if that condition holds
	 */
	public boolean mayBeEqualTo(AutomatonString other) {
		boolean top = false;
		// TODO fix this with handling the top element
		return mayOperation(other, top, String::equals, a -> a);
	}

	/**
	 * Yields {@code true} if and only if {@code other} <b>might</b> be a suffix
	 * of this string. This means that:
	 * <ul>
	 * <li>if both strings are finite (i.e., no loops or top transitions in the
	 * underlying automata), at least one concrete string modeled by
	 * {@code other} is a suffix of at least one concrete string modeled by this
	 * string, according to the semantic of {@link String#endsWith(String)}</li>
	 * <li>otherwise, the intersection between the suffix automaton (according
	 * to {@link Automaton#suffix()} of the automaton underlying this string and
	 * the one underlying {@code other} must not be empty</li>
	 * </ul>
	 * If none of the above conditions hold, this method returns {@code false}.
	 * 
	 * @param other the other string
	 * 
	 * @return {@code true} if that condition holds
	 */
	public boolean mayEndWith(AutomatonString other) {
		boolean top = false;
		outer: for (State f : automaton.getFinalStates())
			for (Transition t : automaton.getOutgoingTransitionsFrom(f))
				if (t.getInput() == TopAtom.INSTANCE) {
					top = true;
					break outer;
				}

		return mayOperation(other, top, String::endsWith, Automaton::suffix);
	}

	/**
	 * Yields {@code true} if and only if {@code other} <b>might</b> be
	 * contained in this string. This means that:
	 * <ul>
	 * <li>if both strings are finite (i.e., no loops or top transitions in the
	 * underlying automata), at least one concrete string modeled by
	 * {@code other} is contained into at least one concrete string modeled by
	 * this string, according to the semantic of
	 * {@link String#contains(CharSequence)}</li>
	 * <li>otherwise, the intersection between the factors automaton (according
	 * to {@link Automaton#factors()} of the automaton underlying this string
	 * and the one underlying {@code other} must not be empty</li>
	 * </ul>
	 * If none of the above conditions hold, this method returns {@code false}.
	 * 
	 * @param other the other string
	 * 
	 * @return {@code true} if that condition holds
	 */
	public boolean mayContain(AutomatonString other) {
		return mayOperation(other, automaton.acceptsTopEventually(), String::contains, Automaton::factors);
	}

	/**
	 * Yields {@code true} if and only if {@code other} <b>might</b> be a prefix
	 * of this string. This means that:
	 * <ul>
	 * <li>if both strings are finite (i.e., no loops or top transitions in the
	 * underlying automata), at least one concrete string modeled by
	 * {@code other} is a prefix of at least one concrete string modeled by this
	 * string, according to the semantic of
	 * {@link String#startsWith(String)}</li>
	 * <li>otherwise, the intersection between the prefix automaton (according
	 * to {@link Automaton#prefix()} of the automaton underlying this string and
	 * the one underlying {@code other} must not be empty</li>
	 * </ul>
	 * If none of the above conditions hold, this method returns {@code false}.
	 * 
	 * @param other the other string
	 * 
	 * @return {@code true} if that condition holds
	 */
	public boolean mayStartWith(AutomatonString other) {
		boolean top = false;
		for (Transition t : automaton.getOutgoingTransitionsFrom(automaton.getInitialState()))
			if (t.getInput() == TopAtom.INSTANCE) {
				top = true;
				break;
			}

		return mayOperation(other, top, String::startsWith, Automaton::prefix);
	}

	private boolean mayOperation(AutomatonString other, boolean okWithTop,
			BiPredicate<String, String> languageComparer, Function<Automaton, Automaton> automataTransformer) {
		if (!automaton.hasCycle() && !other.automaton.hasCycle() && !automaton.acceptsTopEventually()
				&& !other.automaton.acceptsTopEventually())
			return automaton.mayLanguageCheck(other.automaton, languageComparer);

		if (okWithTop)
			return true;

		Automaton transformed = automataTransformer.apply(automaton.explode());
		Automaton otherExploded = other.automaton.explode();
		return !otherExploded.intersection(transformed).isEmptyLanguageAccepted();
	}
}
