package tree;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import vocab.Vocab;

import common.WordForm;

public class CcgTree extends Tree{
    public static final int AN = 1;
    public static final int DN = 2;
    public static final int CN = 3;
    public static final int NN = 4;
//    public static final int NP = 5;
//    public static final int DEFAULT = 0;
    public static final int LEFT_HEAD = 0;
    public static final int RIGHT_HEAD = 1;
    int headInfo = LEFT_HEAD;
    int leftPos = -1;
    int rightPos = 0;
    String cat = "";
    String modifiedCat = "";
    String pos = "";
    String lemma = "";
    int wordVocabIndex = -1;
    int type;

    public CcgTree(String rootLabel) {
        // TODO Auto-generated constructor stub
        super(rootLabel);
    }

    public static CcgTree parseTreeFromCcgXml(String xmlString) throws SAXException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlString.getBytes("iso-8859-1")));
            Node root = doc.getFirstChild();
            CcgTree result = parseTreeFromNode(root);   
            result.setParent(null);
            return result;
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static CcgTree createEmptyTree() {
        CcgTree tree = new CcgTree("None");
        tree.cat = "None";
        tree.lemma = "None";
        tree.pos = "None";
        tree.modifiedCat = "None";
        return tree;
    }
    
    public static int getHeadFromRule(String rule) {
        if (rule.equals("ba")) {
            return RIGHT_HEAD;
        } else if (rule.equals("fa")) {
            return LEFT_HEAD;
        } else if (rule.equals("lex")) {
            return LEFT_HEAD;
        } else if (rule.equals("rp")) {
            return LEFT_HEAD;
        } else {
            //System.out.println("Strange rule: " + rule);
            return LEFT_HEAD;
        }
    }
    
    
    public static String normalizeTag(String tag) {
        tag = tag.replaceAll("\\(", "<");
        tag = tag.replaceAll("\\)", ">");
        tag = tag.replaceAll("\\[.*?\\]", "");
        tag = tag.replaceAll(":", ".");
        tag = tag.replaceAll("_", "-");
        return tag;
    }
    
    public static String normalizeWord(String word) {
        if ("(".equals(word)) {
            word = "LRB";
        } else if (")".equals(word)) {
            word = "RRB";
        }
        word = word.replaceAll(":", ".");
        word = word.replaceAll("_", "-");
        word = word.replaceAll(" ", "-");
        return word;
    }
    
    protected static CcgTree createTreeFromXmlLexicalNode(Node node) {
        NamedNodeMap attributes = node.getAttributes();
        String superTag = attributes.getNamedItem("cat").getNodeValue();
        String modifiedTag = normalizeTag(superTag);
        String rootLabel = modifiedTag;
        CcgTree result = new CcgTree(rootLabel);
        ArrayList<Tree> children = new ArrayList<Tree>();
        
        String word = attributes.getNamedItem("word").getNodeValue();
        word = normalizeWord(word);
        
        CcgTree child = new CcgTree(word);
        
        String lemma = attributes.getNamedItem("lemma").getNodeValue().toLowerCase();
        lemma = normalizeWord(lemma);
        child.lemma = lemma;
        
        child.cat = superTag;
        child.modifiedCat = modifiedTag;
        
        String pos = attributes.getNamedItem("pos").getNodeValue();
        pos = pos.replaceAll(":", ".");
        child.pos = pos;
        children.add(child);
        result.setChildren(children);
        return result;
    }
    
    protected static CcgTree createTreeFromXmlRuleNode(Node node) {
        String rootLabel = node.getAttributes().getNamedItem("cat").getNodeValue();
        rootLabel = normalizeTag(rootLabel);
        CcgTree result = new CcgTree(rootLabel);
        result.headInfo = getHeadFromRule(node.getAttributes().getNamedItem("type").getNodeValue());
        ArrayList<Tree> children = new ArrayList<Tree>();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (!childNode.getNodeName().equals("#text")) {
                children.add(parseTreeFromNode(childNode));
            }
        }
        result.setChildren(children);
        return result;
    }
    
    public static CcgTree parseTreeFromNode(Node node) {
        if (node.getNodeName().equals("ccg")) {
            if (node.getFirstChild() != null) {
                NodeList childNodes = node.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node childNode = childNodes.item(i);
                    if (!childNode.getNodeName().equals("#text")) {
                        return parseTreeFromNode(childNode);
                    }
                }
            } 
            return createEmptyTree();
            
        } else if (node.getNodeName().equals("rule")) {
            return createTreeFromXmlRuleNode(node);
        } else if (node.getNodeName().equals("lf")) {
            return createTreeFromXmlLexicalNode(node);
        } else {
            System.out.println(node.getNodeName());
            System.out.println(node.getParentNode().getNodeName());
            return null;
        }
        
    }
    
    /**
     * Print all information to a string
     * - non-terminal node contains: head position (option for supertag?)
     * - pre-terminal node contains: pos
     * - terminal node contains: word (lemma or not) 
     */
    public String toSimplePennTree(int wordForm) {
        if (this.children.size() == 1) {
            String treeString = "";
            
            if (children.get(0).children.size() != 0) {
                treeString += ((CcgTree) children.get(0)).toSimplePennTree(wordForm);
            } else {
                treeString += "(" + this.headInfo + " ";
                CcgTree terminalChild = (CcgTree) children.get(0);
                switch (wordForm) {
                case WordForm.WORD:
                    treeString += terminalChild.getRootLabel();
                    break;
                case WordForm.LEMMA:
                    treeString += terminalChild.lemma;
                    break;
                case WordForm.WORD_POS:
                    treeString += terminalChild.getRootLabel() 
                        + "-" + terminalChild.pos.toLowerCase().charAt(0);
                    break;
                case WordForm.LEMMA_POS:
                    treeString += terminalChild.lemma  
                        + "-" + terminalChild.pos.toLowerCase().charAt(0);
                    break;
                }
                treeString += ")";
            }
            return treeString;
        } else if (this.children.size() > 1) {
            String treeString = "("+ headInfo;
            treeString += " ";
            for (Tree child : children)
                treeString += ((CcgTree) child).toSimplePennTree(wordForm);
            treeString += ")";
            return treeString;
        } else {
            if (this.isTerminal()) {
                String treeString = "("+ this.getRootLabel();
                treeString += " ";
                treeString += getRootLabel();
                treeString += ")";
                return treeString;
            } else 
                return null;
        }
    }
    
    public static CcgTree fromSimplePennTree(String treeString) {
        try {
            return fromTree(fromPennTree(treeString));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
                
    }
    
    public static CcgTree fromTree(Tree tree) {
        if (tree.children.size() == 0) {
            return new CcgTree(tree.rootLabel);
        }
        
        CcgTree ccgTree = new CcgTree(tree.rootLabel);
        ccgTree.headInfo = Integer.parseInt(ccgTree.rootLabel);
        for (Tree child: tree.children) {
            ccgTree.children.add(fromTree(child));
        }
        return ccgTree;
    }
    
    public List<CcgTree> getAllSubTreeAtHeight(int height) {
        List<CcgTree> list = new ArrayList<CcgTree>(10);
        getHeightAndAdd(list, height);
        return list;
    }
    
    private int getHeightAndAdd(List<CcgTree> list, int targetHeight) {
        int height = 0;
        if (children.size() == 0) {
            height = 1;
            if (this.pos.length() == 1) height = 10;
        } else {
            if (children.size() == 1) {
                CcgTree ccgChild = (CcgTree) children.get(0);
                height = ccgChild.getHeightAndAdd(list, targetHeight);
            } else {
                for (Tree child: children) {
                    CcgTree ccgChild = (CcgTree) child;
                    height = Math.max(height, ccgChild.getHeightAndAdd(list, targetHeight));
                }
                height = height + 1;
            }
        }
        if (height == targetHeight) {
            list.add(this);
        }
        return height;
    }
    
    public int updateSubTreePosition(int leftPos) {
        this.leftPos = leftPos;
        if (children.size() == 0) {
            rightPos = leftPos;
        } else {
            int curChildLeft = leftPos;
            for (Tree child: this.children) {
                CcgTree ccgChild = (CcgTree) child;
                rightPos = ccgChild.updateSubTreePosition(curChildLeft);
                curChildLeft = rightPos + 1;
            }
        }
        return rightPos;
    }
    
    public String getSurfaceString(int wordForm) {
        if (this.isPreTerminal()) {
            CcgTree terminalChild = (CcgTree) children.get(0);
            String wordString;
            switch (wordForm) {
            case WordForm.WORD:
                wordString = terminalChild.getRootLabel();
                break;
            case WordForm.LEMMA:
                wordString = terminalChild.lemma;
                break;
            case WordForm.WORD_POS:
                wordString = terminalChild.getRootLabel() 
                    + "-" + terminalChild.pos.toLowerCase().charAt(0);
                break;
            case WordForm.LEMMA_POS:
                wordString = terminalChild.lemma  
                    + "-" + terminalChild.pos.toLowerCase().charAt(0);
                break;
            default:
                wordString = terminalChild.getRootLabel();
            }
            return wordString;
        } else {
            String result = ((CcgTree) children.get(0)).getSurfaceString(wordForm);
            for (int i = 1; i < children.size(); i++) {
                result += " " + ((CcgTree) children.get(i)).getSurfaceString(wordForm);
            }
            return result;
        }
    }
    
//    public String getSurfaceString() {
//        if (children.size() == 0) {
//            return this.rootLabel;
//        } else {
//            String result = ((CcgTree) children.get(0)).getSurfaceString();
//            for (int i = 1; i < children.size(); i++) {
//                result += " " + ((CcgTree) children.get(i)).getSurfaceString();
//            }
//            return result;
//        }
//    }
    
    public String getFakeString() {
        if (children.size() == 0) {
            return "" + this.leftPos;
        } else {
            String result = ((CcgTree) children.get(0)).getFakeString();
            for (int i = 1; i < children.size(); i++) {
                result += " " + ((CcgTree) children.get(i)).getFakeString();
            }
            return result;
        }
    }
    
    public void updatePhraseType() {
        if (isTerminal()) return;
        if (isPreTerminal()) return;
        if (children.size() == 2) {
            if (children.get(0).isPreTerminal() && children.get(1).isPreTerminal()) {
                String firstLabel = children.get(0).getChildren().get(0).getRootLabel();
                String secondLabel = children.get(1).getChildren().get(0).getRootLabel();
                char firstPos = firstLabel.charAt(firstLabel.length() - 1);
                char secondPos = secondLabel.charAt(secondLabel.length() - 1);
                
                if (firstPos == 'j' && secondPos == 'n') type = AN;
                else if (firstPos == 'd' && secondPos == 'n') type = DN;
                else if (firstPos == 'c' && secondPos == 'n') type = CN;
                else if (firstPos == 'n' && secondPos == 'n') type = NN;
                
                return;
            }
        }
        for (Tree child: children) {
            ((CcgTree) child).updatePhraseType();
        }
    }
    
    public void updateWordIndices(Vocab vocab) {
        if (children.size() == 0) {
            this.wordVocabIndex = vocab.getWordIndex(this.rootLabel);
        } else {
            for (Tree child: children) {
                ((CcgTree) child).updateWordIndices(vocab);
            }
        }
    }
    
    public String getLemma() {
        return lemma;
    }
    
    public int getLeftPos() {
        return leftPos;
    }
    
    public int getRightPos() {
        return rightPos;
    }
    
    public int getType() {
        return type;
    }
    
    public int getWordIndex() {
        return wordVocabIndex;
    }
}
