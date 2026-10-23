package com.procucev.speechtotext.exception;

public class TranscriptionNotFoundException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6733317780672790329L;

	public TranscriptionNotFoundException(Long id) {
        super("Transcription not found with id: " + id);
    }
}
