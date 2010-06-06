package tops.engine;

import java.util.ArrayList;

public class Edge implements Comparable {

    public Vertex left, right; // BAD IDEA!

    public char type;

    public int p, S1, S2, E1, E2, mp;

    private ArrayList matchList;

    public boolean moved;

    private int rangeMinimum;

    private int rangeMaximum;

    public Edge(Vertex l, Vertex r, char t) {
        this.left = l;
        this.right = r;
        this.type = t;
        this.p = 0;
        this.S1 = this.S2 = this.E1 = this.E2 = 0;
        this.matchList = new ArrayList();
        this.mp = 0;
        this.moved = false;
        this.rangeMinimum = -1; // -1 indicates 'infinite' bounds.
        this.rangeMaximum = -1;
    }
    
    public int getRangeMinimum() {
        return this.rangeMinimum;
    }

    public int getRangeMaximum() {
        return this.rangeMaximum;
    }

    public void setRangeMinimum(int rangeMinimum) {
        this.rangeMinimum = rangeMinimum;
    }

    public void setRangeMaximum(int rangeMaximum) {
        this.rangeMaximum = rangeMaximum;
    }

    public boolean rangeMatches(Edge other) {
        return this.rangeMatches(other.getLeft(), other.getRight());
    }

    public boolean rangeMatches(int left, int right) {
        int intermediateVertices = (right - left) - 1;
        boolean minimumMatches = ((this.rangeMinimum == -1) || (intermediateVertices >= this.rangeMinimum));
        boolean maximumMatches = ((this.rangeMaximum == -1) || (intermediateVertices <= this.rangeMaximum));
        return minimumMatches && maximumMatches;
    }

    public char getLType() {
        return this.left.getType();
    }

    public char getRType() {
        return this.right.getType();
    }

    public int getLeft() {
        return this.left.getPos();
    }

    public int getLeftMatch() {
        return this.left.getMatch();
    }

    public Vertex getLeftVertex() {
        return this.left;
    }

    public int getRight() {
        return this.right.getPos();
    }

    public Vertex getRightVertex() {
        return this.right;
    }

    public int getRightMatch() {
        return this.right.getMatch();
    }

    public boolean atPosition(int i, int j) {
        return ((this.left.getPos() == i) && (this.right.getPos() == j));
    }

    public boolean isHBond() {
        return (this.type == 'A' || this.type == 'P' || this.type == 'Z' || this.type == 'X')
                && (this.left.getType() == 'E' || this.left.getType() == 'e');
    }

    public boolean isHPP() {
        return (this.type == 'A' || this.type == 'P' || this.type == 'Z' || this.type == 'X')
                && (this.left.getType() == 'H' || this.left.getType() == 'h');
    }

    public boolean isChiral() {
        return (this.type == 'R' || this.type == 'L' || this.type == 'Z' || this.type == 'X');
    }

    public char getType() {
        return this.type;
    }

    public void setType(char c) {
        this.type = c;
    }

    public boolean matches(Edge te) {

        // Labels match?
        if (this.type != te.type) {
            if ((this.type == 'P') && ((te.type != 'Z') && (te.type != 'X'))) {
                return false;
            } else if ((this.type == 'R') && (te.type != 'Z')) {
                return false;
            } else if ((this.type == 'L') && (te.type != 'X')) {
                return false;
            } else if ((this.type == 'A') || (this.type == 'Z') || (this.type == 'X')) {
                return false;
            }
            // OTHERWISE, /accept/ !
        }
//        System.out.println("this " + this.indexString() + " other " + te.indexString());
        
        return ((this.S1 <= te.S1) 
        		&& (this.S2 <= te.S2) 
        		&& (this.E1 <= te.E1) 
        		&& (this.E2 <= te.E2)) // Edge
                && (this.left.vertexMatches(te.left)) // indexes match?
                && (this.right.vertexMatches(te.right)); // Vertices match?
    }
    
    // DEBUG METHOD
    public String indexString() {
    	return this.S1 + "," + this.S2 + "," + this.E1 + "," + this.E2;
    }

    public boolean alreadyMatched(Edge t) {
        return this.left.matchedTo(t.getLeftVertex()) && this.right.matchedTo(t.getRightVertex());
    }

    public void addMatch(Edge eToAdd) {
        this.matchList.add(eToAdd);
    }

    public void setEndMatches(Edge curr) {
        this.left.setMatch(curr.left);
        this.right.setMatch(curr.right);
    }

    public Edge getCurrentMatch() {
    	return (Edge) this.matchList.get(this.mp);
    }

    public boolean hasMoreMatches() {
    	return this.mp < this.matchList.size();
    }

    public void moveMatchPtr() {
        this.mp++;
    }

    public void resetMatchPtr(int p) {
        this.mp += p;
    }
    
    public void setMatchPtr(int p) {
    	this.mp = p;
    }
    
    public int getMatchPtr() {
    	return this.mp;
    }

    public boolean greaterThan(Edge dat) {
        int disl = this.left.getPos();
        int datl = dat.left.getPos();
        return (disl > datl)
                || ((disl == datl) && (this.right.getPos() > dat.right.getPos()));
    }

    public void reset() {
        this.matchList = new ArrayList();
        this.mp = 0;
    }

    public boolean connectedTo(Edge dat) {
        return (this.left.getPos() == dat.left.getPos())
                || (this.right.getPos() == dat.right.getPos());
    }

    public int compareTo(Object other) {
        if (this.equals(other)) {
            return 0;
        } else {
            if ((other instanceof Edge) && (this.greaterThan((Edge) other))) {
                return 1;
            } else
                return -1;
        }
    }

    public boolean equals(Edge that) {
    	 return (this.left.getPos() == that.left.getPos())
         	 && (this.right.getPos() == that.right.getPos());
    }

    @Override
    public String toString() {
        StringBuffer tmp = new StringBuffer();
        tmp.append(this.left.getPos()).append(':').append(this.right.getPos()).append(
                this.type);
        return tmp.toString();
    }

}

