package it.unive.tarsis.strings;

import it.unive.tarsis.regex.TopAtom;

/**
 * An {@link ExtChar} representing an unknown character.
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class TopExtChar extends ExtChar {
	
	/**
	 * The singleton instance
	 */
	public static final TopExtChar INSTANCE = new TopExtChar();
	
	private TopExtChar() {
		super(TopAtom.STRING.charAt(0));
	}
	
	@Override
	public boolean is(char ch) {
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof TopExtChar;
	}
}
