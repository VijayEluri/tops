package tops.port;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.junit.Test;

import tops.dw.protein.DomainDefinition;
import tops.dw.protein.SecStrucElement;
import tops.dw.protein.TopsFileFormatException;
import tops.port.calculate.Configure;
import tops.port.model.Chain;
import tops.port.model.DsspReader;
import tops.port.model.Protein;
import tops.port.model.SSE;
import tops.view.tops2D.cartoon.CartoonDrawer;

public class TestOptimise {
    
    @Test
    public void test1GSO() throws IOException {
        DsspReader dsspReader = new DsspReader();
        Protein protein = 
                dsspReader.readDsspFile("/Users/maclean/data/dssp/reps/1ifc.dssp");
        Chain chain = protein.getChains().get(0);
        Configure configure = new Configure();
        configure.configure(chain);
        
        Optimise optimise = new Optimise();
        optimise.optimise(chain);
        for (SSE sse : chain.getSSEs()) {
            System.out.println(sse.getSymbolNumber() + 
                    String.format(" at (%s, %s) is %s", 
                            sse.getCartoonX(), sse.getCartoonY(), sse.getDirection()));

        }
        draw("1ifc", chain, "test.png");
//        System.out.println(chain.toTopsFile());
    }
    
    private void draw(String name, Chain chain, String outputFilepath) throws TopsFileFormatException, IOException {
        String topsFile = chain.topsHeader(name) + "\n" + chain.toTopsFile();
//        System.out.println(topsFile);
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("tmp.tops")));
        writer.write(topsFile);
        writer.close();
        
        tops.dw.protein.Protein dwProtein = new tops.dw.protein.Protein("tmp.tops");
        Vector<DomainDefinition> dd = dwProtein.getDomainDefs();
        Vector<SecStrucElement> ll = dwProtein.getLinkedLists();
        CartoonDrawer drawer = new CartoonDrawer();
        
        FileOutputStream fos = new FileOutputStream(outputFilepath);
        final int w = 300;
        final int h = 300;
        for (int i = 0; i < dd.size(); i++) {
            SecStrucElement root = (SecStrucElement) ll.get(i);
            System.out.println("drawing");
            drawer.draw(name, "IMG", w, h, root, fos);
        }
    }

}
