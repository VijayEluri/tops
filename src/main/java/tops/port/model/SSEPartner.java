package tops.port.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SSEPartner {
    
    class Atom {
        
        public String name;
        public double[] coords;

        public Atom(String name, double x, double y, double z) {
            this.name = name;
            this.coords = new double[] { x, y, z };
        }
    }

    class HBond {
        
        public Atom atomA;
        public Atom atomB;
        public double energy;

        public HBond(Atom atomA, Atom atomB, double energy) {
            this.atomA = atomA;
            this.atomB = atomB;
            this.energy = energy;
        }
    }
    
    class Residue {
        
        public int residueNumber;
        public int residueType;
        public Map<String, Atom> atoms;
        public List<HBond> hbonds;

        public Residue(int residueNumber, int residueType) {
            this.residueNumber = residueNumber;
            this.residueType = residueType;
            this.atoms = new HashMap<>();
            this.hbonds = new ArrayList<>();
        }
        
        public void addAtom(Atom atom) {
            this.atoms.put(atom.name, atom);
        }

        public Atom getAtom(String atomName) {
            return this.atoms.get(atomName);
        }

        public void hBond(String atomName, Atom otherAtom, double energy) {
            Atom thisAtom = this.getAtom(atomName);
            this.hbonds.add(new HBond(thisAtom, otherAtom, energy));
        }
    }
    
    class ResiduePair {
        Residue residueA;
        Residue residueB;
        int bridgeType;
        double energy;

        public ResiduePair(Residue residueA, Residue residueB, int bridgeType, double energy) {
            this.residueA = residueA;
            this.residueB = residueB;
            this.bridgeType = bridgeType;
            this.energy = energy;
        }
    }
    
    private List<ResiduePair> residuePairs;
    
    private BridgeType bridgeType;

    public SSEPartner() {
        this.residuePairs = new ArrayList<>();
    }
    
    public boolean contains(SSE sse) {
        // XXX TODO
        return false;
    }

    public List<ResiduePair> bridgeRange() {
        return this.residuePairs;
    }

    public int numberBonds() {
        return this.residuePairs.size();
    }

    public BridgeType getBridgeType() {
        return bridgeType;
    }

    public void setBridgeType(BridgeType bridgeType) {
        this.bridgeType = bridgeType;
    }
}
