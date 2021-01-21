package it.unive.tarsis.automata;

import it.unive.tarsis.automata.algorithms.StringReplacer;
import it.unive.tarsis.regex.Atom;
import it.unive.tarsis.regex.RegularExpression;
import it.unive.tarsis.regex.TopAtom;
import it.unive.tarsis.strings.ExtChar;
import it.unive.tarsis.strings.ExtString;
import it.unive.tarsis.strings.TopExtChar;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for managing instances of {@link Automaton}.
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class Automata {

	/**
	 * Yields {@code true} if and only if {@code first} is <b>always</b>
	 * contained into {@code second}, that is, if {@code first} is a single path
	 * automaton and its longest string is contained in all paths (without
	 * SCCs).
	 * 
	 * @param first  the sub-automaton
	 * @param second the super-automaton
	 * 
	 * @return {@code true} if that condition holds
	 */
	public static boolean mustBeContained(Automaton first, Automaton second) {
		if (first.hasOnlyOnePath()) {
			Automaton C = extractLongestString(first);
			String longest = C.getLanguage().iterator().next();

			List<List<State>> paths = second.getPathExtractor().getAllPaths().parallelStream()
					.filter(p -> p.stream().distinct().collect(Collectors.toList()).equals(p))
					.collect(Collectors.toList());

			Set<State> states = new HashSet<>();
			Set<Transition> delta = new HashSet<>();
			for (List<State> p : paths)
				for (State s : p) {
					states.add(s);
					for (Transition t : second.getOutgoingTransitionsFrom(s))
						if (p.contains(t.getTo()) && !states.contains(t.getTo()))
							// we consider only forward transitions
							// this condition eliminates also self loops
							delta.add(t);
				}

			Automaton withNoScc = new Automaton(delta, states);
			for (String a : withNoScc.getLanguage())
				if (!a.contains(longest))
					return false;

			return true;

		}

		return false;
	}

	/**
	 * Yields {@code true} if and only if {@code first} is contained into
	 * {@code second}, that is, if the language recognized by the intersection
	 * between {@code first} and the complement of {@code second} is empty.
	 * 
	 * @param first  the sub-automaton
	 * @param second the super-automaton
	 * 
	 * @return {@code true} if that condition holds
	 */
	public static boolean isContained(Automaton first, Automaton second) {
		return isEmptyLanguageAccepted(intersection(first, complement(second, alphabetUnion(first, second))));
	}

	/**
	 * Yields the automaton that recognizes all possible prefixes of the strings
	 * recognized by the given automaton.
	 * 
	 * @param automaton the original automaton
	 * 
	 * @return the prefix automaton
	 */
	public static Automaton prefix(Automaton automaton) {
		Automaton result = automaton.clone();

		for (State s : result.getStates())
			s.setFinalState(true);

		return minimize(result);
	}

	/**
	 * Yields the automaton that recognizes all possible suffixes of the strings
	 * recognized by the given automaton.
	 * 
	 * @param automaton the original automaton
	 * 
	 * @return the suffix automaton
	 */
	public static Automaton suffix(Automaton automaton) {
		Automaton result = automaton.clone();

		for (State s : result.getStates())
			s.setInitialState(true);

		return minimize(result);
	}

	/**
	 * Yields the automaton that recognizes all possible substrings of the
	 * strings recognized by the given automaton.
	 * 
	 * @param a the original automaton
	 * 
	 * @return the factors automaton
	 */
	public static Automaton factors(Automaton a) {
		return suffix(prefix(a));
	}

	/**
	 * Yields the set of regular expressions that represent the union of the
	 * alphabets of the two given automata.
	 * 
	 * @param first  the first automaton
	 * @param second the second automaton
	 * 
	 * @return the union of the alphabets
	 */
	public static Set<RegularExpression> alphabetUnion(Automaton first, Automaton second) {
		Set<RegularExpression> fa = first.getAlphabet();
		Set<RegularExpression> sa = second.getAlphabet();

		if (fa.containsAll(sa))
			return fa;

		if (sa.containsAll(fa))
			return sa;

		fa.addAll(sa);
		return fa;
	}

	/**
	 * Yields {@code true} if and only if the given automaton accepts the empty
	 * language.
	 * 
	 * @param automaton the automaton
	 * 
	 * @return {@code true} if that condition holds
	 */
	public static boolean isEmptyLanguageAccepted(Automaton automaton) {
		return minimize(automaton).getFinalStates().isEmpty();
	}

	/**
	 * Computes the intersection between the two given automata.
	 * 
	 * @param first  the first automata
	 * @param second the first automata
	 * 
	 * @return the intersection
	 */
	public static Automaton intersection(Automaton first, Automaton second) {
		// !(!(first) u !(second))
		Set<RegularExpression> commonAlphabet = alphabetUnion(first, second);
		Automaton notFirst = complement(first, commonAlphabet);
		Automaton notSecond = complement(second, commonAlphabet);
		Automaton union = union(notFirst, notSecond);
		Automaton result = complement(union, null);
		return result;
	}

	/**
	 * Computes the concatenation between the given automata.
	 * 
	 * @param automata the automata to concatenate
	 * 
	 * @return the concatenation
	 */
	public static Automaton concat(Automaton... automata) {
		if (automata.length == 0)
			return mkEmptyLanguage();

		Automaton result = automata[0];
		for (int i = 1; i < automata.length; i++)
			result = concat(result, automata[i]);

		return minimize(result);
	}

	/**
	 * Computes the concatenation between the two given automata.
	 * 
	 * @param first  the first automata
	 * @param second the first automata
	 * 
	 * @return the concatenation
	 */
	public static Automaton concat(Automaton first, Automaton second) {
		Map<State, State> mappingFirst = new HashMap<>();
		Map<State, State> mappingSecond = new HashMap<>();
		Set<Transition> newDelta = new HashSet<>();
		Set<State> newStates = new HashSet<>();
		Set<State> firstFinalStates = new HashSet<>();
		Set<State> secondInitialStates = new HashSet<>();

		int c = 0;

		// Add all the first automaton states
		for (State s : first.getStates()) {
			// The first automaton states are not final, can be initial states
			mappingFirst.put(s, new State("q" + c++, s.isInitialState(), false));
			newStates.add(mappingFirst.get(s));
			if (s.isFinalState())
				firstFinalStates.add(s);
		}

		// Add all the second automaton states
		for (State s : second.getStates()) {
			// the second automaton states are final, can't be initial states
			mappingSecond.put(s, new State("q" + c++, false, s.isFinalState()));
			newStates.add(mappingSecond.get(s));
			if (s.isInitialState())
				secondInitialStates.add(s);
		}

		// Add all the first automaton transitions
		for (Transition t : first.getDelta())
			newDelta.add(new Transition(mappingFirst.get(t.getFrom()), mappingFirst.get(t.getTo()), t.getInput()));

		// Add all the second automaton transitions
		for (Transition t : second.getDelta())
			newDelta.add(new Transition(mappingSecond.get(t.getFrom()), mappingSecond.get(t.getTo()), t.getInput()));

		// Add the links between the first automaton final states and the second
		// automaton initial state
		for (State f : firstFinalStates)
			for (State s : secondInitialStates)
				newDelta.add(new Transition(mappingFirst.get(f), mappingSecond.get(s), new Atom("")));

		return minimize(new Automaton(newDelta, newStates));
	}

	/**
	 * Computes the complement of the given automaton w.r.t the given alphabet.
	 * 
	 * @param automaton the automata input
	 * @param alphabet  the alphabet to use during complementation
	 * 
	 * @return the complement of the automata
	 */
	public static Automaton complement(Automaton automaton, Set<RegularExpression> alphabet) {
		Map<State, State> mapping = new HashMap<>();
		Set<Transition> newDelta = new HashSet<>();
		Set<State> newStates = new HashSet<>();

		Automaton a = totalize(automaton, alphabet != null ? alphabet : automaton.getAlphabet());
		// Add states to the mapping, replacing accept states to reject
		for (State s : a.getStates()) {
			mapping.put(s, new State(s.getState(), s.isInitialState(), !s.isFinalState()));
			newStates.add(mapping.get(s));
		}

		// Copying delta set
		for (Transition t : a.getDelta())
			newDelta.add(new Transition(mapping.get(t.getFrom()), mapping.get(t.getTo()), t.getInput()));

		return minimize(new Automaton(newDelta, newStates));
	}

	/**
	 * Performs the totalization of the given automaton w.r.t the given
	 * alphabet.
	 * 
	 * @param automaton the automaton to totalize
	 * @param alphabet  the alphabet to use during totalization
	 * 
	 * @return the totalized automaton
	 */
	public static Automaton totalize(Automaton automaton, Set<RegularExpression> alphabet) {
		Set<State> newState = new HashSet<>();
		Set<Transition> newDelta = new HashSet<>();

		for (State s : automaton.getStates())
			newState.add(s);

		State qbottom = new State("qbottom", false, false);
		newState.add(qbottom);

		for (Transition t : automaton.getDelta())
			newDelta.add(t);

		for (RegularExpression c : alphabet)
			newDelta.add(new Transition(qbottom, qbottom, c));

		Automaton temp = new Automaton(newDelta, newState);

		for (State s : newState)
			for (RegularExpression c : alphabet) {
				Set<State> states = new HashSet<>();
				states.add(s);

				if (!temp.readableSymbolsFromStates(states).contains(c))
					newDelta.add(new Transition(s, qbottom, c));
			}

		return new Automaton(newDelta, newState);
	}

	/**
	 * Computes the union between the given automata.
	 * 
	 * @param automata the automata to unite
	 * 
	 * @return the union
	 */
	public static Automaton union(Automaton... automata) {
		Automaton result = mkEmptyLanguage();

		for (Automaton a : automata)
			result = union(a, result);

		return result;
	}

	/**
	 * Computes the union between the two given automata.
	 * 
	 * @param first  the first automata
	 * @param second the first automata
	 * 
	 * @return the intersection
	 */
	public static Automaton union(Automaton first, Automaton second) {
		State newInitialState = new State("initialState", true, false);
		Set<Transition> newGamma = new HashSet<>();
		Set<State> newStates = new HashSet<>();

		int c = 1;
		Map<State, State> mappingA1 = new HashMap<>();
		Map<State, State> mappingA2 = new HashMap<>();

		newStates.add(newInitialState);

		State initialA1 = null;
		State initialA2 = null;

		for (State s : first.getStates()) {
			mappingA1.put(s, new State("q" + c++, false, s.isFinalState()));
			newStates.add(mappingA1.get(s));
			if (s.isInitialState())
				initialA1 = mappingA1.get(s);
		}

		for (State s : second.getStates()) {
			mappingA2.put(s, new State("q" + c++, false, s.isFinalState()));
			newStates.add(mappingA2.get(s));
			if (s.isInitialState())
				initialA2 = mappingA2.get(s);
		}

		for (Transition t : first.getDelta())
			newGamma.add(new Transition(mappingA1.get(t.getFrom()), mappingA1.get(t.getTo()), t.getInput()));

		for (Transition t : second.getDelta())
			newGamma.add(new Transition(mappingA2.get(t.getFrom()), mappingA2.get(t.getTo()), t.getInput()));

		newGamma.add(new Transition(newInitialState, initialA1, new Atom("")));
		newGamma.add(new Transition(newInitialState, initialA2, new Atom("")));

		return minimize(new Automaton(newGamma, newStates));
	}

	/**
	 * Creates a new automaton composed of a loop over the given automaton.
	 * 
	 * @param a the automaton
	 * 
	 * @return the star automaton
	 */
	public static Automaton star(Automaton a) {
		Automaton result = a.clone();

		for (State f : result.getFinalStates())
			for (State i : result.getInitialStates()) {
				i.setFinalState(true);
				result.getDelta().add(new Transition(f, i, new Atom("")));
			}

		return minimize(result);
	}

	/**
	 * Builds an automaton recognizing no string, that is, recognizing the empty
	 * language.
	 * 
	 * @return the automaton
	 */
	public static Automaton mkEmptyLanguage() {
		Set<State> newStates = new HashSet<>();
		Set<Transition> newGamma = new HashSet<>();
		State initialState = new State("q0", true, false);
		newStates.add(initialState);

		return new Automaton(newGamma, newStates);
	}

	/**
	 * Builds an automaton recognizing only the empty string.
	 * 
	 * @return the automaton
	 */
	public static Automaton mkEmptyString() {
		Set<State> newStates = new HashSet<>();
		Set<Transition> newDelta = new HashSet<>();
		State q0 = new State("q0", true, true);
		newStates.add(q0);

		return new Automaton(newDelta, newStates);
	}

	/**
	 * Builds an automaton recognizing only the top string.
	 * 
	 * @return the automaton
	 */
	public static Automaton mkTopAutomaton() {
		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, true);

		Set<State> states = new HashSet<>();
		states.add(q0);
		states.add(q1);

		Set<Transition> delta = new HashSet<>();
		delta.add(new Transition(q0, q1, TopAtom.INSTANCE));

		return new Automaton(delta, states);
	}

	/**
	 * Builds an automaton recognizing only the given string.
	 * 
	 * @param s the string
	 * 
	 * @return the automaton
	 */
	public static Automaton mkAutomaton(String s) {
		State q0 = new State("q0", true, false);
		State q1 = new State("q1", false, true);

		Set<State> states = new HashSet<>();
		states.add(q0);
		states.add(q1);

		Set<Transition> delta = new HashSet<>();
		delta.add(new Transition(q0, q1, new Atom(s)));

		return new Automaton(delta, states);
	}

	/**
	 * Builds an automaton recognizing only the given extended string.
	 * 
	 * @param s the string
	 * 
	 * @return the automaton
	 */
	public static Automaton mkAutomaton(ExtString s) {
		List<Automaton> result = new ArrayList<>();
		String collector = "";
		for (ExtChar ch : s.collapseTopChars()) {
			if (ch instanceof TopExtChar) {
				if (!collector.isEmpty())
					result.add(mkAutomaton(collector));

				collector = "";
				result.add(mkTopAutomaton());
			} else
				collector += ch.asChar();
		}

		if (!collector.isEmpty())
			result.add(mkAutomaton(collector));

		if (result.isEmpty())
			return mkEmptyString();

		if (result.size() == 1)
			return result.get(0);

		return concat(result.toArray(new Automaton[result.size()]));
	}

	/**
	 * Performs the reverse of the given automaton.
	 * 
	 * @param a the automaton to reverse
	 * 
	 * @return the reversed automaton
	 */
	public static Automaton reverse(Automaton a) {
		Set<State> newStates = new HashSet<>();
		Set<Transition> newDelta = new HashSet<>();
		Map<State, State> mapping = new HashMap<>();

		State newInitialState = new State("init", true, false);
		newStates.add(newInitialState);

		for (State s : a.getStates()) {
			State newState = new State(s.getState(), false, false);
			if (s.isFinalState() && s.isInitialState()) {
				newState.setFinalState(true);
				newDelta.add(new Transition(newInitialState, newState, new Atom("")));
			} else if (s.isFinalState()) {
				newState.setFinalState(false);
				newDelta.add(new Transition(newInitialState, newState, new Atom("")));
			} else if (s.isInitialState()) {
				newState.setFinalState(true);
			}

			mapping.put(s, newState);
			newStates.add(newState);
		}

		for (Transition t : a.getDelta()) {
			RegularExpression reversed = t.getInput() == TopAtom.INSTANCE ? t.getInput()
					: new Atom(StringUtils.reverse(t.getInput().toString()));
			newDelta.add(new Transition(mapping.get(t.getTo()), mapping.get(t.getFrom()), reversed));
		}

		return new Automaton(newDelta, newStates);
	}

	/**
	 * Applies the given comparison function to all the strings of the languages
	 * of both automata. This is a <b>must</b> operation, meaning that all
	 * possible pairs of strings composed by a string of the language of
	 * {@code first} and a string of the language of {@code second} must satisfy
	 * the property expressed by {@code comparer}. If this is not the case, this
	 * method returns {@code false}.
	 * 
	 * @param first    the first automaton
	 * @param second   the second automaton
	 * @param comparer the comparison function between strings of the languages
	 * 
	 * @return {@code true} if that condition hold
	 */
	public static boolean mustLanguageCheck(Automaton first, Automaton second,
			BiFunction<String, String, Boolean> comparer) {
		for (String a : first.getLanguage())
			for (String b : second.getLanguage())
				if (!comparer.apply(a, b))
					return false;
		return true;
	}

	/**
	 * Applies the given comparison function to all the strings of the languages
	 * of both automata. This is a <b>may</b> operation, meaning that at least
	 * one possible pair of strings composed by a string of the language of
	 * {@code first} and a string of the language of {@code second} must satisfy
	 * the property expressed by {@code comparer}. If this is not the case, this
	 * method returns {@code false}.
	 * 
	 * @param first    the first automaton
	 * @param second   the second automaton
	 * @param comparer the comparison function between strings of the languages
	 * 
	 * @return {@code true} if that condition hold
	 */
	public static boolean mayLanguageCheck(Automaton first, Automaton second,
			BiFunction<String, String, Boolean> comparer) {
		for (String a : first.getLanguage())
			for (String b : second.getLanguage())
				if (comparer.apply(a, b))
					return true;
		return false;
	}

	/**
	 * Yields a new automaton that is built by collapsing {@code origin}, that
	 * is, by merging together subsequent states that are never the root of a
	 * branch, the destination of a loop, or that have at least one outgoing
	 * transition recognizing the top string.<br>
	 * <br>
	 * <b>{@code origin} is never modified by this method</b>.
	 * 
	 * @param origin the original automaton
	 * 
	 * @return the collapsed automaton
	 */
	public static Automaton collapse(Automaton origin) {
		HashSet<Vector<State>> collected = new HashSet<>();

		Set<List<State>> paths = origin.getPathExtractor().getAllPaths();
		if (paths.isEmpty())
			return origin;

		for (List<State> v : paths)
			collected.addAll(findMergableStatesInPath(origin, v));

		if (collected.isEmpty())
			return origin;

		Automaton collapsed = origin.clone();
		Set<Transition> edgesToRemove = new HashSet<>();
		Set<State> statesToRemove = new HashSet<>();
		for (Vector<State> v : collected) {
			String accumulated = "";
			if (v.size() == 1)
				statesToRemove.add(v.firstElement());
			else
				for (int i = 0; i < v.size() - 1; i++) {
					State from = v.get(i);
					State to = v.get(i + 1);
					Transition t = origin.getAllTransitionsConnecting(from, to).iterator().next();
					accumulated += ((Atom) t.getInput()).toString();
					edgesToRemove.add(t);
					statesToRemove.add(from);
					statesToRemove.add(to);
				}

			Transition in = collapsed.getIngoingTransitionsFrom(v.firstElement()).iterator().next();
			edgesToRemove.add(in);
			accumulated = ((Atom) in.getInput()).toString() + accumulated;
			Transition out = collapsed.getOutgoingTransitionsFrom(v.lastElement()).iterator().next();
			edgesToRemove.add(out);
			accumulated += ((Atom) out.getInput()).toString();

			collapsed.addTransition(in.getFrom(), out.getTo(), new Atom(accumulated));
		}

		collapsed.removeTransitions(edgesToRemove);
		collapsed.removeStates(statesToRemove);
		collapsed.recomputeOutgoingAdjacencyList();
		return minimize(collapsed);
	}

	private static Set<Vector<State>> findMergableStatesInPath(Automaton origin, List<State> v) {
		Set<Vector<State>> collected = new HashSet<>();
		if (v.size() == 1)
			return collected;

		Vector<State> sequence = new Vector<>();
		boolean collecting = false;
		Set<Transition> tmp;
		for (int i = 0; i < v.size() - 1; i++) {
			State from = v.get(i);
			State to = v.get(i + 1);
			if (origin.getAllTransitionsConnecting(from, to).size() != 1) {
				if (collecting) {
					collecting = false;
					collected.add(sequence);
					sequence = new Vector<>();
				}
			} else if ((tmp = origin.getOutgoingTransitionsFrom(to)).size() == 1
					&& !(tmp.iterator().next().getInput() instanceof TopAtom)) {
				sequence.add(to);
				if (!collecting)
					collecting = true;
			} else if (collecting) {
				collecting = false;
				collected.add(sequence);
				sequence = new Vector<>();
			}
		}

		return collected;
	}

	/**
	 * Yields a deterministic automaton equivalent to the given one. It the
	 * given automaton is already deterministic, then that is returned instead.
	 * <br>
	 * <br>
	 * The given automaton is never modified by this method.
	 * 
	 * @param origin the original automaton
	 * 
	 * @return a deterministic automaton equivalent to the given one.
	 */
	public static Automaton determinize(Automaton origin) {
		if (origin.isDeterministic())
			return origin;

		Set<State> newStates = new HashSet<>();
		Set<Transition> newDelta = new HashSet<>();

		Map<Set<State>, Boolean> marked = new HashMap<>();
		Map<Set<State>, State> statesName = new HashMap<>();
		Deque<Set<State>> unmarked = new LinkedList<>();
		Set<State> temp;
		int num = 0;

		temp = epsilonClosure(origin, origin.getInitialStates());
		statesName.put(temp, new State("q" + String.valueOf(num++), true, containsFinalState(temp)));

		newStates.add(statesName.get(temp));
		marked.put(temp, false);
		unmarked.add(temp);

		while (!unmarked.isEmpty()) {
			Set<State> T = unmarked.getFirst();
			newStates.add(statesName.get(T));
			unmarked.removeFirst();
			marked.put(T, true);

			for (RegularExpression alphabet : origin.readableSymbolsFromStates(T)) {
				temp = epsilonClosure(origin, nextStatesNFA(origin, T, alphabet));

				if (!statesName.containsKey(temp))
					statesName.put(temp, new State("q" + String.valueOf(num++), false, containsFinalState(temp)));

				newStates.add(statesName.get(temp));

				if (!marked.containsKey(temp)) {
					marked.put(temp, false);
					unmarked.addLast(temp);
				}

				newDelta.add(new Transition(statesName.get(T), statesName.get(temp), alphabet));
			}
		}

		return new Automaton(newDelta, newStates);
	}

	private static Set<State> epsilonClosure(Automaton a, State s) {
		Set<State> paths = new HashSet<>();
		Set<State> previous = new HashSet<>();
		Set<State> partial;
		paths.add(s);

		while (!paths.equals(previous)) {
			previous = new HashSet<>(paths);
			partial = new HashSet<>();

			for (State reached : paths)
				for (Transition t : a.getOutgoingTransitionsFrom(reached))
					if (t.isEpsilonTransition())
						partial.add(t.getTo());

			paths.addAll(partial);
		}

		return paths;
	}

	private static Set<State> epsilonClosure(Automaton a, Set<State> set) {
		Set<State> solution = new HashSet<>();

		for (State s : set)
			solution.addAll(epsilonClosure(a, s));

		return solution;
	}

	private static Set<State> nextStatesNFA(Automaton a, Set<State> set, RegularExpression sym) {
		Set<State> solution = new HashSet<>();

		for (State s : set)
			for (Transition t : a.getOutgoingTransitionsFrom(s))
				if (t.getInput().equals(sym))
					solution.add(t.getTo());

		return solution;
	}

	private static boolean containsFinalState(Set<State> states) {
		for (State s : states)
			if (s.isFinalState())
				return true;

		return false;
	}

	private static boolean containsInitialState(Set<State> states) {
		for (State s : states)
			if (s.isInitialState())
				return true;

		return false;
	}

	/**
	 * Yields a new automaton that is built by exploding the given one, that is,
	 * by ensuring that each transition recognizes regular expressions of at
	 * most one character (excluding the ones recognizing the top string). <br>
	 * <br>
	 * <b>The given automaton is never modified by this method</b>.
	 * 
	 * @param a the automaton
	 * 
	 * @return the exploded automaton
	 */
	public static Automaton explode(Automaton a) {
		Automaton exploded = new Automaton(new HashSet<>(), new HashSet<>());
		AtomicLong counter = new AtomicLong();
		Map<State, State> mapping = new HashMap<>();

		for (State origin : a.getStates()) {
			State replaced = mapping.computeIfAbsent(origin,
					s -> new State("q" + counter.getAndIncrement(), s.isInitialState(), s.isFinalState()));
			exploded.addState(replaced);
			for (Transition t : a.getOutgoingTransitionsFrom(origin)) {
				State dest = mapping.computeIfAbsent(t.getTo(),
						s -> new State("q" + counter.getAndIncrement(), s.isInitialState(), s.isFinalState()));
				exploded.addState(dest);
				if (t.getInput().maxLength() < 2)
					exploded.addTransition(replaced, dest, t.getInput());
				else {
					RegularExpression[] regexes = t.getInput().explode();
					State last = replaced;
					for (RegularExpression regex : regexes)
						if (regex == regexes[regexes.length - 1])
							exploded.addTransition(last, dest, regex);
						else {
							State temp = new State("q" + counter.getAndIncrement(), false, false);
							exploded.addState(temp);
							exploded.addTransition(last, temp, regex);
							last = temp;
						}
				}
			}
		}

		return minimize(exploded);
	}

	/**
	 * Yields the sub-automaton contained in the given one that recognizes only
	 * the longest string in the language of {@code a}. Note that this method
	 * assumes that the given automaton is loop-free and that it has only one
	 * path.
	 * 
	 * @param a the automaton
	 * 
	 * @return the sub-automaton
	 */
	public static Automaton extractLongestString(Automaton a) {
		State lastFinalState = null;

		for (State finalState : a.getFinalStates()) {
			Set<Transition> outgoingTransaction = a.getOutgoingTransitionsFrom(finalState);
			if (outgoingTransaction.size() == 0)
				lastFinalState = finalState;
		}

		State nextState = a.getInitialState();
		String s = "";
		while (!nextState.equals(lastFinalState))
			for (Transition t : a.getDelta())
				if (t.getFrom().equals(nextState)) {
					nextState = t.getTo();
					s += t.getInput();
				}

		return mkAutomaton(s);
	}

	/**
	 * Yields a minimal automaton equivalent to the given one through
	 * Brzozowski's minimization algorithm. <br>
	 * <br>
	 * The given automaton is never modified.
	 * 
	 * @param origin the automaton
	 * 
	 * @return a minimal automaton equivalent to this one
	 */
	public static Automaton minimize(Automaton origin) {
		if (!origin.isDeterministic())
			origin = determinize(origin);

		Automaton a = determinize(reverse(origin));
		a.removeUnreachableStates();
		a = determinize(reverse(a));
		a.removeUnreachableStates();

		a.recomputeOutgoingAdjacencyList();

		return a;
	}

	/**
	 * Yields a new automaton where all occurrences of strings recognized by
	 * {@code toReplace} are replaced with the automaton {@code str}, assuming
	 * that {@code toReplace} is finite (i.e., no loops nor top-transitions).
	 * The resulting automaton is then collapsed.<br>
	 * <br>
	 * If {@code toReplace} recognizes a single string, than this method
	 * performs a must-replacement, meaning that the string recognized by
	 * {@code toReplace} will effectively be replaced. Otherwise, occurrences of
	 * strings of {@code toReplace} are not replaced in the resulting automaton:
	 * instead, a branching will be introduced to model an or between the
	 * original string of {@code toReplace} and the whole {@code str}. <br>
	 * <br>
	 * <b>{@code origin} is never modified by this method</b>.
	 * 
	 * @param origin    the original automaton
	 * @param toReplace the automaton recognizing the strings to replace
	 * @param str       the automaton that must be used as replacement
	 * 
	 * @return the replaced automaton
	 */
	public static Automaton replace(Automaton origin, Automaton toReplace, Automaton str) {
		Automaton explodedOrigin = Automata.explode(origin);

		Collection<Automaton> automata = new ArrayList<>();
		boolean isSingleString = toReplace.getLanguage().size() == 1;
		for (String s : toReplace.getLanguage())
			automata.add(collapse(new StringReplacer(explodedOrigin).replace(s, str, isSingleString)));

		if (automata.size() == 1)
			return automata.iterator().next();

		return union(automata.toArray(new Automaton[automata.size()]));
	}

	/**
	 * Performs the parametrized widening operation on {@code a}. <br>
	 * <br>
	 * {@code a} is never modified by this method.
	 * 
	 * @param a the automaton
	 * @param n the parameter of the widening operator
	 * 
	 * @return the widened automaton
	 */
	public static Automaton widening(Automaton a, int n) {
		Map<State, Set<RegularExpression>> languages = new HashMap<>();
		Set<Set<State>> powerStates = new HashSet<>();

		for (State s : a.getStates())
			languages.put(s, getNextSymbols(a, s, n));

		for (State s1 : a.getStates())
			for (State s2 : a.getStates())
				if (languages.get(s1).equals(languages.get(s2))) {
					boolean found = false;
					for (Set<State> singlePowerState : powerStates)
						if (singlePowerState.contains(s1) || singlePowerState.contains(s2)) {
							singlePowerState.add(s1);
							singlePowerState.add(s2);
							found = true;
							break;
						}

					if (!found) {
						Set<State> newPowerState = new HashSet<>();
						newPowerState.add(s1);
						newPowerState.add(s2);
						powerStates.add(newPowerState);
					}
				}

		Set<State> newStates = new HashSet<>();
		Map<Set<State>, State> mapping = new HashMap<>();

		int i = 0;

		for (Set<State> ps : powerStates) {
			State ns = new State("q" + i++, containsInitialState(ps), containsFinalState(ps));
			newStates.add(ns);
			mapping.put(ps, ns);
		}

		Set<Transition> newDelta = new HashSet<>();

		Set<State> fromPartition = null;
		Set<State> toPartition = null;

		for (Transition t : a.getDelta()) {
			for (Set<State> ps : powerStates) {
				if (ps.contains(t.getFrom()))
					fromPartition = ps;
				if (ps.contains(t.getTo()))
					toPartition = ps;
			}

			newDelta.add(new Transition(mapping.get(fromPartition), mapping.get(toPartition), t.getInput()));
		}

		return new Automaton(newDelta, newStates);
	}

	/**
	 * Yields the set of regular expressions of length at most {@code n} that
	 * can be recognized starting from the given state. In this context, the
	 * length of a regular expression is the number of sub-expressions joined
	 * together to build the final regular expression. For instance, given
	 * {@code r1 = aab} and {@code r2 = c*}, {@code r = r1r2} has length 2. Each
	 * regular expression returned by this method is built by inspecting the
	 * outgoing transition from the given state {@code s}, and, for each such
	 * transition, by prepending the symbol on the transition to each regular
	 * expression returned by recursively calling this method on the destination
	 * state after reducing {@code n} by 1.
	 */
	private static Set<RegularExpression> getNextSymbols(Automaton a, State s, int n) {
		Set<RegularExpression> result = new HashSet<>();

		if (n == 0)
			return result;

		for (Transition t : a.getOutgoingTransitionsFrom(s)) {
			RegularExpression partial = t.getInput();
			Set<RegularExpression> nextStrings = getNextSymbols(a, t.getTo(), n - 1);

			if (nextStrings.isEmpty())
				result.add(partial);
			else
				for (RegularExpression next : nextStrings)
					result.add(partial.concat(next));
		}

		return result;
	}
}
