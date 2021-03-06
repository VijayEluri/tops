package tops.dw.protein;

import java.util.ArrayList;
import java.util.List;

import tops.port.model.DomainDefinition;


public class Protein {

    private String name;

    private List<Cartoon> topsLinkedLists;

    private List<DomainDefinition> domainDefs;

    public Protein() {
        this.domainDefs = new ArrayList<>();
        this.topsLinkedLists = new ArrayList<>();
        this.name = "Unknown";
    }
    
    public String getName() {
    	return this.name;
    }
    
    public void setName(String name) {
    	this.name = name;
    }

    public void addTopsLinkedList(Cartoon cartoon, DomainDefinition d) {
        this.topsLinkedLists.add(cartoon);
        this.domainDefs.add(d);
    }

    public int getDomainIndex(CathCode cc) {

        int index = -1;
        CathCode compareCathCode;

        for (DomainDefinition domainDef : domainDefs) {
            compareCathCode = domainDef.getCATHcode();
            if (cc.equals(compareCathCode)) {
                return index;
            }
            index++;
        }
        return index;
    }
    
    public Cartoon getRootSSE(String domainName) {
    	for (int i = 0; i < this.domainDefs.size(); i++) {
    		CathCode code = this.domainDefs.get(i).getCATHcode();
    		if (code.toString().equals(domainName)) {
    			return this.topsLinkedLists.get(i);
    		}
    	}
    	return null;
    }

    public List<Cartoon> getLinkedLists() {
        return this.topsLinkedLists;
    }

    public int numberDomains() {
        return this.domainDefs.size();
    }

    public List<DomainDefinition> getDomainDefs() {
        return this.domainDefs;
    }
    
    public Cartoon getDomain(int i) {
    	return this.topsLinkedLists.get(i);
    }

    @Override
    public String toString() {
        return this.name;
    }

}
