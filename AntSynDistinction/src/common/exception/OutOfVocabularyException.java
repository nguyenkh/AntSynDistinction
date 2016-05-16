package common.exception;

public class OutOfVocabularyException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public OutOfVocabularyException(String msg) {
        super(msg);
    }

}
