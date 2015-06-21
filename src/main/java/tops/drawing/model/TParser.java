package tops.drawing.model;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class TParser {

    private String current;

    private String[] toDo;

    private int index;

    private Pattern edgeP = Pattern.compile("(\\d+):(\\d+)(\\w)"); //the regex

    public TParser() {
        this.toDo = null;
    }

    public TParser(String s) {
        this.current = s;
        this.toDo = null;
    }

    public TParser(String[] sarr) {
        this.toDo = sarr;
        this.current = this.toDo[0];
    }

    public int next() {
        if (this.index++ < this.toDo.length)
            this.current = this.toDo[this.index];
        else
            return -1;
        return 1;
    }

    public void load(String s) {
        this.current = s;
    }

    public String getName() {
        int sp = this.current.indexOf(' ');
        return this.current.substring(0, sp);
    }

    public String getVertexString() {
        int sp = this.current.indexOf(' ');
        int spsp = this.current.indexOf(' ', sp + 1);
        return this.current.substring(sp + 1, spsp);
    }

    public char[] getVertices() {
        return this.getVertexString().toCharArray();
    }

    public String getEdgeString() {
        int sp = this.current.indexOf(' ');
        int spsp = this.current.indexOf(' ', sp + 1);
        int spspsp = this.current.indexOf(' ', spsp + 1);
        if (spspsp == -1)
            spspsp = this.current.length();
        return this.current.substring(spsp + 1, spspsp);
    }
    
    // doesn't work in java1.3 
    public String[] getEdges() {
        String tail = this.getEdgeString(); 
        Matcher m = edgeP.matcher(tail);
        ArrayList<String> bits = new ArrayList<String>(); 
        while (m.find()) { 
            for (int i = 1; i <= m.groupCount(); i++) { 
                bits.add(m.group(i)); 
            } 
        } 
        return (String[]) bits.toArray(new String[0]); 
     }

    // ALTERNATIVE 1.3 method
      /*
    public String[] getEdges() {
        String tail = this.getEdgeString();
        ArrayList bytes = new ArrayList();
        char[] bits = tail.toCharArray();
        StringBuffer numstr = new StringBuffer();
        for (int i = 0; i < bits.length; i++) {
            char c = bits[i];
            if (Character.isDigit(c)) {
                numstr.append(c);
            } else if (Character.isLetter(c)) {
                bytes.add(numstr.toString());
                bytes.add(String.valueOf(c));
                numstr = new StringBuffer();
            } else {
                bytes.add(numstr.toString());
                numstr = new StringBuffer();
            }
        }
        return (String[]) bytes.toArray(new String[0]);
    }
    */

    public String getClassification() {
        int sp = this.current.indexOf(' ');
        int spsp = this.current.indexOf(' ', sp + 1);
        int spspsp = this.current.indexOf(' ', spsp + 1);
        if (spspsp == -1)
            return "";
        return this.current.substring(spspsp + 1);
    }

    public String[] getConnectedComponents() {
        return null;
    }

    public static void main(String[] args) {
        TParser t = new TParser(args[0]);
        System.out.println(t.getName());
        System.out.println(t.getVertices());
        String[] sta = t.getEdges();
        for (int k = 0; k < sta.length; k++) {
            System.out.println("[" + sta[k] + "]");
        }
    }
}