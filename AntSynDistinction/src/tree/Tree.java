package tree;

import java.util.ArrayList;

import common.exception.ValueException;

/**
 * @author Lorenzo Dell'Arciprete
 * This class represents a tree as a root node and references to its children. 
 * As such, this class can also be considered as representing a single tree node 
 * (i.e. the root node).
 * 
 */
public class Tree {
    
    protected String rootLabel;   // The label of the root node
    protected ArrayList<Tree> children = new ArrayList<Tree>();    // The ordered list of children of the root node
    protected Tree parent;
    protected int height;
    protected int leftmostPosition;
    protected int rightmostPosition;
    
    public Tree() {
        
    }
    
    public Tree(String rootLabel) {
        this.rootLabel = rootLabel;
        this.height = 0;
    }
    
    public String getConstruction() {
        // TODO: add this
        if (this.isTerminal() || this.isPreTerminal()) return "";
        else {
            StringBuffer sbResult = new StringBuffer();
            sbResult.append(this.rootLabel);
            for (Tree child : children) {
                sbResult.append(" ");
                sbResult.append(child.rootLabel);
            }
            return sbResult.toString();
        }
    }
    
//    public String getSurfaceString() {
//        String[] surfaceWords = getSurfaceWords();
//        StringBuffer sbResult = new StringBuffer();
//        sbResult.append(surfaceWords[0])
//    }
    
    /**
     * @param treeString - a tree in string parenthetic format
     * @return the Tree object representing the input tree
     * @throws Exception if the input tree string is malformed
     */
    public static Tree fromPennTree(String treeString){
        if (treeString == null || treeString.length() < 1)
            throw new ValueException("Parse error: empty (sub)tree");
        Tree tree = null;
        treeString = treeString.trim();
        if (treeString.indexOf('(') == -1) {
            //It is a terminal node
            tree = new Tree(treeString);
        }
        else if (treeString.charAt(0) == '(' && treeString.charAt(treeString.length()-1) == ')') {
            //It is a tree
            String content = treeString.substring(1, treeString.length()-1);
            int firstPar = content.indexOf('(');
            if (firstPar == -1) {
                //It is either a terminal node or a preterminal node with a single terminal node
                int firstBlank = content.indexOf(' '); 
                if (firstBlank == -1)
                    tree = new Tree(content.trim());
                else {
                    tree = new Tree(content.substring(0, firstBlank).trim());
                    tree.getChildren().add(new Tree(content.substring(firstBlank+1).trim()));
                }
            }
            else {
                //It is a tree
                tree = new Tree(content.substring(0, firstPar).trim());
                content = content.substring(firstPar).trim();
                while (content.length() > 0) {
                    if (content.charAt(0) != '(')
                        throw new ValueException("Parse error for (sub)tree 1: "+ "->" + content + "<- ->" +treeString + "<-");
                    int openPars = 1;
                    int index = 1;
                    while (openPars > 0) {
                        if (index >= content.length())
                            throw new ValueException("Parse error for (sub)tree 2: "+treeString);
                        if (content.charAt(index) == ')')
                            openPars--;
                        else if (content.charAt(index) == '(')
                            openPars++;
                        index++;
                    }
                    tree.getChildren().add(Tree.fromPennTree(content.substring(0, index).trim()));
                    content = content.substring(index).trim();
                }
            }
        }
        else
            throw new ValueException("Parse error for (sub)tree 3: "+treeString);
        // TODO: put it back?
        if (tree != null)
            tree.leafToLowerCase();
        return tree;
    }
    
    /**
     * @return the parenthetic format string representation for this tree 
     */
    public String toPennTree() {
        String treeString = "("+rootLabel;
        if (children.size() == 1) {
            treeString += " ";
            if (children.get(0).getChildren().size() == 0)
                treeString += children.get(0).getRootLabel();
            else
                treeString += children.get(0).toPennTree();
        }
        else if (children.size() > 1) {
            treeString += " ";
            for (Tree child : children)
                treeString += child.toPennTree();
        }
        treeString += ")";
        return treeString;
    }
    
    public void initializeParents() {
        initializeParent(null);
    }
    
    private void initializeParent(Tree parent) {
        this.parent = parent;
        for (Tree child : getChildren())
            child.initializeParent(this);
    }
    
