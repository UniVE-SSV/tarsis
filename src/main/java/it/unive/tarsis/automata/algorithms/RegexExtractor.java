package it.unive.tarsis.automata.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.automata.State;
import it.unive.tarsis.automata.Transition;
import it.unive.tarsis.regex.Atom;
import it.unive.tarsis.regex.Comp;
import it.unive.tarsis.regex.EmptySet;
import it.unive.tarsis.regex.Or;
import it.unive.tarsis.regex.RegularExpression;
import it.unive.tarsis.regex.Star;

/**
 * An algorithm that extract regular expressions from automata.
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class RegexExtractor {

	/**
	 * Yields the minimal regular expression equivalent to the given automaton.
	 * 
	 * @param a the automaton
	 * 
	 * @return the equivalent regular expression
	 */
	public static RegularExpression getMinimalRegex(Automaton a) {
		return getMinimalBrzozowskiRegex(a);
	}

	/**
	 * Yields the set of regular expressions that can be recognized by the given
	 * automaton by extracting one from each path of the automaton.
	 * 
	 * @param a the automaton
	 * 
	 * @return the set of regular expressions
	 */
	public static Set<RegularExpression> getRegexesFromPaths(Automaton a) {
		Set<RegularExpression> result = new HashSet<>();

		for (List<State> p : a.getPathExtractor().getAllPaths())
			for (RegularExpression r : getRegexFromPath(a, p))
				result.add(getMinimalBrzozowskiRegex(r.toAutomaton()));

		return result;
	}

	private static Set<RegularExpression> getRegexFromPath(Automaton a, List<State> p) {
		Set<RegularExpression> result = new HashSet<>();
		Set<RegularExpression> partial = new HashSet<>();
		Set<RegularExpression> toAdd = new HashSet<>();
		Set<RegularExpression> toRemove = new HashSet<>();

		SCCs sccs = SCCs.getSCCs(a);
		partial.add(Atom.EPSILON);

		for (int i = 0; i < p.size() - 1; i++) {
			partial.addAll(toAdd);
			partial.removeAll(toRemove);

			toRemove.clear();
			toAdd.clear();

			if (i == 1)
				partial.remove(Atom.EPSILON);

			if (sccs.isEmpty())
				for (RegularExpression s : partial)
					for (Transition t : a.getAllTransitionsConnecting(p.get(i), p.get(i + 1))) {
						toAdd.add(new Comp(s, t.getInput()).simplify());

						if (p.get(i + 1).isFinalState())
							result.add(new Comp(s, t.getInput()).simplify());
						else
							toRemove.add(s);
					}

			for (Set<State> scc : sccs)
				if (scc.contains(p.get(i)) && scc.contains(p.get(i + 1))) {
					int j = i;
					Set<State> sccInpath = new HashSet<>();
					for (j = i; j < p.size(); j++)
						if (!scc.contains(p.get(j)))
							break;
						else
							sccInpath.add(p.get(j));
					j--;

					RegularExpression regex = getSCCRegex(a, sccInpath, p.get(i), p.get(j));

					for (RegularExpression s : partial) {
						toAdd.add(new Comp(s, regex).simplify());

						if (p.get(j).isFinalState())
							result.add(new Comp(s, regex).simplify());

						toRemove.add(s);
					}

					i = j - 1;
					continue;
				} else if (scc.contains(p.get(i)) && scc.size() == 1) {
					int j = i;
					Set<State> sccInpath = new HashSet<>();
					sccInpath.add(p.get(i));
					RegularExpression regex = getSCCRegex(a, sccInpath, p.get(i), p.get(i));

					for (RegularExpression s : partial) {
						for (Transition t : a.getAllTransitionsConnecting(p.get(i), p.get(i + 1)))
							toAdd.add(new Comp(new Comp(s, regex), t.getInput()).simplify());

						if (p.get(j).isFinalState())
							result.add(new Comp(s, regex).simplify());

						toRemove.add(s);
					}
				} else
					for (RegularExpression s : partial)
						for (Transition t : a.getAllTransitionsConnecting(p.get(i), p.get(i + 1))) {
							toAdd.add(new Comp(s, t.getInput()).simplify());

							if (p.get(i + 1).isFinalState())
								result.add(new Comp(s, t.getInput()).simplify());
							toRemove.add(s);
						}
		}

		partial.addAll(toAdd);
		partial.removeAll(toRemove);

		toRemove.clear();
		toAdd.clear();
		int i = p.size() - 1;

		for (Set<State> scc : sccs)
			if (scc.contains(p.get(i))) {
				int j = i;
				Set<State> sccInpath = new HashSet<>();
				for (j = i; j < p.size(); j++)
					if (!scc.contains(p.get(j)))
						break;
					else
						sccInpath.add(p.get(j));
				j--;

				RegularExpression regex = getSCCRegex(a, sccInpath, p.get(i), p.get(j));

				for (RegularExpression s : partial) {
					result.add(new Comp(s, regex).simplify());
					toRemove.add(s);
				}

				break;
			}

		if (!sccs.isEmpty() && !involvedIntoSCC(sccs, p.get(i)))
			result.addAll(partial);

		return result;
	}

	private static boolean involvedIntoSCC(Set<Set<State>> sccs, State s) {
		for (Set<State> scc : sccs)
			if (scc.contains(s))
				return true;

		return false;
	}
	
	private static State nextNonInitialState(Iterator<State> it) {
		while (it.hasNext()) {
			State cursor = it.next();
			if (!cursor.isInitialState())
				return cursor;
		}
		
		return null;
	}

	/**
	 * https://cs.stackexchange.com/questions/2016/how-to-convert-finite-automata-to-regular-expressions
	 * - (Brzozowski algebraic method)
	 */
	private static RegularExpression getBrzozowskiRegex(Automaton a) {
		a = a.toSingleInitalState();
		int m = a.getStates().size();

		// in both A and B, the initial state is at index 0
		RegularExpression[][] A = new RegularExpression[m][m];
		RegularExpression[] B = new RegularExpression[m];

		Map<Integer, State> mapping = new HashMap<>();
		mapping.put(0, a.getInitialState());
		
		// NOTE: algorithm's indexing is 1-based, java is 0-based
		
		/*
		for i = 1 to m:
		 	if final(i):
		    	B[i] := ε
		  	else:
		    	B[i] := ∅
		for i = 1 to m:
  			for j = 1 to m:
		    	for a in Σ:
			      	if trans(i, a, j):
			        	A[i,j] := a
			      	else:
			        	A[i,j] := ∅
		*/
		Iterator<State> it = a.getStates().iterator();
		for (int i = 0; i < m; i++) {
			State source = mapping.get(i);
			if (source == null) {
				source = nextNonInitialState(it);
				mapping.put(i, source);
			}

			if (source.isFinalState())
				B[i] = Atom.EPSILON;
			else
				B[i] = EmptySet.INSTANCE;
			
			for (int j = 0; j < m; j++) {
				State dest = mapping.get(j);
				if (dest == null) {
					dest = nextNonInitialState(it);
					mapping.put(j, dest);
				}
				
				Iterator<Transition> tt = a.getAllTransitionsConnecting(source, dest).iterator();
				if (!tt.hasNext())
					A[i][j] = EmptySet.INSTANCE; 
				else {
					A[i][j] = tt.next().getInput(); 
					while (tt.hasNext())
						A[i][j] = new Or(A[i][j], tt.next().getInput());
				}
			}				
		}

		/*
		for n = m decreasing to 1:
		  	B[n] := star(A[n,n]) . B[n]
		  	for j = 1 to n:
		    	A[n,j] := star(A[n,n]) . A[n,j];
		  	for i = 1 to n:
		    	B[i] += A[i,n] . B[n]
		    	for j = 1 to n:
		      		A[i,j] += A[i,n] . A[n,j]
		 */
		for (int n = m - 1; n >= 0; n--) {
			Star star_nn = new Star(A[n][n]);
			B[n] = new Comp(star_nn, B[n]);

			for (int j = 0; j < n; j++)
				A[n][j] = new Comp(star_nn, A[n][j]);

			for (int i = 0; i < n; i++) {
				B[i] = new Or(B[i], new Comp(A[i][n], B[n]));

				for (int j = 0; j < n; j++)
					A[i][j] = new Or(A[i][j], new Comp(A[i][n], A[n][j]));
			}
		}

		return B[0].simplify();
	}

	/**
	 * Yields the minimal regular expression equivalent to the given automaton
	 * through the Brozozowski algebraic method. The regular expression is
	 * simplified through heuristics until a fixpoint is reached.
	 * 
	 * @param a the automaton
	 * 
	 * @return the equivalent regular expression
	 */
	public static RegularExpression getMinimalBrzozowskiRegex(Automaton a) {
		RegularExpression regex, simplified = getBrzozowskiRegex(a);

		do {
			regex = simplified;
			simplified = simplified.simplify();
		} while (!regex.equals(simplified));

		return simplified;
	}

	private static RegularExpression getSCCRegex(Automaton a, Set<State> scc, State entry, State exit) {
		Automaton clone = a.clone();
		Set<State> statesToRemove = new HashSet<>();
		Set<Transition> transitionsToRemove = new HashSet<>();

		for (State s : clone.getStates()) {
			if (!scc.contains(s)) {
				statesToRemove.add(s);
				continue;
			}

			if (s.getState().equals(entry.getState()))
				s.setInitialState(true);

			if (s.getState().equals(exit.getState()))
				s.setFinalState(true);
		}

		clone.getStates().removeAll(statesToRemove);

		for (Transition t : clone.getDelta())
			if (!scc.contains(t.getTo()) || !scc.contains(t.getFrom()))
				transitionsToRemove.add(t);

		clone.removeTransitions(transitionsToRemove);
		clone = clone.minimize();

		return getBrzozowskiRegex(clone);
	}
}
