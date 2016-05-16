package io.word;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class CombinedWordInputStream implements WordInputStream {
    Iterator<WordInputStream> streamIterator;
    WordInputStream           currentStream;
    int                       streamCount = 0;

    public CombinedWordInputStream(List<WordInputStream> inputStream) {
        streamIterator = inputStream.iterator();
        if (streamIterator.hasNext()) {
            currentStream = streamIterator.next();
            streamCount++;
            System.out.println("read " + streamCount + "th stream");
        } else {
            currentStream = null;
        }
    }

    @Override
    public String readWord() throws IOException {
        // TODO Auto-generated method stub
        if (currentStream == null) {
            return "";
        }
        while (true) {
            String word = currentStream.readWord();
            if (!word.equals("")) {
                return word;
            }
            currentStream.close();
            boolean hasNextStream = false;
            while (streamIterator.hasNext()) {
                currentStream = streamIterator.next();
                streamCount++;
                System.out.println("read " + streamCount + "th stream");
                if (currentStream == null) {
                    System.out.println("" + streamCount + "th stream is null");
                    continue;
                } else {
                    System.out.println("" + streamCount
                            + "th stream is not null");
                    hasNextStream = true;
                    break;
                }
            }
            if (!hasNextStream) {
                currentStream = null;
                return "";
            }
        }
    }

    @Override
    public boolean endOfFile() {
        // TODO Auto-generated method stub
        if (currentStream == null)
            return true;
        else if (currentStream.endOfFile()) {
            return streamIterator.hasNext();
        } else
            return false;
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub
        if (currentStream == null)
            return;
        else {
            currentStream.close();
            while (streamIterator.hasNext()) {
                currentStream = streamIterator.next();
                if (currentStream != null) {
                    currentStream.close();
                }
            }
        }
    }

}