    public void leafToLowerCase() {
        if (isTerminal()) {
            this.rootLabel = this.rootLabel.toLowerCase();
        } else {
            for (Tree subTree: children) {
                subTree.leafToLowerCase();
            }
        }
    }
    
    @Override
    public String toString() {
        return toPennTree();
    }
    
    public boolean equals(Tree tree) {
        if (!rootLabel.equals(tree.getRootLabel()))
            return false;
        if (children.size() != tree.getChildren().size())
            return false;
        for (int i=0; i<children.size(); i++)
            if (!children.get(i).equals(tree.getChildren().get(i)))
                return false;
        return true;
    }

    public String getRootLabel() {
        return rootLabel;
    }

    public void setRootLabel(String root) {
        this.rootLabel = root;
    }

    public ArrayList<Tree> getChildren() {
        return children;
    }

    public boolean isTerminal() {
        return children.isEmpty();
    }
    
    public boolean isPreTerminal() {
        if (isTerminal())
            return false;
        else {
            for (Tree c : children)
                if (!c.isTerminal())
                    return false;
            return true;
        }
    }

    public void setChildren(ArrayList<Tree> children) {
        this.children = children;
    }

    public Tree getParent() {
        return parent;
    }

    public void setParent(Tree parent) {
        this.parent = parent;
    }
    
    // return the list of the list of nodes of the tree
    // starting from the root
    public ArrayList<Tree> allNodes() {
        return allNodes(this);
    }
    
    private ArrayList<Tree> allNodes(Tree node) {
        ArrayList<Tree> all = new ArrayList<Tree>();
        all.add(node);
        for (Tree child : node.getChildren())
            all.addAll(allNodes(child));
        return all;
    }
    
    public void updateHeight() {
        if (this.isTerminal()) {
            this.height = 0;  
            return;
        } else {
            for (Tree child: children) {
                child.updateHeight();
            }
            int maxHeight = 0;
            for (Tree child: children) {
                int childHeight = child.getHeight();
                if (childHeight > maxHeight) maxHeight = childHeight;
            }
            this.height = maxHeight + 1;
        }
    }

    /**
     * call updateHeight before calling this
     * (it's not very well-designed, if getParent working fine, should be easier
     * to update, instead of calling manually)
     * @return
     */
    public int getHeight() {
        return height;
    }
    
    
    public void updatePosition(int leftPosition) {
        this.leftmostPosition = leftPosition;

        if (this.isTerminal()) {
            this.rightmostPosition = this.leftmostPosition;
        } else {
            for (Tree child: children) {
                child.updatePosition(leftPosition);
                leftPosition = child.getRightmostPosition() + 1;
            }
            // since leftPosition now is the rightmostPosition of the last child + 1
            this.rightmostPosition = leftPosition - 1; 
        }
    }
    
    public String[] getSurfaceWords() {
        int width = getWidth();
        String[] result = new String[width];
        putSurfaceString(0, result);
        return result;
    }
    
    public String getSurfaceString() {
        StringBuffer buffer = new StringBuffer();
        getRecurseveSurfaceString(buffer);
        return buffer.toString();
    }
    
    protected void getRecurseveSurfaceString(StringBuffer buffer) {
        if (isTerminal()) {
            buffer.append(rootLabel);
        } else {
            children.get(0).getRecurseveSurfaceString(buffer);
            for (int i = 1; i < children.size(); i++) {
                buffer.append(" ");
                children.get(i).getRecurseveSurfaceString(buffer);
            }
        }
    }
    
    protected int putSurfaceString(int pos, String[] words) {
        if (this.isTerminal()) {
            words[pos] = this.rootLabel;
            return pos;
        } else {
            for (Tree child: children) {
                pos = child.putSurfaceString(pos, words) + 1;
            }
            return pos - 1;
        }
    }
    
    public int getWidth() {
        if (this.isTerminal()) {
            return 1;
        } else {
            int width = 0;
            for (Tree child: children) {
                width += child.getWidth();
            }
            return width;
        }
    }
    
    public int getLeftmostPosition() {
        return leftmostPosition;
    }
    
    public int getRightmostPosition() {
        return rightmostPosition;
    }
}