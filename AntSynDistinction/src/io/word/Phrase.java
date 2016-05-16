package io.word;

import common.WordForm;

import tree.CcgTree;

public class Phrase {
    public int   phraseType;
    public int   startPosition;
    public int   endPosition;
    public CcgTree tree;

    public Phrase(int phraseType, int startPosition, int endPosition,
            CcgTree tree) {
        this.phraseType = phraseType;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.tree = tree;
    }

    public String toString() {
        StringBuffer sbResult = new StringBuffer();
        sbResult.append("phrase type:" + phraseType + "\n");
        sbResult.append("start:" + startPosition + "\n");
        sbResult.append("end:" + endPosition + "\n");
        sbResult.append("surface: \'" + tree.getSurfaceString(WordForm.WORD) + "\'\n");
        return sbResult.toString();
    }
}
