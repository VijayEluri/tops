package tops.drawing.actions;

import java.util.ArrayList;

import tops.drawing.Diagram;

public class FlipSymbols implements Action {
    private ArrayList symbolNumbers;
    private Diagram diagram;
    
    public FlipSymbols(ArrayList symbolNumbers, Diagram diagram) {
        this.symbolNumbers = symbolNumbers;
        this.diagram = diagram;
    }
    
    public void doIt() {
        this.diagram.flipSSEList(symbolNumbers);
    }
    
    public void undoIt() {
        this.diagram.flipSSEList(symbolNumbers);
    }
    
    public String description() {
        return "Flip SSE";
    }
}
