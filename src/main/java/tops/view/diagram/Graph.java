package tops.view.diagram;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Graph {

    private List<Vertex> sses;
    private List<Edge> edges;
    private boolean isLaidOut;

    public Graph() {
        this.sses = new ArrayList<Vertex>();
        this.edges = new ArrayList<Edge>();
        this.isLaidOut = false;
    }
    
    public void addVertex(Vertex vertex) {
        this.sses.add(vertex);
    }
    
    public void addEdge(Edge edge) {
        this.edges.add(edge);
    }
    
    public boolean needsLayout() {
        return !this.isLaidOut;
    }
    
    public void layout(double axis, int w) {
        int numberOfVertices = this.sses.size();
        int centerSeparation = w / numberOfVertices;
        int boxWidth = centerSeparation / 2;

        for (int i = 0; i < numberOfVertices; ++i) {
            int xpos = (i * centerSeparation) + (boxWidth / 2);
            Rectangle2D box = 
                new Rectangle2D.Double((double) xpos, axis, boxWidth, centerSeparation);
            Vertex vertex = this.getVertex(i);
            vertex.setBounds(box);
        }
        this.isLaidOut = true;
    }

    public int numberOfVertices() {
        return this.sses.size();
    }
    
    public Vertex getVertex(int i) {
        return this.sses.get(i);
    }
    
    public List<Vertex> getVertices() {
        return this.sses;
    }
    
    public List<Edge> getEdges() {
        return this.edges;
    }

    public int numberOfEdges() {
        return this.edges.size();
    }
    
    public Edge getEdge(int i) {
        return this.edges.get(i);
    }

    public boolean isEmpty() {
        return this.sses.isEmpty();
    }
    
    public String toPostscript(int w, int h, double axis) {
        StringBuilder ps = new StringBuilder();
        ps.append("%!PS-Adobe-3.0 EPSF-3.0\n");
        //ps.append("%!PS-Adobe-3.0\n");
        ps.append("%%Creator : tops.view.tops2D.diagram.DiagramDrawer\n");
        int x1 = 0;
        int y1 = 0;
        int x2 = w + x1;
        int y2 = h + y1;
        ps.append("%%BoundingBox: " + x1 + " " + y1 + " " + x2 + " " + y2 + "\n");
        ps.append("%%EndComments\n");
        ps.append("%%EndProlog\n");

        ps.append("1 -1 scale\n");    // flip the Y-axis
        ps.append("0 -" + h + " translate\n");    // translate back up

        int numberOfObjects = this.sses.size() + this.edges.size();
        Shape[] shapes = new Shape[numberOfObjects];
        Color[] colors = new Color[numberOfObjects];
        int index = 0;
        Iterator<Vertex> sseItr = this.sses.iterator();

        while (sseItr.hasNext()) {
            Vertex sse = (Vertex) (sseItr.next());
            shapes[index] = sse.getShape();
            colors[index] = sse.getColor();
            index++;
        }

        Iterator<Edge> j = this.edges.iterator();
        while (j.hasNext()) {
            Edge edge = (Edge) (j.next());
            shapes[index] = edge.getShape(axis);
            colors[index] = edge.getColor();
            index++;
        }
        
        for (int i = 0; i < shapes.length; i++) {
            PathIterator pathIterator = shapes[i].getPathIterator(new AffineTransform());
            ps.append("newpath\n");
            while (!pathIterator.isDone()) {
                float[] coords = new float[6]; 
                int segmentType = pathIterator.currentSegment(coords);
                switch (segmentType) {
                    case PathIterator.SEG_MOVETO:
                        ps.append(coords[0]).append(" ");
                        ps.append(coords[1]);
                        ps.append(" moveto\n");
                        break;
                    case PathIterator.SEG_LINETO:
                        ps.append(coords[0]).append(" ");
                        ps.append(coords[1]);
                        ps.append(" lineto\n");
                        break;
                    case PathIterator.SEG_QUADTO:
                        ps.append("quadto\n");
                        break;
                    case PathIterator.SEG_CUBICTO:
                        ps.append(coords[0]).append(" ");
                        ps.append(coords[1]).append(" ");
                        ps.append(coords[2]).append(" ");
                        ps.append(coords[3]).append(" ");
                        ps.append(coords[4]).append(" ");
                        ps.append(coords[5]);
                        ps.append(" curveto\n");
                        break;
                    case PathIterator.SEG_CLOSE:
                        ps.append("closepath\n");
                        break;
                    default: break;
                }
                pathIterator.next();
            }
            //ps.append("1 setlinewidth\n");
            float[] rgb = colors[i].getRGBComponents(null);
            ps.append(rgb[0]).append(" ");
            ps.append(rgb[1]).append(" ");
            ps.append(rgb[2]).append(" setrgbcolor\n");
            ps.append("stroke\n");
        }
        ps.append("%EndDocument\n");
        ps.append("%EOF\n");

        return ps.toString();
    }
}
