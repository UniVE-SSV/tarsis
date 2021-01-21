package it.unive.tarsis.automata;

import it.unive.tarsis.regex.RegularExpression;
import it.unive.tarsis.regex.TopAtom;

/**
 * A transition connecting two states of the automaton.
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class Transition {

	/**
	 * The source state
	 */
	private State from;

	/**
	 * The destination state
	 */
	private State to;

	/**
	 * The symbol recognized by the transition
	 */
	private RegularExpression input;

	/**
	 * Builds a new transition.
	 * 
	 * @param from  the source state
	 * @param to    the destination state
	 * @param input the symbol recognized
	 */
	public Transition(State from, State to, RegularExpression input) {
		this.from = from;
		this.to = to;
		this.input = input;
	}

	/**
	 * Yields the source state of this transition.
	 * 
	 * @return the source state
	 */
	public State getFrom() {
		return from;
	}

	/**
	 * Yields the destination state of this transition.
	 * 
	 * @return the destination state
	 */
	public State getTo() {
		return to;
	}

	/**
	 * Yields the symbol recognized by this transition.
	 * 
	 * @return the symbol
	 */
	public RegularExpression getInput() {
		return input;
	}

	/**
	 * Yields {@code true} if and only if this transition recognizes <b>only</b>
	 * the empty string.
	 * 
	 * @return {@code true} if and only if that condition holds
	 */
	public boolean isEpsilonTransition() {
		return getInput() instanceof TopAtom ? false : getInput().toString().isEmpty();
	}

	@Override
	public String toString() {
		return from.getState() + " [" + input + "] -> " + to.getState();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((input == null) ? 0 : input.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transition other = (Transition) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (input == null) {
			if (other.input != null)
				return false;
		} else if (!input.equals(other.input))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}
}
