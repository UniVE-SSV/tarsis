package it.unive.tarsis.automata;

/**
 * A state of the automaton.
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class State {

	/**
	 * The name of the state
	 */
	private String state;

	/**
	 * True if and only if this state is an accepting state
	 */
	private boolean isFinalState;

	/**
	 * True if and only if this state is an initial state
	 */
	private boolean isInitialState;

	/**
	 * Builds a new state.
	 * 
	 * @param state          the name of the state
	 * @param isInitialState true if and only if this state is an accepting
	 *                           state
	 * @param isFinalState   true if and only if this state is an initial state
	 */
	public State(String state, boolean isInitialState, boolean isFinalState) {
		this.state = state;
		this.isFinalState = isFinalState;
		this.isInitialState = isInitialState;
	}

	/**
	 * Yields {@code true} if and only if this state is an initial state.
	 * 
	 * @return {@code true} if that condition holds
	 */
	public boolean isInitialState() {
		return isInitialState;
	}

	/**
	 * Updates the property of this state of being an initial state according to
	 * the given boolean.
	 * 
	 * @param isInitialState whether or not this state has to be marked as
	 *                           initial state
	 */
	public void setInitialState(boolean isInitialState) {
		this.isInitialState = isInitialState;
	}

	/**
	 * Yields the name of this state.
	 * 
	 * @return the name
	 */
	public String getState() {
		return state;
	}

	/**
	 * Updates the name of this state.
	 * 
	 * @param state the new name
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * Yields {@code true} if and only if this state is a final state.
	 * 
	 * @return {@code true} if that condition holds
	 */
	public boolean isFinalState() {
		return isFinalState;
	}

	/**
	 * Updates the property of this state of being a final state according to
	 * the given boolean.
	 * 
	 * @param isFinalState whether or not this state has to be marked as final
	 *                         state
	 */
	public void setFinalState(boolean isFinalState) {
		this.isFinalState = isFinalState;
	}

	@Override
	public String toString() {
		return "[" + this.getState() + "]" + (isInitialState ? "[init]" : "") + (isFinalState ? "[final]" : "");
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof State)
			return this.getState().equals(((State) other).getState());
		return false;
	}

	@Override
	public int hashCode() {
		return this.getState().hashCode();
	}

	@Override
	public State clone() {
		return new State(this.getState(), this.isInitialState, this.isFinalState);
	}
}
