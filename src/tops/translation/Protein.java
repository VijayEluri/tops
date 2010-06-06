package tops.translation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Protein {

    private String id;

    private ArrayList chains;

    public Protein() {
        this.id = "";
        this.chains = new ArrayList();
    }

    public Protein(String id) {
        this();
        this.id = id.toLowerCase();
    }

    public String getID() {
        return this.id;
    }

    public void addChain(Chain chain) {
        this.chains.add(chain);
    }

    public Iterator chainIterator() {
        return this.chains.iterator();
    }

    /*
     * public void findStructure(StructureFinder structureFinder) { for (int i =
     * 0; i < this.chains.size(); i++) { structureFinder.findStructure((Chain)
     * this.chains.get(i)); } }
     */

    public HashMap toTopsDomainStrings(HashMap chainDomainMap) {
        HashMap chainDomainStringMap = new HashMap();
        for (int i = 0; i < this.chains.size(); i++) {
            Chain chain = (Chain) this.chains.get(i);
            chainDomainStringMap.put(chain.getCathCompatibleLabel(), chain
                    .toTopsDomainStrings(chainDomainMap));
        }
        return chainDomainStringMap;
    }

    public String[] toTopsChainStringArray() {
        String[] chainStrings = new String[this.chains.size()];
        for (int i = 0; i < this.chains.size(); i++) {
            Chain chain = (Chain) this.chains.get(i);
            chainStrings[i] = chain.toTopsString(new Domain(0));
        }
        return chainStrings;
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < this.chains.size(); i++) {
            Chain chain = (Chain) this.chains.get(i);
            stringBuffer.append(chain.toString());
        }
        return stringBuffer.toString();
    }

}
