package io.word;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class PushBackWordStream implements WordInputStream {
    protected PushbackInputStream inputStream;
    protected int                 maxWordLength;
    boolean                       reachedEndOfFile = false;

    public PushBackWordStream(String filePath, int maxWordLength)
            throws IOException {
        this.maxWordLength = maxWordLength;
        inputStream = new PushbackInputStream(new BufferedInputStream(
                new FileInputStream(filePath)));
    }

    public PushBackWordStream(InputStream is, int maxWordLength) {
        this.maxWordLength = maxWordLength;
        inputStream = new PushbackInputStream(new BufferedInputStream(is));
    }

    @Override
    public String readWord() throws IOException {
        StringBuffer buff = new StringBuffer();
        boolean newString = true;
        char ch;
        while (true) {
            int nextCh = inputStream.read();
            if (nextCh == -1) {
                reachedEndOfFile = true;
                break;
            }
            ch = (char) nextCh;
            // for window character
            if (ch == 13)
                continue;
            if ((ch == ' ') || (ch == '\t') || (ch == '\n')) {
                if (!newString) {
                    if (ch == '\n') {
                        inputStream.unread(ch);
                    }
                    break;
                }
                // end of line = end of sentence
                if (ch == '\n') {
                    return "</s>";
                } else
                    continue;
            }
            buff.append(ch);
            newString = false;
        }
        String result = buff.toString();
        if (result.length() > maxWordLength) {
            return result.substring(0, maxWordLength);
        } else {
            return result;
        }
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public boolean endOfFile() {
        return reachedEndOfFile;
    }

}
