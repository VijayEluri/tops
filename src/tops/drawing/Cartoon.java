package tops.drawing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import tops.drawing.symbols.Bond;
import tops.drawing.symbols.Box;
import tops.drawing.symbols.CartoonConnector;
import tops.drawing.symbols.Circle;
import tops.drawing.symbols.ConnectionSymbol;
import tops.drawing.symbols.DashedLine;
import tops.drawing.symbols.EquilateralTriangle;
import tops.drawing.symbols.SSESymbol;



public class Cartoon implements Cloneable {
    private ArrayList sseSymbols;
    private ArrayList connections;
    private ArrayList rArcs;
    private ArrayList lArcs;
    private ArrayList aBonds;
    private ArrayList pBonds;
    
    private CartoonConnector selected_connector;
    
    private int MAX_SSE_SIZE;
    private int CURRENT_SSE_SIZE;

    public Cartoon() {
        this.MAX_SSE_SIZE = 20;
        this.CURRENT_SSE_SIZE = this.MAX_SSE_SIZE;
        
        this.sseSymbols = new ArrayList();
        this.connections = new ArrayList();
        this.rArcs = new ArrayList();
        this.lArcs = new ArrayList();
        this.aBonds = new ArrayList();
        this.pBonds = new ArrayList();
        
        this.createTermini();
        this.selected_connector = null;
        
    }
    
    public Cartoon(ArrayList sseSymbols, ArrayList connections, ArrayList rArcs, ArrayList lArcs, ArrayList aBonds, ArrayList pBonds) {
        this();
        this.sseSymbols = sseSymbols;
        this.connections = connections;
        this.rArcs = rArcs;
        this.lArcs = lArcs;
        this.aBonds = aBonds;
        this.pBonds = pBonds;
    }
    
    /**
     * @param dimension the width or height of the canvas 
     * @return the size of the sses in this cartoon
     */
    public int getSSESize(int dimension) {
        int calculatedSize = (int) dimension / (this.sseSymbols.size() + 1);
        if (calculatedSize > this.MAX_SSE_SIZE) {
            return this.MAX_SSE_SIZE;
        }
        return calculatedSize;
    }

    public Object clone() {
        try {
            Cartoon c = (Cartoon) super.clone();
            
            c.sseSymbols = new ArrayList();
            for (int i = 0; i < this.sseSymbols.size(); i++) {
                c.sseSymbols.add(((SSESymbol)this.sseSymbols.get(i)).clone());
            }
            
            c.connections = new ArrayList();
            for (int i = 0; i < this.connections.size(); i++) {
                CartoonConnector connector = (CartoonConnector)this.connections.get(i);
                CartoonConnector shallowClone = (CartoonConnector) connector.clone(); 
                this.cloneBond(connector, shallowClone, c.sseSymbols);
                c.connections.add(shallowClone);
            }
            
            c.lArcs = this.cloneBonds(c, this.lArcs);
            c.rArcs = this.cloneBonds(c, this.rArcs);
            c.aBonds = this.cloneBonds(c, this.aBonds);
            c.pBonds = this.cloneBonds(c, this.pBonds);
            
            c.selected_connector = null;
            
            return c;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }
    
    private ArrayList cloneBonds(Cartoon clone, ArrayList bonds) {
        ArrayList bondClones = new ArrayList();
        for (int i = 0; i < bonds.size(); i++) {
            Bond bond = (Bond) bonds.get(i);
            Bond shallowClone = (Bond) bond.clone();
            this.cloneBond(bond, shallowClone, clone.sseSymbols);
            bondClones.add(shallowClone);
        }
        return bondClones;
    }
    
    private void cloneBond(ConnectionSymbol originalConnection, ConnectionSymbol clonedConnection, 
            ArrayList clonedSSESymbols) {
        SSESymbol start = originalConnection.getStartSSESymbol();
        SSESymbol end = originalConnection.getEndSSESymbol();
        int startIndex = this.sseSymbols.indexOf(start);
        int endIndex = this.sseSymbols.indexOf(end);
        clonedConnection.setStartSSESymbol((SSESymbol) clonedSSESymbols.get(startIndex));
        clonedConnection.setEndSSESymbol((SSESymbol) clonedSSESymbols.get(endIndex));
    }
    
    public void relayout() {
        for (int i = 0; i < this.sseSymbols.size(); i++ ) {
            SSESymbol sseSymbol = (SSESymbol) this.sseSymbols.get(i);
            sseSymbol.recreateShape();
        }
        
        for (int i = 0; i < this.connections.size(); i++) {
            CartoonConnector connection = (CartoonConnector) this.connections.get(i);
            connection.recreateShape();
        }
        
        Bond bond;
        for (int i = 0; i < this.lArcs.size(); i++) {
            bond = (Bond) lArcs.get(i);
            bond.recreateShape();
        }

        for (int i = 0; i < rArcs.size(); i++) {
            bond = (Bond) rArcs.get(i);
            bond.recreateShape();
        }

        for (int i = 0; i < aBonds.size(); i++) {
            bond = (Bond) aBonds.get(i);
            bond.recreateShape();
        }

        for (int i = 0; i < pBonds.size(); i++) {
            bond = (Bond) pBonds.get(i);
            bond.recreateShape();
        }
    }
    
    public void writeToStream(PrintWriter pw) {

        // figures
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol cur_fig = (SSESymbol) this.sseSymbols.get(i);
            pw.write(cur_fig.toString());
            pw.println();
        }
        
        
        Bond bond;
        // l arcs
        for (int i = 0; i < this.lArcs.size(); i++) {
            bond = (Bond) lArcs.get(i);
            pw.write(bond.toString());
            pw.println();
        }

        // r arc
        for (int i = 0; i < rArcs.size(); i++) {
            bond = (Bond) rArcs.get(i);
            pw.write(bond.toString());
            pw.println();
        }

        // a_bonds
        for (int i = 0; i < aBonds.size(); i++) {
            bond = (Bond) aBonds.get(i);
            pw.write(bond.toString());
            pw.println();
        }

        // p_bonds
        for (int i = 0; i < pBonds.size(); i++) {
            bond = (Bond) pBonds.get(i);
            pw.write(bond.toString());
            pw.println();
        }
    }
    
