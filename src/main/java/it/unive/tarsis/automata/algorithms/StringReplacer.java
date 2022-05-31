package it.unive.tarsis.automata.algorithms;

import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.automata.State;
import it.unive.tarsis.automata.Transition;
import it.unive.tarsis.regex.Atom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * An algorithm that replaces strings across all paths of an automaton.
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class StringReplacer {

	/**
	 * The target automaton
	 */
	private final Automaton origin;

	/**
	 * The string searching algorithm
	 */
	private final StringSearcher searcher;

	/**
	 * Builds the replacer. For this algorithm to work correctly, the target
	 * automaton is first exploded with a call to {@link Automaton#explode()}.
	 * 
	 * @param origin the target automaton
	 */
	public StringReplacer(Automaton origin) {
		this.origin = origin.explode();
		searcher = new StringSearcher(origin);
	}

	/**
	 * Yields a new automaton where every occurrence of {@code toReplace} have
	 * been replaced with {@code str}. If {@code must} is {@code true}, then
	 * this method effectively replaces {@code toReplace}. Otherwise, a
	 * may-replacement is perfomed, meaning that {@code toReplaced} is replaced
	 * with {@code toReplace || str}.
	 * 
	 * @param toReplace the string to replace
	 * @param str       the automaton to use as a replacement
	 * @param must      whether or not a must-replacement has to be made
	 * 
	 * @return the replaced automaton
	 */
	public Automaton replace(String toReplace, Automaton str, boolean must) {
		if (toReplace.isEmpty())
			return emptyStringReplace(str);

		Set<Vector<Transition>> replaceablePaths = searcher.searchInAllPaths(toReplace);

		if (replaceablePaths.isEmpty())
			return origin;

		Automaton replaced = must ? str : str.union(Automaton.mkAutomaton(toReplace));
		AtomicLong counter = new AtomicLong();

		for (Vector<Transition> path : replaceablePaths) {

			// start replacing inputs
			Set<State> statesToRemove = new HashSet<>();
			Set<Transition> edgesToRemove = new HashSet<>();
			for (int i = path.size() - 1; i >= 0; i--) {
				Transition t = path.get(i);
				if (i == path.size() - 1)
					// last step: just remove it;
					edgesToRemove.add(t);
				else
				// we need to check if there is a branch in the destination node
				// in that case, we keep both the transition and the node
				// otherwise, we can remove both of them
				if (origin.getOutgoingTransitionsFrom(t.getTo()).size() < 2) {
					edgesToRemove.add(t);
					statesToRemove.add(t.getTo());
				} else
					// we must stop since we found a branch
					break;
			}

			origin.removeTransitions(edgesToRemove);
			origin.removeStates(statesToRemove);

			// we add the new automaton
			Map<State, State> conversion = new HashMap<>();
			Set<State> states = new HashSet<>();
			Set<Transition> delta = new HashSet<>();

			Function<State, State> maker = s -> new State("r" + counter.getAndIncrement(), false, false);
			for (State origin : replaced.getStates()) {
				State r = conversion.computeIfAbsent(origin, maker);
				states.add(r);
				for (Transition t : replaced.getOutgoingTransitionsFrom(origin)) {
					State dest = conversion.computeIfAbsent(t.getTo(), maker);
					states.add(dest);
					delta.add(new Transition(r, dest, t.getInput()));
				}
			}

			origin.getStates().addAll(states);
			origin.getDelta().addAll(delta);
			for (State s : replaced.getInitialStates())
				origin.addTransition(path.firstElement().getFrom(), conversion.get(s), Atom.EPSILON);
			for (State f : replaced.getFinalStates())
				origin.addTransition(conversion.get(f), path.lastElement().getTo(), Atom.EPSILON);

			origin.recomputeOutgoingAdjacencyList();
		}

		return origin;
	}

	private Automaton emptyStringReplace(Automaton str) {
		AtomicLong counter = new AtomicLong();
		Set<State> states = new HashSet<>();
		Set<Transition> delta = new HashSet<>();

		// states will be a superset of the original ones,
		// except that all final states are tuned non-final:
		// all paths will end with `str`
		Map<State, State> mapper = new HashMap<>();
		origin.getStates().forEach(s -> mapper.put(s, new State(s.getState(), s.isInitialState(), false)));
		states.addAll(mapper.values());

		Function<State, State> maker = s -> new State("r" + counter.getAndIncrement(), false, false);

		for (Transition t : origin.getDelta()) {
			Map<State, State> conversion = new HashMap<>();

			for (State origin : str.getStates()) {
				State r = conversion.computeIfAbsent(origin, maker);
				states.add(r);
				for (Transition tt : str.getOutgoingTransitionsFrom(origin)) {
					State dest = conversion.computeIfAbsent(tt.getTo(), maker);
					states.add(dest);
					delta.add(new Transition(r, dest, tt.getInput()));
				}
			}

			for (State s : str.getInitialStates())
				delta.add(new Transition(mapper.get(t.getFrom()), conversion.get(s), Atom.EPSILON));
			for (State f : str.getFinalStates())
				delta.add(new Transition(conversion.get(f), mapper.get(t.getTo()), t.getInput()));
		}

		maker = s -> new State("r" + counter.getAndIncrement(), false, s.isFinalState());
		for (State f : origin.getFinalStates()) {
			Map<State, State> conversion = new HashMap<>();

			for (State origin : str.getStates()) {
				State r = conversion.computeIfAbsent(origin, maker);
				states.add(r);
				for (Transition tt : str.getOutgoingTransitionsFrom(origin)) {
					State dest = conversion.computeIfAbsent(tt.getTo(), maker);
					states.add(dest);
					delta.add(new Transition(r, dest, tt.getInput()));
				}
			}

			for (State s : str.getInitialStates())
				delta.add(new Transition(f, conversion.get(s), Atom.EPSILON));
		}

		return new Automaton(delta, states);
	}
}
