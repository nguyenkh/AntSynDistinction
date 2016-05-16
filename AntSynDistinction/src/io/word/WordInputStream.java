package io.word;

import java.io.IOException;

public interface WordInputStream {
    /**
     * get the next word from the stream
     * 
     * @return A string as the next word
     *         If it's the end of the stream, return ""
     *         If it's the end of a sentence, return "</s>"
     * @throws IOException
     */
    public String readWord() throws IOException;

    /**
     * Check if the we reach the end of the stream
     * Seem a bit redundant since we
     * can get this information
     * 
     * @return true: if the end of the stream is reach false: otherwise
     */
    public boolean endOfFile();

    /**
     * close the stream
     * 
     * @throws IOException
     */
    public void close() throws IOException;
}
