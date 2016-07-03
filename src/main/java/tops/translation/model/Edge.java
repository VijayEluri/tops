package tops.translation.model;

public class Edge implements Comparable<Edge> {

    private BackboneSegment first;

    private BackboneSegment second;

    private char type;

    public Edge(BackboneSegment first, BackboneSegment second, char type) {
        this.first = first;
        this.second = second;
        this.type = type;
    }

    public int compareTo(Edge other) {
        int firstCompare = this.first.compareTo(other.first);
        if (firstCompare == 0) {
            return this.second.compareTo(other.second);
        } else {
            return firstCompare;
        }
    }

    @Override
    public boolean equals(Object other) {
        return this.first == ((Edge) other).first
                && this.second == ((Edge) other).second;
    }

    public void mergeWith(Edge chiral) {
        if (chiral.type == 'R') {
            this.type = 'Z';
        } else {
            this.type = 'X';
        }
    }

    public char getType() {
        return this.type;
    }

    @Override
    public String toString() {
        int i = this.first.getNumber() + 1;
        int j = this.second.getNumber() + 1;
        return i + ":" + j + this.type;
    }
}
