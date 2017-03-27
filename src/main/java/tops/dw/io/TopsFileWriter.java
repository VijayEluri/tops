package tops.dw.io;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.List;

import tops.dw.protein.DomainDefinition;
import tops.dw.protein.IntegerInterval;
import tops.dw.protein.Protein;
import tops.dw.protein.SecStrucElement;

public class TopsFileWriter {
    

    public void writeTopsFile(Protein protein, OutputStream os) {

        if (os == null)
            return;

        PrintWriter pw = new PrintWriter(os, true);

        this.writeTopsHeader(protein, pw);

        if ((protein.getDomainDefs() != null) && (protein.getLinkedLists() != null)) {

            List<DomainDefinition> ddefs = protein.getDomainDefs();
            List<SecStrucElement> lls = protein.getLinkedLists();

            int i;
            for (i = 0; i < ddefs.size(); i++) {

                DomainDefinition dd = ddefs.get(i);
                pw.print("DOMAIN_NUMBER " + i + " " + dd.getCATHcode());
                Enumeration<IntegerInterval> sfs = dd.getSequenceFragments();
                while (sfs.hasMoreElements()) {
                    IntegerInterval sf = sfs.nextElement();
                    // XXX currently printing a 0 fragment index
                    pw.print(" " + 0 + " " + sf.getLower() + " " + sf.getUpper());
                }
                pw.print("\n\n");

                SecStrucElement s;
                for (s = lls.get(0); s != null; s = s.GetTo()) {
                    s.PrintAsText(pw);
                    pw.print("\n");
                }

            }
        }

    }

    private void writeTopsHeader(Protein protein, PrintWriter pw) {

        pw.println("##");
        pw.println("## TOPS: tops.dw.protein topology information file");
        pw.println("##");
        pw.println("## Protein code " + protein.getName());

        pw.println("## Number of domains " + protein.numberDomains());
        pw.println("##");
        pw.print("\n");

    }


}