    public Dimension getSize() {
        
        int largest_x = 0;
        int largest_y = 0;

        for (int i = 0; i < sseSymbols.size(); i++) {
            SSESymbol cur_fig = (SSESymbol) sseSymbols.get(i);
            Rectangle fig_rect = cur_fig.getShape().getBounds();

            if (i == 0) {
                // the largest_x and y become the first entry
                largest_x = fig_rect.x;
                largest_y = fig_rect.y;
            } else {
                if (fig_rect.x > largest_x) {
                    largest_x = fig_rect.x;
                }

                if (fig_rect.y > largest_y) {
                    largest_y = fig_rect.y;
                }
            }
        } 
        return new Dimension(largest_x, largest_y);
    }
    
    public Point getCenterPoint() {
        int centerX = 0;
        int centerY = 0;
        
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = (SSESymbol) this.sseSymbols.get(i);
            Point c = sseSymbol.getCenter();
            centerX += c.x;
            centerY += c.y;
        }
        int n = this.sseSymbols.size();
        centerX = (int) (((float) centerX) / ((float) n));
        centerY = (int) (((float) centerY) / ((float) n));
        
        return new Point(centerX, centerY);
    }
    
    public Rectangle2D getBoundingBox() {
        double minX = 0;
        double minY = 0;
        double maxX = 0;
        double maxY = 0;
        
        for (int i = 0; i < sseSymbols.size(); i++) {
            SSESymbol sseSymbol = (SSESymbol) sseSymbols.get(i);
            Rectangle symbolBounds = sseSymbol.getShape().getBounds();
            
            if (i == 0) {
                minX = symbolBounds.x;
                minY = symbolBounds.y;
                maxX = symbolBounds.getMaxX();
                maxY = symbolBounds.getMaxY();
            } else {
                if (symbolBounds.x < minX) minX = symbolBounds.x;
                if (symbolBounds.y < minY) minY = symbolBounds.y;
                if (symbolBounds.getMaxX() > maxX) maxX = symbolBounds.getMaxX();
                if (symbolBounds.getMaxY() > maxY) maxY = symbolBounds.getMaxY();
            }
        }
        
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }
    
    public boolean hasSelectedConnector() {
        return this.selected_connector != null;
    }
    
    public CartoonConnector getSelectedConnector() {
        return this.selected_connector;
    }
    
    public void setSelectedConnector(CartoonConnector connection) {
        this.selected_connector = connection;
    }
    
    public void removeSSESymbol(int symbolNumber) {
        // add the new connection
        int n = this.numberOfSSESymbols();
        if (n > 1) {
            // SPECIAL CASE! Selected the first symbol
            if ((n != 1 && symbolNumber != 1 && (symbolNumber != n))) {
                SSESymbol s = this.getSSESymbol(symbolNumber);
                SSESymbol d = this.getSSESymbol(symbolNumber - 2);
                
                CartoonConnector con = new CartoonConnector(symbolNumber - 1, s, d);
                this.addCartoonConnector(con);
            }
        }

        //remove the connection
        int size = this.numberOfConnectors();
        int loop = 0;

        boolean in_coming_only = symbolNumber == n;
        boolean out_going_only = symbolNumber == 1;

        while (loop < size) {
            CartoonConnector bond = this.getCartoonConnector(loop);
            int source_num = bond.getStartSSESymbol().getSymbolNumber();
            int dest_num = bond.getEndSSESymbol().getSymbolNumber();

            if (in_coming_only) {
                if (dest_num == symbolNumber)
                    this.removeCartoonConnector(bond);
                break;
            } else if (out_going_only) {
                if (source_num == symbolNumber)
                    this.removeCartoonConnector(bond);
                break;
            } else {
                if (source_num == symbolNumber || dest_num == symbolNumber) {

                    this.removeCartoonConnector(bond);
                    size = size - 1;
                    loop = loop - 1;
                }
            }
            loop++;
        }
        
        // inefficient, but equivalent to previous code
        this.removeArc(symbolNumber, 0);
        this.removeArc(symbolNumber, 1);
        this.removeArc(symbolNumber, 2);
        this.removeArc(symbolNumber, 3);

        this.removeSSESymbol(symbolNumber);
    }
    
    // make the N/C termini for an empty Cartoon
    public void createTermini() {
        int terminusSize = this.MAX_SSE_SIZE / 2;
        Box nTerminus = new Box(1, 250, 250, terminusSize, "N");
        Box cTerminus = new Box(3, 350, 250, terminusSize, "C"); 

        this.sseSymbols.add(nTerminus);
        this.sseSymbols.add(cTerminus);

        CartoonConnector con = new CartoonConnector(2, nTerminus, cTerminus);
        this.connections.add(con);
    }
    
    public void fixTerminalPositions() {
    	int numberOfSymbols = this.sseSymbols.size();
    	SSESymbol nTerminus = this.getSSESymbol(0);
    	SSESymbol cTerminus = this.getSSESymbol(numberOfSymbols - 1);
    	
    	// guard against calling this method on an empty cartoon
    	if (numberOfSymbols > 2) {
    		SSESymbol firstSSE = this.getSSESymbol(1);
    		Point firstCenter = firstSSE.getBoundingBoxCenter();
    		
    		// FIXME : what if there are only 3 symbols?
    		if (numberOfSymbols > 3) {
    			SSESymbol secondSSE = this.getSSESymbol(2);
    			Point secondCenter = secondSSE.getBoundingBoxCenter();
    			int dx = secondCenter.x - firstCenter.x;
    			int dy = secondCenter.y - firstCenter.y;
    			nTerminus.setPosition(firstCenter.x - dx, firstCenter.y - dy);
    			this.getCartoonConnector(0).recreateShape();
    		}	
    		
    		SSESymbol lastSSE = this.getSSESymbol(numberOfSymbols - 2);
    		Point lastCenter = lastSSE.getBoundingBoxCenter(); 
    		
    		if (numberOfSymbols > 4) {
    			SSESymbol penultimateSSE = this.getSSESymbol(numberOfSymbols - 3);
    			Point penultimateCenter = penultimateSSE.getBoundingBoxCenter();
    			int dx = penultimateCenter.x - lastCenter.x;
    			int dy = penultimateCenter.y - lastCenter.y;
    			cTerminus.setPosition(lastCenter.x - dx, lastCenter.y - dy);
    			this.getCartoonConnector(this.connections.size() - 1).recreateShape();
    		}
    	}
    	
    }
    
    public Iterator sseSymbolIterator() {
        return this.sseSymbols.iterator();
    }

    public void clear() {
        this.sseSymbols.clear();
        this.connections.clear();
        this.rArcs.clear();
        this.lArcs.clear();
        this.aBonds.clear();
        this.pBonds.clear();
        
        // after clearing, we still want to display N/C boxes
        this.createTermini();
    }
    
    public void addSSESymbol(SSESymbol sseSymbol) {
        if (this.selected_connector != null) {
            this.addSSESymbolAtSelectedConnector(sseSymbol);
        } else {
            this.addSSESymbolBeforeCTerminus(sseSymbol);
        }
    }

    /*
     * Use the selected connector to add sseSymbol between the endpoints. 
     */
    public void addSSESymbolAtSelectedConnector(SSESymbol sseSymbol) {
        SSESymbol start = this.selected_connector.getStartSSESymbol();
        SSESymbol end =  this.selected_connector.getEndSSESymbol();
        this.insertSymbol(start, end, sseSymbol);
    }
    
    /*
     * If just clicking somewhere on the canvas, adds a sseSymbol just before the CTerm. 
     */
    public void addSSESymbolBeforeCTerminus(SSESymbol sseSymbol) {
    	this.addSSESymbolBeforeCTerminus(sseSymbol, true);
    }
    
    public void addSSESymbolBeforeCTerminus(SSESymbol sseSymbol, boolean selectSymbol) {
        
        int numberOfSSESymbols = this.numberOfSSESymbols();
        SSESymbol source;
        if (numberOfSSESymbols == 2) {  // only N/C
            source = (SSESymbol) this.sseSymbols.get(0);
        } else {
            source = this.getSSESymbol(numberOfSSESymbols - 2);
        }
        SSESymbol dest = (SSESymbol) this.sseSymbols.get(numberOfSSESymbols - 1);
        this.insertSymbol(source, dest, sseSymbol);
        
        if (selectSymbol) {
        	sseSymbol.setSelectionState(true);
        }
    }
    
    public void insertSymbol(SSESymbol source, SSESymbol dest, SSESymbol newSSESymbol) {
        System.out.println("inserting between " + source + " and " + dest);
        int sourceIndex = this.sseSymbols.indexOf(source);
        if (sourceIndex == -1) {
            System.out.println(source + " not found in :");
            for (int i = 0; i < this.sseSymbols.size(); i++) {
                SSESymbol sseSymbol  = (SSESymbol) this.sseSymbols.get(i);
                System.out.println(sseSymbol);
            }
        }
        sseSymbols.add(sourceIndex + 1, newSSESymbol);
        
        this.renumberSymbolsFrom(sourceIndex + 1);
        newSSESymbol.setSymbolNumber(source.getSymbolNumber() + 1);
        
        CartoonConnector connector = (CartoonConnector) this.connections.get(sourceIndex);
        connector.setEndSSESymbol(newSSESymbol);
        connector.recreateShape();
        
        connections.add(sourceIndex + 1, new CartoonConnector(sourceIndex + 1, newSSESymbol, dest));
    }
    
    public void renumberSymbolsFrom(int fromIndex) {
        for (int i = fromIndex; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = (SSESymbol) this.sseSymbols.get(i);
            sseSymbol.setSymbolNumber(i);
        }
    }
    
    public void updateConnectors(SSESymbol sseSymbol) {
        this.updateConnectors(this.sseSymbols.indexOf(sseSymbol));
    }

    public void updateConnectors(int symbolIndex) {
        if (symbolIndex > 0) {
            CartoonConnector previous = (CartoonConnector) this.connections.get(symbolIndex - 1);
            previous.recreateShape();
        }
        
        if (symbolIndex < this.sseSymbols.size() - 1) {
            CartoonConnector next = (CartoonConnector) this.connections.get(symbolIndex);
            next.recreateShape();
        }
    }

    public SSESymbol selectSSESymbol(Point p) {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol cur_fig = (SSESymbol) this.sseSymbols.get(i);
            if (cur_fig.containsPoint(p.x, p.y)) {
                cur_fig.setSelectionState(true);
                return cur_fig;
            }
        }
        return null;
    }
    
    public SSESymbol toggleSelectSSESymbol(Point p) {
        SSESymbol selected = null;
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = (SSESymbol) this.sseSymbols.get(i);
            if (sseSymbol.containsPoint(p.x, p.y)) {
                if (sseSymbol.isSelected()) {
                    sseSymbol.setSelectionState(false);
                } else {
                    sseSymbol.setSelectionState(true);
                }
                selected = sseSymbol;
            } else {
                sseSymbol.setSelectionState(false);
            }
        }
        return selected;
    }
    
    public SSESymbol toggleHighlightSSESymbol(Point p) {
        SSESymbol highlighted = null;
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = (SSESymbol) this.sseSymbols.get(i);
            if (sseSymbol.containsPoint(p.x, p.y)) {
                sseSymbol.setHighlightState(true);
                highlighted = sseSymbol;
            } else {
                sseSymbol.setHighlightState(false);
            }
        }
        return highlighted;
    }
    
    public void moveSelectedSSESymbols(int xDif, int yDif) {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol currentSymbol = (SSESymbol) this.sseSymbols.get(i);
            if (currentSymbol.isSelected()) {
                currentSymbol.move(xDif, yDif);
                this.updateConnectors(i);
                this.updateBonds(currentSymbol, aBonds);
                this.updateBonds(currentSymbol, pBonds);
                this.updateBonds(currentSymbol, lArcs);
                this.updateBonds(currentSymbol, rArcs);
            }
        }
    }
    
    public void updateBonds(SSESymbol symbol, ArrayList bonds) {
        for (int i = 0; i < bonds.size(); i++) {
            Bond bond = (Bond) bonds.get(i);
            if (bond.contains(symbol)) {
                bond.recreateShape();
            }
        }
    }
     
    public SSESymbol getSSESymbolAt(Point p) {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol currentSymbol = (SSESymbol) this.sseSymbols.get(i);
            if (currentSymbol.containsPoint(p.x, p.y)) {
                return currentSymbol;
            }
        }
        return null;
    }
    
    public ArrayList getSelectedSSESymbols() {
        ArrayList selected = new ArrayList();
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol currentSymbol = (SSESymbol) this.sseSymbols.get(i);
            if (currentSymbol.isSelected()) {
                selected.add(currentSymbol);
            }
        }
        return selected;
    }
    
    public SSESymbol getSelectedSSESymbol(Point p) {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol currentSymbol = (SSESymbol) this.sseSymbols.get(i);
            if (currentSymbol.isSelected() && currentSymbol.containsPoint(p.x, p.y)) {
                return currentSymbol;
            }
        }
        return null;
    }
    
    public int numberOfSelectedSSESymbols() {
        int numberOfSelectedSSESymbols = 0;
        for (int i = 0; i < sseSymbols.size(); i++) {
            SSESymbol currentSymbol = (SSESymbol)sseSymbols.get(i);
            if (currentSymbol.isSelected()) {
                numberOfSelectedSSESymbols++;
            }
        }
        return numberOfSelectedSSESymbols;
    }
    
    public void selectAllSSESymbols() {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = (SSESymbol) this.sseSymbols.get(i);
            sseSymbol.setSelectionState(true);
        } 
    }
    
    public void deselectAllSSESymbols() {
        for (int i = 0; i < sseSymbols.size(); i++) {
            SSESymbol currentSymbol = (SSESymbol) sseSymbols.get(i);
            currentSymbol.setSelectionState(false);
        }
    }
    
    // returns true if the first sseSymbol is a strand (gmt:?)
    public boolean connectionStartsOnTop() {
        if (this.numberOfSSESymbols() > 0) {
            SSESymbol first_fig = this.getSSESymbol(0);
            return first_fig instanceof EquilateralTriangle;
        } else
            return false;
    }
    
    public void centerOn(int x, int y) {
    	this.centerOn(new Point(x, y));
    }
    
    public void centerOn(Point p) {
        Point center = this.getCenterPoint();
        System.out.println("centering " + center + " on " + p);
        int dX = p.x - center.x;
        int dY = p.y - center.y;
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = (SSESymbol) this.sseSymbols.get(i);
            sseSymbol.move(dX, dY);
        }
        this.relayout();
    }
    
    public void flip() {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol currentSymbol = (SSESymbol) (this.sseSymbols.get(i));
            currentSymbol.flip();
        }
    }
    
    public void flipSymbol(SSESymbol symbol) {
        symbol.flip();
        this.updateConnectors(symbol);
    }
    
    public void flipXAxis(Point p) {
        for (int i = 0; i < this.numberOfSSESymbols(); i++) {

            SSESymbol currentSSESymbol = (SSESymbol) this.sseSymbols.get(i);

            int point_y = p.y;
            int y_dif;

            Point currentPoint = currentSSESymbol.getCenter();

            if (currentPoint.y > p.y) {
                y_dif = currentPoint.y - point_y;
                currentSSESymbol.move(0, -y_dif);
            } else {
                y_dif = point_y - currentPoint.y;
                currentSSESymbol.move(0, y_dif);
            }
        }
        this.relayout();
    }
    
    public void flipYAxis(Point p) {
        for (int i = 0; i < this.numberOfSSESymbols(); i++) {

            SSESymbol currentSSESymbol = (SSESymbol) this.sseSymbols.get(i);
            int point_x = p.x;
            int x_dif;

            Point currentPoint = currentSSESymbol.getCenter();

            if (currentPoint.y > p.y) {
                x_dif = currentPoint.y - point_x;
                currentSSESymbol.move(-x_dif, 0);
            } else {
                x_dif = point_x - currentPoint.y;
                currentSSESymbol.move(x_dif, 0);
            }
        }
        this.relayout();
    }

 
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = (SSESymbol) this.sseSymbols.get(i);
            sseSymbol.draw(g2);
        }
        
        for (int i = 0; i < this.connections.size(); i++) {
            CartoonConnector connection = (CartoonConnector) this.connections.get(i);
            connection.draw(g2);
        }
        
        Iterator e = lArcs.iterator();
        while (e.hasNext()) {
            ((DashedLine) e.next()).draw(g2);
        }

        e = rArcs.iterator();
        while (e.hasNext()) {
            ((DashedLine) e.next()).draw(g2);
        }

        e = pBonds.iterator();
        while (e.hasNext()) {
            ((DashedLine) e.next()).draw(g2);
        }

        e = aBonds.iterator();
        while (e.hasNext()) {
            ((DashedLine) e.next()).draw(g2);
        }
    }
    
    public SSESymbol getSSESymbol(int i) {
        return (SSESymbol) this.sseSymbols.get(i);
    }
    
    public SSESymbol getSSESymbolByNumber(int symbolnum) {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol fig = (SSESymbol) this.sseSymbols.get(i);
            if (fig.hasSymbolNumber(symbolnum)) {
                return fig;
            }
        }
        return null;
    }

    public int numberOfSSESymbols() {
        return this.sseSymbols.size();
    }
    
    public void addCartoonConnector(CartoonConnector c) {
        this.connections.add(c);
    }
    
    public void addCartoonConnector(int i, CartoonConnector c) {
        this.connections.add(i, c);
    }
    
    public CartoonConnector getCartoonConnector(int i) {
        return (CartoonConnector) this.connections.get(i);
    }
    
    public void removeCartoonConnector(CartoonConnector connection) {
        this.connections.remove(connection);
    }
    
    public int numberOfConnectors() {
        return this.connections.size();
    }
    
    public void checkSize(int sseSize) {
        if (sseSize != this.CURRENT_SSE_SIZE) {
            this.CURRENT_SSE_SIZE = sseSize;
            this.resizeSymbols();
        }
    }
    
    public void resizeSymbols() {
        
    }
    
    public void createUpStrand(int symbolNumber, int x, int y, int canvasSize) {
        int sseSize = this.getSSESize(canvasSize);
        EquilateralTriangle equilateralTriangle = new EquilateralTriangle(symbolNumber, x, y, sseSize, false);
        this.checkSize(sseSize);
        this.addSSESymbol(equilateralTriangle);
    }
    
    public void createDownStrand(int symbolNumber, int x, int y, int canvasSize) {
        int sseSize = this.getSSESize(canvasSize);
        EquilateralTriangle equilateralTriangle = new EquilateralTriangle(symbolNumber, x, y, sseSize, true);
        this.checkSize(sseSize);
        this.addSSESymbol(equilateralTriangle);
    }
    
    public void createUpHelix(int symbolNumber, int x, int y, int canvasSize) {
        int sseSize = this.getSSESize(canvasSize);
        Circle circle = new Circle(symbolNumber, x, y, sseSize, false);
        this.checkSize(sseSize);
        this.addSSESymbol(circle);
    }
    
    public void createDownHelix(int symbolNumber, int x, int y, int canvasSize) {
        int sseSize = this.getSSESize(canvasSize);
        Circle circle = new Circle(symbolNumber, x, y, sseSize, true);
        this.checkSize(sseSize);
        this.addSSESymbol(circle);
    }
    
    public void createRightArc(SSESymbol source, SSESymbol destination) {
        this.rArcs.add(new DashedLine(source, destination, Bond.RIGHT_CHIRAL));
    }
    
    public void createLeftArc(SSESymbol source, SSESymbol destination) {
        this.lArcs.add(new DashedLine(source, destination, Bond.LEFT_CHIRAL));
    }
    
    public void createABond(SSESymbol source, SSESymbol destination) {
        this.aBonds.add(new DashedLine(source, destination, Bond.ANTIPARALLEL_HBOND));
    }
    
    public void createPBond(SSESymbol source, SSESymbol destination) {
        this.pBonds.add(new DashedLine(source, destination, Bond.PARALLEL_HBOND));
    }

    public void removeArc(int symbolNumber, int arcType) {
        Iterator itr;
        switch (arcType) {
            case Bond.RIGHT_CHIRAL: itr = this.rArcs.iterator(); break;
            case Bond.LEFT_CHIRAL: itr = this.lArcs.iterator(); break;
            case Bond.ANTIPARALLEL_HBOND: itr = this.aBonds.iterator(); break;
            case Bond.PARALLEL_HBOND: itr = this.pBonds.iterator(); break;
            default: return;
        }
        
        while (itr.hasNext()) {
            ConnectionSymbol bond = (Bond) itr.next();
            int source_num = bond.getStartSSESymbol().getSymbolNumber();
            int dest_num = bond.getEndSSESymbol().getSymbolNumber();

            if (source_num == symbolNumber || dest_num == symbolNumber) {
                itr.remove();
                return;
            }
        }
    }

    public void deleteSSESymbol(SSESymbol sseSymbol) {
        deleteBonds(sseSymbol, this.lArcs);
        deleteBonds(sseSymbol, this.rArcs);
        deleteBonds(sseSymbol, this.aBonds);
        deleteBonds(sseSymbol, this.pBonds);
        deleteConnections(sseSymbol);
        this.sseSymbols.remove(sseSymbol);
    }
    
    private void deleteBonds(SSESymbol sseSymbol, ArrayList bonds) {
        Iterator itr = bonds.iterator();
        while (itr.hasNext()) {
            Bond bond = (Bond) itr.next();
            if (bond.contains(sseSymbol)) {
                itr.remove();
            }
        }
    }

    private void deleteConnections(SSESymbol sseSymbol) {
        int symbolIndex = this.sseSymbols.indexOf(sseSymbol);
        SSESymbol nextSymbol = (SSESymbol) this.sseSymbols.get(symbolIndex + 1);
        
        CartoonConnector previousConnection = (CartoonConnector) this.connections.get(symbolIndex - 1);
        this.connections.remove(symbolIndex);
        previousConnection.setEndSSESymbol(nextSymbol);
        previousConnection.recreateShape();
    }
    
    public String getVertexString() {
        // only concerned with the items in the sseSymbols araylist

        //THIS IS HORIBLE : basically, we don't know if there are inserts until we encounter one
        //...so in order to get a proper vertex string like N[]E[]E[1:2]E[]C we have to make BOTH!
        String vertexSt = "";
        String insertedVertexSt = "";

        //this is a hack to ensure there are [] after N and before C, but ONLY if there are any other inserts!
        boolean hasInserts = false;

        for (int i = 0; i < sseSymbols.size(); i++) {
            SSESymbol fig = ((SSESymbol)sseSymbols.get(i));


            if (i < sseSymbols.size()) {
                // now add the range info
                for (int j = 0; j < this.connections.size(); j++) {
                    // get the connection


                    CartoonConnector cur_con = (CartoonConnector)connections.get(j);

                    int con_source = cur_con.getStartSSESymbol().getSymbolNumber();
                    int con_dest = cur_con.getEndSSESymbol().getSymbolNumber();


                    if ( (con_source == i && con_dest == i + 1 ) ||
                            (con_source == i + 1 && con_dest == i) ) {
                        // we add the description of the connection to the
                        // vertex string
                        
                        // THIS IS MADNESS
                        //String range_info = cur_con.label();
                        String range_info = ""; // FIXME

                        //range_info.replaceAll(":", "-");

                        if (range_info.equals("")) {
                            insertedVertexSt += "[]";
                        } else {
                            hasInserts = true;
                            if (range_info.equals("*"))
                                range_info = "";

                            insertedVertexSt += "[" + range_info + "]";
                        }
                    }
                } // for
            } //  if

            if (fig instanceof EquilateralTriangle && !fig.isDown()) {
                vertexSt += "E";
                insertedVertexSt += "E";
            } else if (fig instanceof EquilateralTriangle && fig.isDown()) {
                vertexSt += "e";
                insertedVertexSt += "e";
            } else if (fig instanceof Circle && fig.isDown()) {
                vertexSt += "h";
                insertedVertexSt += "h";
            } else if (fig instanceof Circle && !fig.isDown()) {
                vertexSt += "H";
                insertedVertexSt += "H";
            }
        }
        // now add on the last vertex
        // we are looking one less than normal

        if (hasInserts) {
            return "N[]" + insertedVertexSt + "[]C";
        } else {
            return "N" + vertexSt + "C";
        }
    }

    public String getEdgeString() {
        String edgeVertex = "";
        validateHBonds();

        for (int i = 0; i < sseSymbols.size(); i++) {
            edgeVertex += getBondInfo(i + 1, rArcs, "R");
            edgeVertex += getBondInfo(i + 1, lArcs, "L");
            edgeVertex += getBondInfo(i + 1, aBonds, "A");
            edgeVertex += getBondInfo(i + 1, pBonds, "P");
        }
        return edgeVertex;
    }

    public void validateHBonds() {
        // loop through the aBonds and pBonds loking for bonds that
        // should be switched

        ArrayList temp_bonds = new ArrayList();
        temp_bonds.clear();

        for (int i = 0; i < pBonds.size(); i++) {
            // look for bonds in aBonds that should be in pBonds
            Bond cur_bond = (Bond)pBonds.get(i);

            SSESymbol source = cur_bond.getStartSSESymbol();
            SSESymbol dest = cur_bond.getEndSSESymbol();

            // now look for A bonds
            if ((source instanceof EquilateralTriangle && source.isDown()) 
                    || (source instanceof EquilateralTriangle && dest instanceof EquilateralTriangle)) {
                // remove this bond from a_Bonds, it belongs in P bonds
                // strore them into temp bonds temporarily.
                temp_bonds.add(cur_bond);
            }
        }
        pBonds.removeAll(temp_bonds);
        aBonds.addAll(temp_bonds);

        temp_bonds.clear();
        for (int i = 0; i < aBonds.size(); i++) {
            // look for bonds in aBonds that should be in pBonds
            Bond cur_bond = (Bond)aBonds.get(i);

            SSESymbol source = cur_bond.getStartSSESymbol();
            SSESymbol dest = cur_bond.getEndSSESymbol();

            String source_class = source.getClass().toString();
            String dest_class = dest.getClass().toString();

            // now look for P bonds
            if (source_class.equals(dest_class) ) {
                // remove this bond from a_Bonds, it belongs in P bonds
                // strore them into temp bonds temporarily.
                temp_bonds.add(cur_bond);
            }
        }
        aBonds.removeAll(temp_bonds);
        pBonds.addAll(temp_bonds);

    }

    // returns the R_Arc from that is created between a symbol and another symbol
    // if one of those symbols is sseSymbols<symbolNum>
    public String getBondInfo(int symbolNum, ArrayList ar, String sse_rep) {
        String bond_string = "";
        for (int j = 0; j < ar.size(); j++) {
            Bond bond = ((Bond)ar.get(j));

            // the source number for this arc
            int source_num = bond.getStartSSESymbol().getSymbolNumber() - 1;
            int dest_num = bond.getEndSSESymbol().getSymbolNumber() - 1;

            if (source_num == symbolNum) {
                boolean souceLessThanDest = source_num < dest_num;
                if (souceLessThanDest == true)
                    bond_string += source_num + ":" + dest_num + sse_rep + " ";

                if (souceLessThanDest == false)
                    bond_string += dest_num + ":" + source_num + sse_rep + " ";
            }
        }
        return bond_string;
    }

    
    public boolean bondBetween(SSESymbol start, SSESymbol end, ArrayList bonds) {
        for (int i = 0; i < bonds.size(); i++) {
            Bond bond = (Bond) bonds.get(i);
            if (bond.contains(start) && bond.contains(end)) {
                return true;
            }
        }
        return false;
    }

    // returns true if a Hydrogen bond can be creates between the source and the destination
    public boolean canCreateHydrogenBond(SSESymbol source, SSESymbol dest) {
        //can only create a Hydrogen bond between Strands
        if (source instanceof Circle || dest instanceof Circle) {
            return false;
        }

        if ((bondBetween(source, dest, this.aBonds)) || (bondBetween(source, dest, this.pBonds))) {
            return false;
        }
        return true;
    }

    public boolean shouldCreateParallelBond(SSESymbol source, SSESymbol dest) {
        return (source.isDown() && dest.isDown()) || (!source.isDown() && !dest.isDown());
    }

    public boolean shouldCreateAntiParallelBond(SSESymbol source, SSESymbol dest) {
        return (!source.isDown() && dest.isDown()) || (!source.isDown() && dest.isDown());
    }

    public boolean canCreateRArc(SSESymbol source, SSESymbol dest) {
        boolean isValid;

        if (source instanceof EquilateralTriangle) {
            isValid = dest instanceof EquilateralTriangle;
        } else if (source instanceof Circle){
            isValid = dest instanceof Circle;
        }

        if (bondBetween(source, dest, this.aBonds)) {
            return false;
        }

        isValid = !(bondBetween(source, dest, this.lArcs));
        isValid = !(bondBetween(source, dest, this.rArcs));

        return isValid;
    }

    //returns true if it is valid to ceate a R arc between the source and destination
    public boolean canCreateLArc(SSESymbol source, SSESymbol dest) {
        boolean isValid = true;

        //check if there exists an L Arc between the source and destination
        if (source instanceof EquilateralTriangle)
            isValid = (dest instanceof EquilateralTriangle);
        else if (source instanceof Circle )
            isValid = dest instanceof Circle;

        if (bondBetween(source, dest, this.aBonds))   // can't create a L_A bond
            isValid = isValid && false;

        isValid = !(bondBetween(source, dest, this.rArcs));
        isValid = !(bondBetween(source, dest, this.lArcs));
        return isValid;
    }

    public boolean canCreatePBond(SSESymbol source, SSESymbol dest) {
       return source != dest;
    }

    public boolean canCreateABond(SSESymbol source, SSESymbol dest) {
        return !this.bondBetween(source, dest, aBonds) && !this.bondBetween(source, dest, pBonds);
    }

    // outputs this SSE String
    public String toString() {
        String vertex = this.getVertexString();
        String edge = this.getEdgeString();
        return vertex + " " + edge;
    }

}
