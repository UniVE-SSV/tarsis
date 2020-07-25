package it.unive.tarsis.strings;

/**
 * An extended character, that is, an object that single character. This class
 * is needed to be able to represent an unknown character.
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class ExtChar {

	/**
	 * The underlying character
	 */
	private final char ch;

	/**
	 * Builds the extended character
	 * 
	 * @param ch the underlying character
	 */
	public ExtChar(char ch) {
		this.ch = ch;
	}

	/**
	 * Converts this extended character to a normal character.
	 * 
	 * @return the character
	 */
	public char asChar() {
		return ch;
	}

	/**
	 * Yields {@code true} if and only if this extended character represent the given one.
	 * 
	 * @param ch the character
	 * @return {@code true} if that condition holds
	 */
	public boolean is(char ch) {
		return this.ch == ch;
	}

	@Override
	public int hashCode() {
		return Character.hashCode(ch);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;

		ExtChar other = (ExtChar) obj;
		if (ch != other.ch)
			return false;

		return true;
	}

	@Override
	public String toString() {
		return String.valueOf(ch);
	}
}
