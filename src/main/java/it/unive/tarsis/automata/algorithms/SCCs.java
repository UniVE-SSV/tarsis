package it.unive.tarsis.automata.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.automata.State;
import it.unive.tarsis.automata.Transition;

/**
 * The SCCs of an automaton, represented as a set of sets of states.
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class SCCs extends HashSet<Set<State>> {

	private static final long serialVersionUID = -6255520662881648061L;

	/**
	 * Yields the SCCs of the given automaton.
	 * 
	 * @param a the automaton
	 * @return the SCCs
	 */
	public static SCCs getSCCs(Automaton a) {
		return extendedTarjan(a);
	}

	private static SCCs extendedTarjan(Automaton a) {
		Map<State, Integer> indexes = new HashMap<>();
		Map<State, Integer> lowlink = new HashMap<>();
		Set<State> onStack = new HashSet<>();

		Stack<State> stack = new Stack<>();

		int index = 0;

		SCCs partialResult = new SCCs();
		SCCs result = new SCCs();

		for (State v : a.getStates()) 
			if (!indexes.containsKey(v)) {
				partialResult.clear();
				strongConnect(a, v, indexes, lowlink, onStack, stack, index, partialResult);
				for (Set<State> r : partialResult)
					if (!(r.isEmpty()))
						result.add(r);
			}

		Set<Set<State>> toRemove = new HashSet<>();

		for (Set<State> scc : result) {
			boolean isSCC = false;
			if (scc.size() == 1) {
				for (State v : scc)
					for (Transition t : a.getOutgoingTransitionsFrom(v))
						if (t.getTo().equals(v)) {
							isSCC = true;
							break;
						}

				if (!isSCC)
					toRemove.add(scc);
			}
		}

		SCCs realResult = new SCCs();
		result.removeAll(toRemove);

		for (Set<State> scc : result) 
			for (State s1 : scc) {
				Set<State> ns = new HashSet<>(scc);
				for (State s2 : a.getStates())
					if (!scc.contains(s2) && !s1.equals(s2) && a.areMutuallyReachable(s1, s2))
						ns.add(s2);

				realResult.add(ns);
			}

		return realResult;
	}

	private static void strongConnect(Automaton a, State v, Map<State, Integer> indexes,
			Map<State, Integer> lowlink, Set<State> onStack, Stack<State> stack, int index,
			Set<Set<State>> result) {

		// Set the depth index for v to the smallest unused index
		indexes.put(v, index);
		lowlink.put(v, index);
		index++;
		stack.push(v);
		onStack.add(v);

		// Consider successors of v
		for (Transition t : a.getOutgoingTransitionsFrom(v)) {

			// Reached state from v
			State w = t.getTo();
			if (!indexes.containsKey(w)) {
				// Successor w has not yet visit, recurse on it
				strongConnect(a, w, indexes, lowlink, onStack, stack, index, result);
				lowlink.put(v, Math.min(lowlink.get(v), lowlink.get(w)));
			} else if (onStack.contains(w)) {
				// Successor w is in stack S and hence in the current SCC
				// If w is not on stack, then (v, w) is a cross-edge in the DFS tree and must be
				// ignored
				// Note: The next line may look odd - but is correct.
				// It says w.index not w.lowlink; that is deliberate and from the original paper
				lowlink.put(v, Math.min(lowlink.get(v), indexes.get(w)));
			}
		}

		if (lowlink.get(v) == indexes.get(v)) {
			Set<State> scc = new HashSet<>();

			// Start a new strongly connected component
			State w = null;

			do {
				w = stack.pop();
				onStack.remove(w);
				scc.add(w);
			} while (!(w.equals(v)));

			result.add(scc);
		}

	}
}
