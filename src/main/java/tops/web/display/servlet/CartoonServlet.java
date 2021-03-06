package tops.web.display.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tops.dw.io.TopsFileWriter;
import tops.dw.protein.Cartoon;
import tops.dw.protein.Protein;
import tops.view.cartoon.CartoonDrawer;

public class CartoonServlet extends HttpServlet {

	private static final long serialVersionUID = -8579686840885253792L;

	static {
        System.setProperty("java.awt.headless", "true");
    }

    private static final int DEFAULT_WIDTH = 200;

    private static final int DEFAULT_HEIGHT = 200;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
    	// convert the URI into a cartoon source
    	ServletConfig config = this.getServletConfig();
        CartoonDataSource source = new URICartoonDataSource(request, config);
        
        Protein protein;
        try {
        	protein = source.getCartoon();
        	if (protein == null) { 
        		System.out.println("protein null!");	// XXX - throw error
        	}
        	handle(protein, source.getParams(), response);
        } catch (IOException ioe) {
        	this.error(ioe.getMessage(), response);
        	return;
        }

    }
    
    private void handle(Protein protein, Map<String, String> params, HttpServletResponse response) throws IOException {
        // quickly check if we only want the text of the file
        if (params.get("fileType").equals("tops")) { // TOPS! file format (basically, old-style tops file!)
            response.setContentType("text/plain");
            ServletOutputStream out = response.getOutputStream();
            TopsFileWriter topsFileWriter = new TopsFileWriter();
            topsFileWriter.writeTopsFile(protein, out);
            return;
        }

        Cartoon cartoon = protein.getDomain(0);	// for now...
        String domain = params.get("domain");

        if (cartoon == null) {
            this.error("Domain not found : " + domain, response);
            return;
        }

        String fileType = params.get("fileType");
        String widthParam = params.get("width");
        String heightParam = params.get("height");
        int width = (widthParam == null)? DEFAULT_WIDTH : Integer.parseInt(widthParam);
        int height = (heightParam == null)? DEFAULT_HEIGHT : Integer.parseInt(heightParam);
        CartoonDrawer drawer = new CartoonDrawer();
        if (fileType.equals("gif")) { // Image - could be several types?

            response.setContentType("image/gif");
            OutputStream out = response.getOutputStream();

            drawer.draw(domain, "IMG", width, height, cartoon, out);

        } else if (fileType.equals("pdf")) { // Portable Document Format, using iText library

            response.setContentType("application/pdf");
            OutputStream out = response.getOutputStream();

            drawer.draw(domain, "PDF", width, height, cartoon, out);

        } else if (fileType.equals("svg")) { // Support Vector Graphics - basically XML

            response.setContentType("image/svg+xml");
            PrintWriter writer = response.getWriter();

            drawer.draw(domain, "SVG", cartoon, writer);

        } else if (fileType.equals("ps")) { // Postscript - basically text

            response.setContentType("application/ps");
            PrintWriter writer = response.getWriter();

            drawer.draw(domain, "PS", cartoon, writer);

        } else {
            this.error("filetype is not supported", response);
            return;
        }
    }

    private void error(String problem, HttpServletResponse response) {
        response.setContentType("text/html");
        PrintWriter pout = null;
        try {
            pout = response.getWriter();
        } catch (IOException ioe) {
        }

        pout.println("<html><head><title>ERROR</title></head>");
        pout.println("<body>Sorry, could not draw this cartoon. Reason : <p>" + problem);
        pout.println("</body></html>");
    }

}
