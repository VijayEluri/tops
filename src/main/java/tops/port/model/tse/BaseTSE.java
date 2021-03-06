package tops.port.model.tse;

import java.util.ArrayList;
import java.util.List;

import tops.port.model.FixedType;
import tops.port.model.SSE;

/**
 * Tertiary structure element - sheet, barrel, sandwich, and so on.
 * 
 * @author maclean
 *
 */
public class BaseTSE {
    
    private FixedType type; // XXX refactor, but useful for now
    
    private List<SSE> elements;
    
    public BaseTSE(FixedType type) {
        this.type = type;
        this.elements = new ArrayList<SSE>();
    }

    public FixedType getType() {
        return type;
    }

    public void setType(FixedType type) {
        this.type = type;
    }

    public List<SSE> getElements() {
        return elements;
    }

    public void setElements(List<SSE> elements) {
        this.elements = elements;
    }
    
    public void add(SSE element) {
        this.elements.add(element);
    }
    
    public boolean contains(SSE element) {
        return elements.contains(element);
    }
    
    public int size() {
        return elements.size();
    }
    
    public SSE getFirst() {
        return elements.get(0);
    }
    
    public SSE get(int index) {
        return elements.get(index);
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        int index = 0;
        for (SSE sse : elements) {
            sb.append(sse.getSymbolNumber());
            if (index < elements.size() - 1) {
                sb.append("-");
            }
            index++;
        }
        return sb.toString();
    }

}
