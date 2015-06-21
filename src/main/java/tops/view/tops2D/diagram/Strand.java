package tops.view.tops2D.diagram;

import java.awt.Shape;
import java.awt.geom.GeneralPath;


public class Strand extends SSE {

    public Strand(boolean b, int i) {
        super(b, i);
    }

    @Override
    public Shape createShape() {
        int bx = (int) (this.bb.getX());
        int by = (int) (this.bb.getY());
        int bh = (int) (this.bb.getHeight());
        int bw = (int) (this.bb.getWidth());
        int pnty = (this.isDown) ? (by + bh) : by;

        GeneralPath gp = new GeneralPath();
        gp.moveTo(bx + (bw / 2), pnty);
        gp.lineTo(bx + bw, by + (bh / 2));
        gp.lineTo(bx, by + (bh / 2));
        gp.closePath();
        return gp;
    }

}