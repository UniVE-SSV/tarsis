package it.unive.tarsis.automata.algorithms;

import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.automata.State;
import it.unive.tarsis.automata.Transition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.apache.commons.lang3.tuple.Triple;

/**
 * An algorithm that extracts paths from an automaton.
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class PathExtractor {

	/**
	 * The target automaton
	 */
	private final Automaton automaton;

	/**
	 * Builds the extractor.
	 * 
	 * @param a the automaton
	 */
	public PathExtractor(Automaton a) {
		this.automaton = a;
	}

	/**
	 * Yields all possible paths going from an initial state to a final state in
	 * the target automaton. Note that each node of an SCC of the automaton will
	 * appear at most once in each path.
	 * 
	 * @return the set of all possible paths
	 */
	public Set<List<State>> getAllPaths() {
		Set<Transition[]> paths = depthFirst(automaton.getInitialState());

		Set<List<State>> result = new HashSet<>();
		for (Transition[] tt : paths) {
			List<State> path = new ArrayList<>(tt.length + 1);

			for (Transition t : tt) {
				path.add(t.getFrom());

				if (t.getTo().isFinalState())
					path.add(t.getTo());
			}

			simplify(path);
			result.add(path);
		}

		return result;
	}

	private Set<Transition[]> depthFirst(State src) {
		Set<Transition[]> paths = new HashSet<>();
		Stack<Triple<State, Transition[], int[]>> ws = new Stack<>();
		ws.push(Triple.of(src, new Transition[0], new int[0]));

		do {
			Triple<State, Transition[], int[]> current = ws.pop();
			State node = current.getLeft();
			Transition[] visited = current.getMiddle();
			int[] hashes = current.getRight();
			int len = visited.length;

			Set<Transition> tr = automaton.getOutgoingTransitionsFrom(node);

			transitions: for (Transition t : tr) {
				int thash = t.hashCode();

				int count = 0;
				for (int i = 0; i < len; i++) {
					// we look at the element's hash before invoking the
					// actual comparison for fast failure
					if (visited[i] == t || (hashes[i] == thash && t.equals(visited[i])))
						count++;

					if (count > 1)
						continue transitions;
				}

				Transition[] copy = Arrays.copyOf(visited, len + 1);
				int[] hashesCopy = Arrays.copyOf(hashes, len + 1);
				copy[len] = t;
				hashesCopy[len] = thash;

				if (t.getTo().isFinalState())
					paths.add(copy);
				ws.push(Triple.of(t.getTo(), copy, hashesCopy));
			}
		} while (!ws.isEmpty());

		return paths;
	}

	/**
	 * Finds the minimum path between the two given states using the Dijkstra
	 * algorithm.
	 * 
	 * @param src    the source node
	 * @param target the destination node
	 * 
	 * @return the minimum path
	 */
	public List<State> minimumDijkstra(State src, State target) {
		Set<State> unSettledNodes = new HashSet<>();
		Map<State, Integer> distance = new HashMap<>();
		Map<State, State> predecessors = new HashMap<>();

		distance.put(src, 0);
		unSettledNodes.add(src);

		while (unSettledNodes.size() > 0) {
			State node = getMinimum(unSettledNodes, distance);
			unSettledNodes.remove(node);
			findMinimalDistances(node, distance, predecessors, unSettledNodes);
		}

		return getPath(target, predecessors);
	}

	private void findMinimalDistances(State node, Map<State, Integer> distance, Map<State, State> predecessors,
			Set<State> unSettledNodes) {
		Set<State> adjacentNodes = automaton.getNextStates(node);
		for (State target : adjacentNodes)
			if (getShortestDistance(target, distance) > getShortestDistance(node, distance)
					+ getDistance(node, target)) {
				distance.put(target, getShortestDistance(node, distance) + getDistance(node, target));
				predecessors.put(target, node);
				unSettledNodes.add(target);
			}
	}

	private int getDistance(State node, State target) {
		for (Transition edge : automaton.getDelta())
			if (edge.getFrom().equals(node) && edge.getTo().equals(target))
				return 1;
		// should never happen
		return -1;
	}

	private static State getMinimum(Set<State> vertexes, Map<State, Integer> distance) {
		State minimum = null;
		for (State vertex : vertexes)
			if (minimum == null)
				minimum = vertex;
			else if (getShortestDistance(vertex, distance) < getShortestDistance(minimum, distance))
				minimum = vertex;

		return minimum;
	}

	private static int getShortestDistance(State destination, Map<State, Integer> distance) {
		Integer d = distance.get(destination);
		if (d == null)
			return Integer.MAX_VALUE;
		else
			return d;
	}

	private static List<State> getPath(State target, Map<State, State> predecessors) {
		List<State> path = new LinkedList<>();
		State step = target;

		// check if a path exists
		if (predecessors.get(step) == null) {
			path.add(target);
			return path;
		}

		path.add(step);
		while (predecessors.get(step) != null) {
			step = predecessors.get(step);
			path.add(step);
		}

		// Put it into the correct order
		Collections.reverse(path);
		return path;
	}

	private static void simplify(List<State> path) {
		ListIterator<State> it = path.listIterator();
		while (it.hasNext()) {
			if (!it.hasPrevious()) {
				it.next();
				continue;
			}

			it.previous(); // we move back one to get the two elements to
							// compare
			State previous = it.next();
			State current = it.next();
			if (previous.equals(current))
				it.remove();
		}
	}
}
