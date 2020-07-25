package it.unive.tarsis.automata.algorithms;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.commons.lang3.tuple.Pair;

import it.unive.tarsis.automata.Automaton;
import it.unive.tarsis.automata.State;
import it.unive.tarsis.automata.Transition;

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
	 * Yields all possible paths going from an initial state to a final state in the
	 * target automaton. Note that each node of an SCC of the automaton will appear
	 * at most once in each path.
	 * 
	 * @return the set of all possible paths
	 */
	public Set<List<State>> getAllPaths() {
		Set<List<Transition>> paths = ConcurrentHashMap.newKeySet();
		Set<List<State>> result = new HashSet<>();
		depthFirst(automaton.getInitialState(), paths);

		for (List<Transition> tt : paths) {
			List<State> path = new LinkedList<>();

			for (Transition t : tt) {
				path.add(t.getFrom());

				if (t.getTo().isFinalState())
					path.add(t.getTo());

			}

			result.add(simplify(path));
		}

		return result;
	}

	private void depthFirst(State src, Set<List<Transition>> paths) {
		Stack<Pair<State, Deque<Transition>>> ws = new Stack<>();
		ws.push(Pair.of(src, new ConcurrentLinkedDeque<>()));
		
		do {
			Pair<State, Deque<Transition>> current = ws.pop();
			State node = current.getLeft();
			Deque<Transition> visited = current.getRight();
			
			Set<Transition> tr = automaton.getOutgoingTransitionsFrom(node);
			
			transitions:
			for (Transition t : tr) {
				int count = 0;
				for (Transition in : visited) 
					if (in.equals(t) && ++count > 1)
						continue transitions;

				visited.add(t);

				if (t.getTo().isFinalState()) 
					paths.add(new LinkedList<>(visited));
				ws.push(Pair.of(t.getTo(), new LinkedList<>(visited)));

				visited.removeLast();				
			}
		} while (!ws.isEmpty());
	}

	/**
	 * Finds the minimum path between the two given states using the Dijkstra
	 * algorithm.
	 * 
	 * @param src    the source node
	 * @param target the destination node
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

	private static LinkedList<State> getPath(State target, Map<State, State> predecessors) {
		LinkedList<State> path = new LinkedList<State>();
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

	private List<State> simplify(List<State> path) {
		List<State> result = new LinkedList<>();
		for (int i = 0; i < path.size(); i++)
			if (i == path.size() - 1)
				result.add(path.get(i));
			else if (path.get(i).equals(path.get(i + 1))) {
				int j = i;
				for (j = i; j < path.size() - 1; j++)
					if (!path.get(j).equals(path.get(j + 1)))
						break;
				result.add(path.get(j));
				i = j;
			} else
				result.add(path.get(i));

		return result;
	}
}
