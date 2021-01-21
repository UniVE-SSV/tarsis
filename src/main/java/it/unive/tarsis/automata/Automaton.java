package it.unive.tarsis.automata;

import static it.unive.tarsis.automata.Automata.isContained;
import static it.unive.tarsis.automata.Automata.minimize;

import it.unive.tarsis.automata.algorithms.PathExtractor;
import it.unive.tarsis.automata.algorithms.RegexExtractor;
import it.unive.tarsis.regex.RegularExpression;
import it.unive.tarsis.regex.TopAtom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An automaton, represented as a set of states and a set of transitions.
 * Transitions recognize regular expression of type {@link RegularExpression}.
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class Automaton {

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

	@Override
	public Automaton clone() {
		Set<State> newStates = new HashSet<>();
		Set<Transition> newDelta = new HashSet<>();
		HashMap<String, State> nameToStates = new HashMap<String, State>();

		for (State s : this.states) {
			State newState = new State(s.getState(), s.isInitialState(), s.isFinalState());
			newStates.add(newState);
			nameToStates.put(newState.getState(), newState);
		}

		for (Transition t : this.delta)
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
	 * Equal operator between automata, implemented as:<br>
	 * {@code language(A).equals(language(B))} if both automata are loop free,
	 * {@code A.contains(B) && B.contains(A)} otherwise.<br>
	 * <br>
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
		if (other instanceof Automaton) {
			Automaton a = clone();
			Automaton b = ((Automaton) other).clone();

			if (!a.hasCycle() && !b.hasCycle())
				return a.getLanguage().equals(b.getLanguage());

			a = minimize(a);
			b = minimize(b);

			if (a.hasCycle() && !b.hasCycle() || !a.hasCycle() && b.hasCycle())
				return false;

			if (!a.hasCycle() && !b.hasCycle())
				return a.getLanguage().equals(b.getLanguage());

			if (!isContained(a, b))
				return false;

			if (!isContained(b, a))
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
		Automaton a = minimize(this);

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
}
