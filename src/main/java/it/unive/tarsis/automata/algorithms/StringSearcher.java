package it.unive.tarsis.automata.algorithms;

import it.unive.tarsis.automata.Automata;
import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.automata.State;
import it.unive.tarsis.automata.Transition;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * An algorithm that searches strings across all paths of an automaton.
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class StringSearcher {

	/**
	 * The target automaton
	 */
	private final Automaton automaton;

	/**
	 * The string to search
	 */
	private String searchString;

	/**
	 * True if and only if we are currently matching characters
	 */
	private boolean matching;

	/**
	 * Builds the searcher. For this algorithm to work correctly, the target
	 * automaton should be exploded first with a call to
	 * {@link Automata#explode(Automaton)}.
	 * 
	 * @param origin the target automaton
	 */
	public StringSearcher(Automaton origin) {
		automaton = origin;
		searchString = null;
		matching = false;
	}

	/**
	 * Yields a set containing all the sequences of transitions that recognize
	 * the given string.
	 * 
	 * @param toSearch the string to search
	 * 
	 * @return the set of sequences of transitions
	 */
	public Set<Vector<Transition>> searchInAllPaths(String toSearch) {
		Set<Vector<Transition>> collected = new HashSet<>();

		Set<List<State>> paths = automaton.getPathExtractor().getAllPaths();

		if (paths.size() == 0)
			return collected;

		for (List<State> v : paths)
			collected.addAll(searchInPath(v, toSearch));

		return collected;
	}

	@SuppressWarnings("unchecked")
	private Set<Vector<Transition>> searchInPath(List<State> v, String toSearch) {

		Set<Vector<Transition>> collected = new HashSet<>();
		if (v.size() == 1 && toSearch.length() == 1)
			return handleSelfLoop(v, collected);

		Vector<Transition> path = new Vector<>();
		resetSearchState(path, toSearch);
		for (int i = 0; i < v.size() - 1; i++) {
			State from = v.get(i);
			State to = v.get(i + 1);
			Set<Transition> transitions = automaton.getAllTransitionsConnecting(from, to);

			if (transitions.size() == 0)
				continue;

			for (Transition t : transitions) {
				if (matching)
					if (t.getInput().is(searchString.substring(0, 1)))
						// we found a matching char
						advanceSearch(path, t);
					else {
						resetSearchState(path, toSearch);
						if (t.getInput().is(searchString.substring(0, 1)))
							startSearch(path, t);
					}
				else if (t.getInput().is(searchString.substring(0, 1)))
					// we found the beginning of the string
					startSearch(path, t);
			}

			if (searchString.isEmpty()) {
				collected.add((Vector<Transition>) path.clone());
				resetSearchState(path, toSearch);
			}

		}

		return collected;
	}

	private Set<Vector<Transition>> handleSelfLoop(List<State> v, Set<Vector<Transition>> collected) {
		// self loop!
		Set<Transition> transitions = automaton.getAllTransitionsConnecting(v.get(0), v.get(0));

		if (transitions.size() == 0)
			return collected;

		for (Transition t : transitions)
			if (t.getInput().is(searchString.substring(0, 1))) {
				Vector<Transition> result = new Vector<>();
				result.add(t);
				collected.add(result);
			}

		return collected;
	}

	private void advanceSearch(Vector<Transition> path, Transition t) {
		searchString = searchString.substring(1);
		path.add(t);
	}

	private void startSearch(Vector<Transition> path, Transition t) {
		matching = true;
		advanceSearch(path, t);
	}

	private void resetSearchState(Vector<Transition> path, String toSearch) {
		matching = false;
		searchString = toSearch;
		path.clear();
	}
}
