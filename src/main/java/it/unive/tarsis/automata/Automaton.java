package it.unive.tarsis.automata;

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
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import it.unive.tarsis.automata.algorithms.PathExtractor;
import it.unive.tarsis.automata.algorithms.RegexExtractor;
import it.unive.tarsis.automata.algorithms.StringReplacer;
import it.unive.tarsis.regex.Atom;
import it.unive.tarsis.regex.RegularExpression;
import it.unive.tarsis.regex.TopAtom;
import it.unive.tarsis.strings.ExtChar;
import it.unive.tarsis.strings.ExtString;
import it.unive.tarsis.strings.TopExtChar;

/**
 * An automaton, represented as a set of states and a set of transitions.
 * Transitions recognize regular expression of type {@link RegularExpression}.
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class Automaton {

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
	 * The set of transitions
	 */
	private Set<Transition> delta;

	/**
	 * The set of states
	 */
	private Set<State> states;

	/**
	 * Outgoing adjacency list
	 */
	private Map<State, Set<Transition>> adjacencyListOutgoing;

	/**
	 * The path extractor that is tied to this automaton
	 */
	private final PathExtractor pathExtractor;

	/**
	 * Builds a new automaton.
	 * 
	 * @param delta  the set of transitions
	 * @param states the set of states
	 */
	public Automaton(Set<Transition> delta, Set<State> states) {
		this.delta = delta;
		this.states = states;
		pathExtractor = new PathExtractor(this);
		recomputeOutgoingAdjacencyList();
	}

	/**
	 * Recomputes the outgoing adjacency list of this automaton. The list is not
	 * automatically updated by most of the update operations for delaying the
	 * computation at the end of the processing.
	 */
	public void recomputeOutgoingAdjacencyList() {
		adjacencyListOutgoing = new HashMap<>();

		for (Transition t : delta)
			adjacencyListOutgoing.computeIfAbsent(t.getFrom(), s -> new HashSet<>()).add(t);
	}

	/**
	 * Yields the path extractor tied to this automaton, that can extract paths
	 * from it.
	 * 
	 * @return the path extractor
	 */
	public PathExtractor getPathExtractor() {
		return pathExtractor;
	}

	/**
	 * Yields the set of regular expressions that represents the alphabet of the
	 * symbols recognized by this automaton.
	 * 
	 * @return the alphabet recognized by this automaton
	 */
	public Set<RegularExpression> getAlphabet() {
		Set<RegularExpression> alphabet = new HashSet<>();

		for (Transition t : delta)
			alphabet.add(t.getInput());

		return alphabet;
	}

	/**
	 * Yields true if and only if this automaton is deterministic, that is, if
	 * the transition relation is a function. This is computed by detecting if
	 * none of the states of this automaton has two outgoing transitions
	 * recognizing the same symbol but going to different states.
	 * 
	 * @return {@code true} if and only if that condition holds
	 */
	public boolean isDeterministic() {
		for (State s : states) {
			Set<Transition> outgoingTranisitions = getOutgoingTransitionsFrom(s);
			for (Transition t : outgoingTranisitions)
				if (t.getInput().isEmpty())
					return false;
				else
					for (Transition t2 : outgoingTranisitions)
						if (t2.getInput().isEmpty())
							return false;
						else if (!t.getTo().equals(t2.getTo()) && t.getInput().equals(t2.getInput()))
							return false;
		}

		return true;
	}

	/**
	 * Yields the set of transitions contained in this automaton.
	 * 
	 * @return the set of transitions
	 */
	public Set<Transition> getDelta() {
		return delta;
	}

	/**
	 * Yields true if and only if the two given states are mutually reachable,
	 * that is, if there exist a path going from {@code s1} to {@code s2} and
	 * one going from {@code s2} to {@code s1}. Paths are searched through the
	 * Dijkstra algorithm.
	 * 
	 * @param s1 the first state
	 * @param s2 the second state
	 * 
	 * @return {@code true} if and only if {@code s1} and {@code s2} are
	 *             mutually reachable
	 */
	public boolean areMutuallyReachable(State s1, State s2) {
		return !pathExtractor.minimumDijkstra(s1, s2).isEmpty() && !pathExtractor.minimumDijkstra(s2, s1).isEmpty();
	}

	/**
	 * Yields the set of all outgoing transitions from the given state.
	 * 
	 * @param s the state
	 * 
	 * @return the set of outgoing transitions
	 */
	public Set<Transition> getOutgoingTransitionsFrom(State s) {
		return adjacencyListOutgoing.computeIfAbsent(s, ss -> new HashSet<>());
	}

	/**
	 * Yields the set of all ingoing transitions to the given state.
	 * 
	 * @param s the state
	 * 
	 * @return the set of ingoing transitions
	 */
	public Set<Transition> getIngoingTransitionsFrom(State s) {
		List<Transition> collect = delta.parallelStream().filter(t -> t.getTo().equals(s)).collect(Collectors.toList());

		Set<Transition> result = new HashSet<>();
		for (Transition t : collect)
			result.add(t);

		return result;
	}

	/**
	 * Yields the set of regular expressions that can be read (accepted) by the
	 * given set of states.
	 * 
	 * @param states the set of states
	 * 
	 * @return the set of regular expression that can be read
	 */
	public Set<RegularExpression> readableSymbolsFromStates(Set<State> states) {
		Set<RegularExpression> result = new HashSet<>();

		for (State s : states)
			for (Transition t : getOutgoingTransitionsFrom(s))
				if (!t.getInput().isEmpty())
					result.add(t.getInput());

		return result;
	}

	/**
	 * Removes all unreachable states from this automaton.
	 */
	public void removeUnreachableStates() {

		Set<State> reachableStates = new HashSet<>();
		Set<State> newStates = new HashSet<>();
		Set<State> temp = new HashSet<>();

		reachableStates.add(getInitialState());
		newStates.add(getInitialState());

		do {

			for (State s : newStates) {
				for (Transition t : getOutgoingTransitionsFrom(s))
					temp.add(t.getTo());
			}

			temp.removeAll(reachableStates);
			newStates.clear();
			newStates.addAll(temp);
			reachableStates.addAll(newStates);
		} while (!newStates.isEmpty());

		states.removeIf(s -> !reachableStates.contains(s));
		delta.removeIf(t -> !reachableStates.contains(t.getFrom()) || !reachableStates.contains(t.getTo()));
	}

	/**
	 * Yields the set of transitions going from {@code s1} to {@code s2}.
	 * 
	 * @param s1 the source state
	 * @param s2 the destination state
	 * 
	 * @return the set of transitions connecting the two states
	 */
	public Set<Transition> getAllTransitionsConnecting(State s1, State s2) {
		Set<Transition> result = new HashSet<>();

		for (Transition t : this.delta)
			if (t.getFrom().equals(s1) && t.getTo().equals(s2))
				result.add(t);

		return result;
	}

	private void moveVertex(State vertex, Set<State> sourceSet, Set<State> destinationSet) {
		sourceSet.remove(vertex);
		destinationSet.add(vertex);
	}

	/**
	 * Yields the set of final states of this automaton.
	 * 
	 * @return the set of final states
	 */
	public Set<State> getFinalStates() {
		Set<State> result = new HashSet<>();

		for (State s : states)
			if (s.isFinalState())
				result.add(s);

		return result;
	}

	/**
	 * Yields one of the initial states of this automaton. If this automaton
	 * does not have an initial state, this method returns {@code null}.
	 * 
	 * @return one of the initial states, or {@code null}
	 */
	public State getInitialState() {
		for (State s : states)
			if (s.isInitialState())
				return s;

		return null;
	}

	/**
	 * Yields the set of initial states of this automaton.
	 * 
	 * @return the set of initial states
	 */
	public Set<State> getInitialStates() {
		Set<State> initialStates = new HashSet<>();

		for (State s : states)
			if (s.isInitialState())
				initialStates.add(s);

		return initialStates;
	}

	/**
	 * Yields the set of states of this automaton.
	 * 
	 * @return the set of states
	 */
	public Set<State> getStates() {
		return states;
	}

	/**
	 * Yields a textual representation of the adjacency matrix of this
	 * automaton.
	 * 
	 * @return a string containing a textual representation of the automaton
	 */
	public String automatonPrint() {
		String result = "";
		Set<Transition> transitions;

		for (State st : states)
			if (!(transitions = getOutgoingTransitionsFrom(st)).isEmpty() || st.isFinalState() || st.isInitialState()) {
				result += "[" + st.getState() + "] "
						+ (st.isFinalState() ? "[accept]" + (st.isInitialState() ? "[initial]\n" : "\n")
								: "[reject]" + (st.isInitialState() ? "[initial]\n" : "\n"));
				for (Transition t : transitions)
					result += "\t" + st + " " + t.getInput() + " -> " + t.getTo() + "\n";
			}

		return result;
	}

	@Override
	public String toString() {
		return RegexExtractor.getMinimalRegex(this).toString();
	}

	public Automaton copy() {
		Set<State> newStates = new HashSet<>();
		Set<Transition> newDelta = new HashSet<>();
		HashMap<String, State> nameToStates = new HashMap<String, State>();

		for (State s : states) {
			State newState = new State(s.getState(), s.isInitialState(), s.isFinalState());
			newStates.add(newState);
			nameToStates.put(newState.getState(), newState);
		}

		for (Transition t : delta)
			newDelta.add(new Transition(nameToStates.get(t.getFrom().getState()),
					nameToStates.get(t.getTo().getState()), t.getInput()));

		Automaton result = new Automaton(newDelta, newStates);
		result.adjacencyListOutgoing = new HashMap<>(adjacencyListOutgoing);
		return result;
	}

	/**
	 * Yields the language recognized by this automaton, that is, the set of all
	 * strings recognized by this automaton. In this context, a regular
	 * expression representing the top string is treated as a one-character
	 * string containing only the top character stored in
	 * {@link TopAtom#STRING}.
	 * 
	 * @return the language recognized by this automaton
	 */
	public Set<String> getLanguage() {
		return extractStrings(new HashSet<String>(), "", this.getInitialState(), null);
	}

	private Set<String> extractStrings(Set<String> set, String partialString, State currentState, Transition prevT) {
		if (prevT != null) {
			partialString += prevT.getInput();
			if (currentState.isFinalState())
				set.add(partialString);
		} else if (currentState.isInitialState() && currentState.isFinalState())
			set.add("");

		for (Transition t : this.getOutgoingTransitionsFrom(currentState))
			extractStrings(set, new String(partialString), t.getTo(), t);

		return set;
	}

	/**
	 * Yields the set of regular expressions that represent the union of the
	 * alphabets of this automaton and the given one.
	 * 
	 * @param other the other automaton
	 * 
	 * @return the union of the alphabets
	 */
	private Set<RegularExpression> alphabetUnion(Automaton other) {
		Set<RegularExpression> fa = getAlphabet();
		Set<RegularExpression> sa = other.getAlphabet();

		if (fa.containsAll(sa))
			return fa;

		if (sa.containsAll(fa))
			return sa;

		fa.addAll(sa);
		return fa;
	}

	/**
	 * Performs the totalization of this automaton w.r.t the given alphabet.
	 * 
	 * @param alphabet the alphabet to use during totalization
	 * 
	 * @return the totalized automaton
	 */
	private Automaton totalize(Set<RegularExpression> alphabet) {
		Set<State> newState = new HashSet<>();
		Set<Transition> newDelta = new HashSet<>();

		for (State s : getStates())
			newState.add(s);

		State qbottom = new State("qbottom", false, false);
		newState.add(qbottom);

		for (Transition t : getDelta())
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
	 * Computes the complement of this automaton w.r.t the given alphabet.
	 * 
	 * @param alphabet the alphabet to use during complementation
	 * 
	 * @return the complement of the automata
	 */
	private Automaton complement(Set<RegularExpression> alphabet) {
		Map<State, State> mapping = new HashMap<>();
		Set<Transition> newDelta = new HashSet<>();
		Set<State> newStates = new HashSet<>();

		Automaton a = totalize(alphabet != null ? alphabet : getAlphabet());
		// Add states to the mapping, replacing accept states to reject
		for (State s : a.getStates()) {
			mapping.put(s, new State(s.getState(), s.isInitialState(), !s.isFinalState()));
			newStates.add(mapping.get(s));
		}

		// Copying delta set
		for (Transition t : a.getDelta())
			newDelta.add(new Transition(mapping.get(t.getFrom()), mapping.get(t.getTo()), t.getInput()));

		return new Automaton(newDelta, newStates).minimize();
	}

	/**
	 * Computes the union between this automaton and the given one.
	 * 
	 * @param other the other automata
	 * 
	 * @return the union
	 */
	public Automaton union(Automaton other) {
		State newInitialState = new State("initialState", true, false);
		Set<Transition> newGamma = new HashSet<>();
		Set<State> newStates = new HashSet<>();

		int c = 1;
		Map<State, State> mappingA1 = new HashMap<>();
		Map<State, State> mappingA2 = new HashMap<>();

		newStates.add(newInitialState);

		State initialA1 = null;
		State initialA2 = null;

		for (State s : getStates()) {
			mappingA1.put(s, new State("q" + c++, false, s.isFinalState()));
			newStates.add(mappingA1.get(s));
			if (s.isInitialState())
				initialA1 = mappingA1.get(s);
		}

		for (State s : other.getStates()) {
			mappingA2.put(s, new State("q" + c++, false, s.isFinalState()));
			newStates.add(mappingA2.get(s));
			if (s.isInitialState())
				initialA2 = mappingA2.get(s);
		}

		for (Transition t : getDelta())
			newGamma.add(new Transition(mappingA1.get(t.getFrom()), mappingA1.get(t.getTo()), t.getInput()));

		for (Transition t : other.getDelta())
			newGamma.add(new Transition(mappingA2.get(t.getFrom()), mappingA2.get(t.getTo()), t.getInput()));

		newGamma.add(new Transition(newInitialState, initialA1, Atom.EPSILON));
		newGamma.add(new Transition(newInitialState, initialA2, Atom.EPSILON));

		return new Automaton(newGamma, newStates).minimize();
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
			result = a.union(result);

		return result;
	}

	/**
	 * Computes the intersection between this automaton and the given one.
	 * 
	 * @param other the other automata
	 * 
	 * @return the intersection
	 */
	public Automaton intersection(Automaton other) {
		// !(!(first) u !(second))
		Set<RegularExpression> commonAlphabet = alphabetUnion(other);
		Automaton notFirst = complement(commonAlphabet);
		Automaton notSecond = other.complement(commonAlphabet);
		Automaton union = notFirst.union(notSecond);
		Automaton result = union.complement(null);
		return result;
	}

	/**
	 * Yields {@code true} if and only if this automaton accepts the empty
	 * language.
	 * 
	 * @return {@code true} if that condition holds
	 */
	public boolean isEmptyLanguageAccepted() {
		return minimize().getFinalStates().isEmpty();
	}

	/**
	 * Yields {@code true} if and only if {@code this} is contained into
	 * {@code other}, that is, if the language recognized by the intersection
	 * between {@code this} and the complement of {@code other} is empty.
	 * 
	 * @param other the other automaton
	 * 
	 * @return {@code true} if that condition holds
	 */
	public boolean isContained(Automaton other) {
		return intersection(other.complement(alphabetUnion(other))).isEmptyLanguageAccepted();
	}

	/**
	 * Equal operator between automata, implemented as:<br>
	 * {@code language(A).equals(language(B))} if both automata are loop free,
	 * {@code A.contains(B) && B.contains(A)} otherwise.<br>
	 * <br>
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
		if (other instanceof Automaton) {
			Automaton o = (Automaton) other;
			if (!hasCycle() && !o.hasCycle())
				return getLanguage().equals(o.getLanguage());

			Automaton a = copy();
			Automaton b = o.copy();

			a = a.minimize();
			b = b.minimize();

			if (a.hasCycle() && !b.hasCycle() || !a.hasCycle() && b.hasCycle())
				return false;

			if (!a.hasCycle() && !b.hasCycle())
				return a.getLanguage().equals(b.getLanguage());

			if (!a.isContained(b))
				return false;

			if (!b.isContained(a))
				return false;

			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return states.size() + delta.size();
	}

	/**
	 * Yields the length of the longest string recognized by this automaton,
	 * with {@link Integer#MAX_VALUE} representing infinity.
	 * 
	 * @return the length of the longest string
	 */
	public int maxLengthString() {
		Set<List<State>> paths = pathExtractor.getAllPaths();
		if (paths.size() == 0)
			return 0;

		int max = Integer.MIN_VALUE, tmp;
		for (List<State> v : paths)
			if ((tmp = maxStringLengthTraversing(v)) > max)
				max = tmp;

		return max;
	}

	private int maxStringLengthTraversing(List<State> path) {
		if (path.size() == 0)
			return 0;

		if (path.size() == 1)
			// length of self loops
			return maxStringLength(path.get(0), path.get(0));

		int len = 0;
		for (int i = 0; i < path.size() - 1; i++)
			len += maxStringLength(path.get(i), path.get(i + 1));

		return len;
	}

	private int maxStringLength(State from, State to) {
		Set<Transition> transitions = getAllTransitionsConnecting(from, to);
		if (delta.size() == 0)
			return 0;

		if (delta.size() == 1)
			return delta.iterator().next().getInput().maxLength();

		int len = -1;
		for (Transition t : transitions)
			if (len == -1)
				len = t.getInput().maxLength();
			else
				len = Math.max(len, t.getInput().maxLength());

		return len;
	}

	/**
	 * Yields {@code true} if and only if this automaton contains at least one
	 * cycle.
	 * 
	 * @return {@code true} if that condition holds
	 */
	public boolean hasCycle() {
		Set<State> whiteSet = new HashSet<>();
		Set<State> graySet = new HashSet<>();
		Set<State> blackSet = new HashSet<>();

		for (State vertex : states)
			whiteSet.add(vertex);

		while (whiteSet.size() > 0) {
			State current = whiteSet.iterator().next();
			if (cycleSearchWithDfs(current, whiteSet, graySet, blackSet))
				return true;
		}

		return false;
	}

	private boolean cycleSearchWithDfs(State current, Set<State> whiteSet, Set<State> graySet, Set<State> blackSet) {
		// move current to gray set from white set and then explore it.
		moveVertex(current, whiteSet, graySet);

		for (Transition t : getOutgoingTransitionsFrom(current)) {
			State neighbor = t.getTo();

			// if in black set means already explored so continue.
			if (blackSet.contains(neighbor))
				continue;

			// if in gray set then cycle found.
			if (graySet.contains(neighbor))
				return true;

			if (cycleSearchWithDfs(neighbor, whiteSet, graySet, blackSet))
				return true;
		}

		// move vertex from gray set to black set when done exploring.
		moveVertex(current, graySet, blackSet);

		return false;
	}

	/**
	 * Yields the set of possible successors of the given node.
	 * 
	 * @param node the node
	 * 
	 * @return the set of possible successors
	 */
	public Set<State> getNextStates(State node) {
		Set<State> neighbors = new HashSet<>();
		for (Transition edge : adjacencyListOutgoing.get(node))
			neighbors.add(edge.getTo());

		return neighbors;
	}

	/**
	 * Adds a new state to this automaton.
	 * 
	 * @param s the state to add
	 */
	public void addState(State s) {
		states.add(s);
	}

	/**
	 * Builds a new transition going from {@code from} to {@code to} and
	 * recognizing {@code input} and adds it to the set of transitions of this
	 * automaton, <b>without recomputing the outgoing adjacency list</b>.
	 * 
	 * @param from  the source node
	 * @param to    the destination node
	 * @param input the input to be recognized by the transition
	 */
	public void addTransition(State from, State to, RegularExpression input) {
		addTransition(new Transition(from, to, input));
	}

	/**
	 * Adds the given transition to the set of transitions of this automaton,
	 * <b>without recomputing the outgoing adjacency list</b>.
	 * 
	 * @param t the transition to add
	 */
	public void addTransition(Transition t) {
		delta.add(t);
	}

	/**
	 * Removes every transition in the given set from the ones of this
	 * automaton, <b>without recomputing the outgoing adjacency list</b>.
	 * 
	 * @param ts the set of transitions to remove
	 */
	public void removeTransitions(Set<Transition> ts) {
		delta.removeAll(ts);
	}

	/**
	 * Removes every state in the given set from the ones of this automaton,
	 * <b>without recomputing the outgoing adjacency list</b>.
	 * 
	 * @param ts the set of states to remove
	 */
	public void removeStates(Set<State> ts) {
		states.removeAll(ts);
	}

	/**
	 * Yields {@code true} if and only if there is at least one transition in
	 * this automaton that recognizes a top string.
	 * 
	 * @return {@code true} if that condition holds
	 */
	public boolean acceptsTopEventually() {
		removeUnreachableStates();
		for (Transition t : delta)
			if (t.getInput() instanceof TopAtom)
				return true;

		return false;
	}

	/**
	 * Yields {@code true} if and only if this automaton has only one path. The
	 * general idea is that, after minimization, if the automaton has only one
	 * path then the each state is the starting point of a transition at most
	 * once, and it is the destingation node of a transition at most once. Note
	 * that this method assumes that this automaton is loop-free.
	 * 
	 * @return {@code true} if that condition holds
	 */
	public boolean hasOnlyOnePath() {
		Automaton a = minimize();

		Set<State> transFrom = new HashSet<>();
		Set<State> transTo = new HashSet<>();
		State from = null;
		State to = null;

		for (Transition t : a.delta) {
			from = t.getFrom();
			if (transFrom.contains(from))
				return false;

			transFrom.add(from);

			to = t.getTo();
			if (transTo.contains(to))
				return false;

			transTo.add(to);
		}

		return true;
	}

	private Set<State> epsilonClosure(State s) {
		Set<State> paths = new HashSet<>();
		Set<State> previous = new HashSet<>();
		Set<State> partial;
		paths.add(s);

		while (!paths.equals(previous)) {
			previous = new HashSet<>(paths);
			partial = new HashSet<>();

			for (State reached : paths)
				for (Transition t : getOutgoingTransitionsFrom(reached))
					if (t.isEpsilonTransition())
						partial.add(t.getTo());

			paths.addAll(partial);
		}

		return paths;
	}

	private Set<State> epsilonClosure(Set<State> set) {
		Set<State> solution = new HashSet<>();

		for (State s : set)
			solution.addAll(epsilonClosure(s));

		return solution;
	}

	private Set<State> nextStatesNFA(Set<State> set, RegularExpression sym) {
		Set<State> solution = new HashSet<>();

		for (State s : set)
			for (Transition t : getOutgoingTransitionsFrom(s))
				if (t.getInput().equals(sym))
					solution.add(t.getTo());

		return solution;
	}

	private static boolean containsInitialState(Set<State> states) {
		for (State s : states)
			if (s.isInitialState())
				return true;

		return false;
	}

	private static boolean containsFinalState(Set<State> states) {
		for (State s : states)
			if (s.isFinalState())
				return true;

		return false;
	}

	/**
	 * Yields a deterministic automaton equivalent to this one. It this
	 * automaton is already deterministic, it is immediately returned instead.
	 * <br>
	 * <br>
	 * This automaton is never modified by this method.
	 * 
	 * @return a deterministic automaton equivalent to this one.
	 */
	private Automaton determinize() {
		if (isDeterministic())
			return this;

		Set<State> newStates = new HashSet<>();
		Set<Transition> newDelta = new HashSet<>();

		Map<Set<State>, Boolean> marked = new HashMap<>();
		Map<Set<State>, State> statesName = new HashMap<>();
		Deque<Set<State>> unmarked = new LinkedList<>();
		Set<State> temp;
		int num = 0;

		temp = epsilonClosure(getInitialStates());
		statesName.put(temp, new State("q" + String.valueOf(num++), true, containsFinalState(temp)));

		newStates.add(statesName.get(temp));
		marked.put(temp, false);
		unmarked.add(temp);

		while (!unmarked.isEmpty()) {
			Set<State> T = unmarked.getFirst();
			newStates.add(statesName.get(T));
			unmarked.removeFirst();
			marked.put(T, true);

			for (RegularExpression alphabet : readableSymbolsFromStates(T)) {
				temp = epsilonClosure(nextStatesNFA(T, alphabet));

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

	/**
	 * Performs the reverse of this automaton.
	 * 
	 * @return the reversed automaton
	 */
	public Automaton reverse() {
		Set<State> newStates = new HashSet<>();
		Set<Transition> newDelta = new HashSet<>();
		Map<State, State> mapping = new HashMap<>();

		State newInitialState = new State("init", true, false);
		newStates.add(newInitialState);

		for (State s : getStates()) {
			State newState = new State(s.getState(), false, false);
			if (s.isFinalState() && s.isInitialState()) {
				newState.setFinalState(true);
				newDelta.add(new Transition(newInitialState, newState, Atom.EPSILON));
			} else if (s.isFinalState()) {
				newState.setFinalState(false);
				newDelta.add(new Transition(newInitialState, newState, Atom.EPSILON));
			} else if (s.isInitialState()) {
				newState.setFinalState(true);
			}

			mapping.put(s, newState);
			newStates.add(newState);
		}

		for (Transition t : getDelta()) {
			RegularExpression reversed = t.getInput() == TopAtom.INSTANCE ? t.getInput()
					: new Atom(StringUtils.reverse(t.getInput().toString()));
			newDelta.add(new Transition(mapping.get(t.getTo()), mapping.get(t.getFrom()), reversed));
		}

		return new Automaton(newDelta, newStates);
	}

	/**
	 * Yields a minimal automaton equivalent to this one through Brzozowski's
	 * minimization algorithm. <br>
	 * <br>
	 * This automaton is never modified.
	 * 
	 * @return a minimal automaton equivalent to this one
	 */
	public Automaton minimize() {
		Automaton a = this;
		if (!isDeterministic())
			a = determinize();

		a = a.reverse().determinize();
		a.removeUnreachableStates();
		a = a.reverse().determinize();
		a.removeUnreachableStates();
		a.recomputeOutgoingAdjacencyList();

		return a;
	}

	/**
	 * Computes the concatenation between this automaton and the given one.
	 * 
	 * @param other the other automata
	 * 
	 * @return the concatenation
	 */
	public Automaton concat(Automaton other) {
		Map<State, State> mappingFirst = new HashMap<>();
		Map<State, State> mappingSecond = new HashMap<>();
		Set<Transition> newDelta = new HashSet<>();
		Set<State> newStates = new HashSet<>();
		Set<State> firstFinalStates = new HashSet<>();
		Set<State> secondInitialStates = new HashSet<>();

		int c = 0;

		// Add all the first automaton states
		for (State s : getStates()) {
			// The first automaton states are not final, can be initial states
			mappingFirst.put(s, new State("q" + c++, s.isInitialState(), false));
			newStates.add(mappingFirst.get(s));
			if (s.isFinalState())
				firstFinalStates.add(s);
		}

		// Add all the second automaton states
		for (State s : other.getStates()) {
			// the second automaton states are final, can't be initial states
			mappingSecond.put(s, new State("q" + c++, false, s.isFinalState()));
			newStates.add(mappingSecond.get(s));
			if (s.isInitialState())
				secondInitialStates.add(s);
		}

		// Add all the first automaton transitions
		for (Transition t : getDelta())
			newDelta.add(new Transition(mappingFirst.get(t.getFrom()), mappingFirst.get(t.getTo()), t.getInput()));

		// Add all the second automaton transitions
		for (Transition t : other.getDelta())
			newDelta.add(new Transition(mappingSecond.get(t.getFrom()), mappingSecond.get(t.getTo()), t.getInput()));

		// Add the links between the first automaton final states and the second
		// automaton initial state
		for (State f : firstFinalStates)
			for (State s : secondInitialStates)
				newDelta.add(new Transition(mappingFirst.get(f), mappingSecond.get(s), Atom.EPSILON));

		return new Automaton(newDelta, newStates).minimize();
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
			result = result.concat(automata[i]);

		return result.minimize();
	}

	/**
	 * Yields a new automaton that is built by exploding this one, that is, by
	 * ensuring that each transition recognizes regular expressions of at most
	 * one character (excluding the ones recognizing the top string). <br>
	 * <br>
	 * <b>This automaton is never modified by this method</b>.
	 * 
	 * @return the exploded automaton
	 */
	public Automaton explode() {
		Automaton exploded = new Automaton(new HashSet<>(), new HashSet<>());
		AtomicLong counter = new AtomicLong();
		Map<State, State> mapping = new HashMap<>();

		for (State origin : getStates()) {
			State replaced = mapping.computeIfAbsent(origin,
					s -> new State("q" + counter.getAndIncrement(), s.isInitialState(), s.isFinalState()));
			exploded.addState(replaced);
			for (Transition t : getOutgoingTransitionsFrom(origin)) {
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

		return exploded.minimize();
	}

	private Set<Vector<State>> findMergableStatesInPath(List<State> v) {
		Set<Vector<State>> collected = new HashSet<>();
		if (v.size() == 1)
			return collected;

		Vector<State> sequence = new Vector<>();
		boolean collecting = false;
		Set<Transition> tmp;
		for (int i = 0; i < v.size() - 1; i++) {
			State from = v.get(i);
			State to = v.get(i + 1);
			if (getAllTransitionsConnecting(from, to).size() != 1) {
				if (collecting) {
					collecting = false;
					collected.add(sequence);
					sequence = new Vector<>();
				}
			} else if ((tmp = getOutgoingTransitionsFrom(to)).size() == 1
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
	 * Yields a new automaton that is built by collapsing {@code this}, that is,
	 * by merging together subsequent states that are never the root of a
	 * branch, the destination of a loop, or that have at least one outgoing
	 * transition recognizing the top string.<br>
	 * <br>
	 * <b>{@code this} is never modified by this method</b>.
	 * 
	 * @return the collapsed automaton
	 */
	private Automaton collapse() {
		HashSet<Vector<State>> collected = new HashSet<>();

		Set<List<State>> paths = getPathExtractor().getAllPaths();
		if (paths.isEmpty())
			return this;

		for (List<State> v : paths)
			collected.addAll(findMergableStatesInPath(v));

		if (collected.isEmpty())
			return this;

		Automaton collapsed = copy();
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
					Transition t = getAllTransitionsConnecting(from, to).iterator().next();
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
		return collapsed.minimize();
	}

	/**
	 * Creates a new automaton composed of a loop over this one.
	 * 
	 * @return the star automaton
	 */
	public Automaton star() {
		Automaton result = copy();

		for (State f : result.getFinalStates())
			for (State i : result.getInitialStates()) {
				i.setFinalState(true);
				result.getDelta().add(new Transition(f, i, Atom.EPSILON));
			}

		return result.minimize();
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
	private Set<RegularExpression> getNextSymbols(State s, int n) {
		Set<RegularExpression> result = new HashSet<>();

		if (n == 0)
			return result;

		for (Transition t : getOutgoingTransitionsFrom(s)) {
			RegularExpression partial = t.getInput();
			Set<RegularExpression> nextStrings = getNextSymbols(t.getTo(), n - 1);

			if (nextStrings.isEmpty())
				result.add(partial);
			else
				for (RegularExpression next : nextStrings)
					result.add(partial.concat(next));
		}

		return result;
	}

	/**
	 * Performs the parametrized widening operation on {@code a} on this
	 * automaton.<br>
	 * <br>
	 * {@code this} is never modified by this method.
	 * 
	 * @param n the parameter of the widening operator
	 * 
	 * @return the widened automaton
	 */
	public Automaton widening(int n) {
		Map<State, Set<RegularExpression>> languages = new HashMap<>();
		Set<Set<State>> powerStates = new HashSet<>();

		for (State s : getStates())
			languages.put(s, getNextSymbols(s, n));

		for (State s1 : getStates())
			for (State s2 : getStates())
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

		for (Transition t : getDelta()) {
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
	 * <b>{@code this} is never modified by this method</b>.
	 * 
	 * @param toReplace the automaton recognizing the strings to replace
	 * @param str       the automaton that must be used as replacement
	 * 
	 * @return the replaced automaton
	 */
	public Automaton replace(Automaton toReplace, Automaton str) {
		Collection<Automaton> automata = new ArrayList<>();
		boolean isSingleString = toReplace.getLanguage().size() == 1;
		for (String s : toReplace.getLanguage())
			automata.add(new StringReplacer(this).replace(s, str, isSingleString).collapse());

		if (automata.size() == 1)
			return automata.iterator().next();

		return union(automata.toArray(new Automaton[automata.size()]));
	}

	/**
	 * Applies the given comparison function to all the strings of the languages
	 * of this automaton and the given one. This is a <b>may</b> operation,
	 * meaning that at least one possible pair of strings composed by a string
	 * of the language of {@code this} and a string of the language of
	 * {@code other} must satisfy the property expressed by {@code predicate}.
	 * If this is not the case, this method returns {@code false}.
	 * 
	 * @param other     the other automaton
	 * @param predicate the comparison function between strings of the languages
	 * 
	 * @return {@code true} if that condition hold
	 */
	public boolean mayLanguageCheck(Automaton other, BiPredicate<String, String> predicate) {
		for (String a : getLanguage())
			for (String b : other.getLanguage())
				if (predicate.test(a, b))
					return true;
		return false;
	}

	/**
	 * Yields the automaton that recognizes all possible prefixes of the strings
	 * recognized by this automaton.
	 * 
	 * @return the prefix automaton
	 */
	public Automaton prefix() {
		Automaton result = copy();

		for (State s : result.getStates())
			s.setFinalState(true);

		return result.minimize();
	}

	/**
	 * Yields the automaton that recognizes all possible suffixes of the strings
	 * recognized by this automaton.
	 * 
	 * @return the suffix automaton
	 */
	public Automaton suffix() {
		Automaton result = copy();

		for (State s : result.getStates())
			s.setInitialState(true);

		return result.minimize();
	}

	/**
	 * Yields the automaton that recognizes all possible substrings of the
	 * strings recognized by this automaton.
	 * 
	 * @return the factors automaton
	 */
	public Automaton factors() {
		return prefix().suffix();
	}

	/**
	 * Yields {@code true} if and only if {@code this} is <b>always</b>
	 * contained into {@code other}, that is, if {@code first} is a single path
	 * automaton and its longest string is contained in all paths (without SCCs)
	 * of {@code other}.
	 * 
	 * @param other the other automaton
	 * 
	 * @return {@code true} if that condition holds
	 */
	public boolean mustBeContained(Automaton other) {
		if (hasOnlyOnePath()) {
			Automaton C = extractLongestString();
			String longest = C.getLanguage().iterator().next();

			List<List<State>> paths = other.getPathExtractor().getAllPaths().parallelStream()
					.filter(p -> p.stream().distinct().collect(Collectors.toList()).equals(p))
					.collect(Collectors.toList());

			Set<State> states = new HashSet<>();
			Set<Transition> delta = new HashSet<>();
			for (List<State> p : paths)
				for (State s : p) {
					states.add(s);
					for (Transition t : other.getOutgoingTransitionsFrom(s))
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
	 * Applies the given comparison function to all the strings of the languages
	 * of this automaton and the given one. This is a <b>must</b> operation,
	 * meaning that all possible pairs of strings composed by a string of the
	 * language of {@code this} and a string of the language of {@code other}
	 * must satisfy the property expressed by {@code predicate}. If this is not
	 * the case, this method returns {@code false}.
	 * 
	 * @param other     the other automaton
	 * @param predicate the comparison function between strings of the languages
	 * 
	 * @return {@code true} if that condition hold
	 */
	public boolean mustLanguageCheck(Automaton other, BiPredicate<String, String> predicate) {
		for (String a : getLanguage())
			for (String b : other.getLanguage())
				if (!predicate.test(a, b))
					return false;
		return true;
	}

	/**
	 * Yields the sub-automaton contained in this one that recognizes only the
	 * longest string in the language of {@code this}. Note that this method
	 * assumes that the given automaton is loop-free and that it has only one
	 * path.
	 * 
	 * @return the sub-automaton
	 */
	public Automaton extractLongestString() {
		State lastFinalState = null;

		for (State finalState : getFinalStates()) {
			Set<Transition> outgoingTransaction = getOutgoingTransitionsFrom(finalState);
			if (outgoingTransaction.size() == 0)
				lastFinalState = finalState;
		}

		State nextState = getInitialState();
		String s = "";
		while (!nextState.equals(lastFinalState))
			for (Transition t : getDelta())
				if (t.getFrom().equals(nextState)) {
					nextState = t.getTo();
					s += t.getInput();
				}

		return mkAutomaton(s);
	}
	
	public Automaton toSingleInitalState() {
		if (getInitialStates().size() < 2)
			return this;
		
		Automaton a = copy();
		State newInit = new State("qInit", true, false);
		a.addState(newInit);
		for (State i : getInitialStates()) {
			i.setInitialState(false);
			a.addTransition(i, i, Atom.EPSILON);
		}
		
		return a;
	}
}
