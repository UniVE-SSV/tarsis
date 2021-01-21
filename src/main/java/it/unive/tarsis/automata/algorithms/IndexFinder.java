package it.unive.tarsis.automata.algorithms;

import it.unive.tarsis.automata.Automata;
import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.automata.State;
import it.unive.tarsis.automata.Transition;
import it.unive.tarsis.regex.TopAtom;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

/**
 * An algorithm that finds all possible indexes of the first occurrences of an
 * automaton into another one.
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class IndexFinder {

	/**
	 * Yields the minimum and maximum indexes where {@code search} first appears
	 * in {@code automaton}. If the second element of the returned pair is
	 * {@code null}, it means infinity, that is, there is at least a top
	 * transition or a loop preceding one of the first matches.<br>
	 * <br>
	 * Note that, if {@code search} accepts the empty string, the returned pair
	 * will be {@code 0, automaton.maxLengthString()}, or {@code 0, null} if
	 * {@code automaton} is not finite.
	 * 
	 * @param automaton the automaton
	 * @param search    the automaton to search
	 * 
	 * @return a pair of integers, representing the minimum and maximum indexes
	 *             where {@code search} first appears in {@code automaton}, or
	 *             {@code null} to represent infinity
	 */
	public static Pair<Integer, Integer> findIndexesOf(Automaton automaton, Automaton search) {
		Automaton exploded = Automata.explode(automaton);
		StringSearcher searcher = new StringSearcher(exploded);
		Set<List<State>> allPaths = exploded.getPathExtractor().getAllPaths();
		Set<Transition> topTransitions = exploded.getDelta().parallelStream()
				.filter(t -> t.getInput() == TopAtom.INSTANCE).collect(Collectors.toSet());
		int min = Integer.MAX_VALUE;
		int max = -1;
		boolean maxIsInfinity = true;
		int index;
		for (String s : search.getLanguage()) {
			if (s.isEmpty())
				// we can directly return 0, len(aut)
				return Pair.of(0,
						automaton.acceptsTopEventually() || automaton.hasCycle() ? null : automaton.maxLengthString());
			Set<Vector<Transition>> matching = searcher.searchInAllPaths(s);
			for (List<State> p : allPaths) {
				int lower = Integer.MAX_VALUE;
				boolean isInfinity = false;
				int missing = 0;
				for (Vector<Transition> match : matching)
					// we search for the minimum position along this path
					// where the string can be found
					if ((index = p.indexOf(match.firstElement().getFrom())) != -1
							&& (match.size() < 2 || p.indexOf(match.get(1).getFrom()) == index + 1)) {
						int i = index;
						long tops = topTransitions.parallelStream().map(t -> Pair.of(t.getFrom(), t.getTo()))
								.map(pair -> Pair.of(p.indexOf(pair.getLeft()), p.indexOf(pair.getRight())))
								.filter(pair -> pair.getLeft() == pair.getRight() - 1)
								.filter(pair -> pair.getLeft() < i).count();
						index -= tops; // TOP should not count
						if (index <= lower) {
							if (tops > 0) {
								if (index < lower)
									isInfinity = true;
							} else
								isInfinity = false;

							lower = index;
						}
					} else
						missing++;

				if (missing == matching.size())
					min = -1;
				else if (lower <= min) {
					if (isInfinity) {
						if (lower < min)
							maxIsInfinity = true;
					} else
						maxIsInfinity = false;

					min = lower;
				}

				if (lower >= max) {
					if (isInfinity) {
						if (lower > max)
							maxIsInfinity = true;
					} else
						maxIsInfinity = false;

					max = lower;
				}
			}
		}

		return Pair.of(min, maxIsInfinity ? null : max);
	}
}
