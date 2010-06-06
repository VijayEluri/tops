package tops.view.tops3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

/*
 * This is just a fancy name for a Director class that controls the 
 * construction of <i>Structure</i>s by <i>Framework</i>s.
 */

public class Architect {

    private Framework framework;

    public Architect(Framework framework) {
        this.framework = framework;
    }

    public void buildStrandSheet(String vertices, String hbonds, String chirals) {
        int numVertices = vertices.length() - 2;
        ArrayList newOrder = this.sheetReOrder(hbonds);
        System.out.println(newOrder);

        // String type = null;
        // String orientation = null;

        for (int i = 0; i < newOrder.size(); i++) {
            int pos = Integer.parseInt((String) newOrder.get(i)); // this is
                                                                    // the
                                                                    // position
                                                                    // in the
                                                                    // chain
            char c = vertices.charAt(pos); // since vertices is ordered by N-C,
                                            // this works
            this.framework.addSSE(pos, c);
        }
        HashMap chiralMap = this.makeChiralMap(chirals);
        int f = 0; // first SSE
        int s = 1; // second SSE
        int chirality; // er...chirality
        while ((f < numVertices - 1) && (s < numVertices)) {
            Object[] tmp = (Object[]) chiralMap.get(new Integer(f + 1)); // plus
                                                                            // one!!
                                                                            // (poor
                                                                            // numeration
                                                                            // again)
            if (tmp == null)
                chirality = Framework.CHIRAL_NONE;
            else {
                char c = ((Character) tmp[1]).charValue();
                System.out.println("debug : " + c);
                chirality = (c == 'R') ? Framework.CHIRAL_RIGHT
                        : Framework.CHIRAL_LEFT;
            }
            this.framework.connectSSEs(f, s, chirality);
            System.out.println("connecting : " + f + " and " + s
                    + " chirality " + chirality);
            f++;
            s++;
        }
    }

    private HashMap makeChiralMap(String chirals) {
        HashMap chiralMap = new HashMap();
        StringTokenizer tokenizer = new StringTokenizer(chirals, ".");
        while (tokenizer.hasMoreTokens()) {
            String t = tokenizer.nextToken();
            int colon = t.indexOf(':');
            Integer left = new Integer(t.substring(0, colon));
            Integer right = new Integer(t.substring(colon + 1, t.length() - 1));
            Character type = new Character(t.charAt(t.length() - 1));
            Object[] tmp = new Object[2]; // gah!
            tmp[0] = right;
            tmp[1] = type;
            chiralMap.put(left, tmp);
        }
        return chiralMap;
    }

    private ArrayList sheetReOrder(String hbonds) {
        ArrayList newOrder = new ArrayList();
        HashMap edgeMap = new HashMap();
        StringTokenizer tokenizer = new StringTokenizer(hbonds, "."); // split
                                                                        // on
                                                                        // dots
        // first, map the edge view to a vertex view
        while (tokenizer.hasMoreTokens()) {
            String t = tokenizer.nextToken();
            int colon = t.indexOf(':');
            String left = t.substring(0, colon);
            String right = t.substring(colon + 1, t.length() - 1);
            this.mapVertex(left, right, edgeMap);
            this.mapVertex(right, left, edgeMap);
        }
        // now, go through the vertices to find the endpoints (edgestrands)
        Set keys = edgeMap.keySet();
        Iterator i = keys.iterator();
        ArrayList edgeStrands = new ArrayList();
        while (i.hasNext()) {
            String vertex = (String) i.next();
            ArrayList connections = (ArrayList) edgeMap.get(vertex);
            if (connections.size() < 2)
                edgeStrands.add(vertex);
        }

        // gah! now find the smallest edgestrand
        int currentSmallest = keys.size(); // oddly, set the left end to be the
                                            // highest possible value!
        for (int e = 0; e < edgeStrands.size(); e++) {
            String v = (String) edgeStrands.get(e);
            int vnum = Integer.parseInt(v);
            if (vnum < currentSmallest)
                currentSmallest = vnum;
        }
        System.out.println("leftEnd : " + currentSmallest);
        // now follow the path of the sheet
        int k = 0;
        String currentVertex = String.valueOf(currentSmallest);
        String lastVertex = currentVertex;
        String nextVertex = new String();
        while (k < keys.size()) {
            ArrayList conn = (ArrayList) edgeMap.get(currentVertex);
            for (int c = 0; c < conn.size(); c++) {
                String v = (String) conn.get(c);
                if (!v.equals(lastVertex))
                    nextVertex = v;
            }
            newOrder.add(currentVertex);
            lastVertex = currentVertex;
            currentVertex = nextVertex;
            k++;
        }
        return newOrder;
    }

    private void mapVertex(String one, String two, HashMap edgeMap) {
        // System.out.println("mapping : " + one + " and " + two);
        ArrayList o = (ArrayList) edgeMap.get(one);
        if (o == null)
            o = new ArrayList();
        o.add(two);
        edgeMap.put(one, o);
        // System.out.println("edgemap now " + edgeMap);
    }
}
