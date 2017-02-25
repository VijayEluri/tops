package tops.engine.drg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tops.engine.Edge;
import tops.engine.Result;
import tops.engine.TopsStringFormatException;
import tops.engine.Vertex;

public class Comparer {
    private Pattern largestCommonPattern;
    private Matcher m;

    private static final Edge[] hbond_types = {
        new Edge(new Vertex('E',0), new Vertex('E', 0), 'P'),
        new Edge(new Vertex('e',0), new Vertex('e', 0), 'P'),
        new Edge(new Vertex('e',0), new Vertex('E', 0), 'A'),
        new Edge(new Vertex('E',0), new Vertex('e', 0), 'A'),
    };

    private static final Edge[] chiral_types = {
        new Edge(new Vertex('E',0), new Vertex('E', 0), 'L'),
        new Edge(new Vertex('E',0), new Vertex('E', 0), 'R'),
        new Edge(new Vertex('e',0), new Vertex('e', 0), 'L'),
        new Edge(new Vertex('e',0), new Vertex('e', 0), 'R'),
        new Edge(new Vertex('H',0), new Vertex('H', 0), 'L'),
        new Edge(new Vertex('H',0), new Vertex('H', 0), 'R'),
        new Edge(new Vertex('h',0), new Vertex('h', 0), 'L'),
        new Edge(new Vertex('h',0), new Vertex('h', 0), 'R'),
    };

    public Comparer() {
        this.largestCommonPattern = new Pattern();
        this.m = new Matcher();
    }
    
    public Pattern findPattern(String[] instances) throws TopsStringFormatException {
    	m.setDiagrams(instances);
    	Pattern p = this.extendAndMatch();
        
        String[][] ins = m.generateInserts(p);
        if (ins != null) {
            String[] fin = Utilities.doInserts(ins);
            if (fin != null) {
                String spliced = p.splice(fin);
                Pattern chi = this.doChirals(new Pattern(spliced));
                chi.sortEdges();
                chi.rename("pattern:");
                return chi;
            } else {
                System.out.println("Problem : " + p);
                return p;
            }
        }
        
        return p;
    }
   
    public String matchAndGetInserts(Pattern p) {
        return this.m.matchAndGetInserts(p);
    }

    public String findPatternStringWithInserts(String[] instances) {
        try {
            Pattern p = this.findPattern(instances);
            return this.m.matchAndGetInserts(p);
        } catch (TopsStringFormatException tsfe) {
            System.err.println("AARGH! : " + tsfe.toString());
            return "";
        }
    }

    public String findPatternAndDoCompression(String[] instances, boolean makeInserts) throws TopsStringFormatException {
        Pattern p = this.findPattern(instances);
        float compression = 1 - Utilities.doCompression(instances, p);
        if (makeInserts) {
            String resultWithInserts = m.matchAndGetInserts(p);
            return new String( compression + "\t" + resultWithInserts);
        } else {
            return new String( compression + "\t" + p.toString());
        }
    }

    public void clear() {
        this.largestCommonPattern = new Pattern();
    }

    public Result[] compare(String probe, String[] examples) throws TopsStringFormatException {
        List<Result> results = new ArrayList<Result>(examples.length);
        Pattern[] pair = new Pattern[2];
        pair[0] = new Pattern(probe);
        System.out.println("for probe : \t" + probe);
        m = new Matcher();

        for (int e = 0; e < examples.length; e++) { //for each example, compare to pattern
            pair[1] = new Pattern(examples[e]);
            m.setDiagrams(pair);

            Pattern p = this.extendAndMatch();

            if (m.runsSuccessfully(p)) {
//              String tmp = result.getMatchString(); TODO : remove?
            } else { 
                //System.out.println("SEVERE PROBLEM : FINAL PATTERN " + result + " does not match " + examples[e]); 
            }

            String[][] ins = m.generateInserts(p);
            String[] fin = Utilities.doInserts(ins);

            Pattern chi = p;
            if (fin != null) {
                String spliced = p.splice(fin);
                chi = doChirals(new Pattern(spliced));
            }

            chi.sortEdges();
            chi.rename("pattern:");

            float c2 = Utilities.doDrgCompression(pair, chi);
            float TEMPORARY_HACK = 1 - c2;

            Result r = new Result(TEMPORARY_HACK, pair[1].getName(), 
            		chi.toString(), pair[1].getClassification());

            results.add(r);
            this.clear();               //reset pattern
        }

        Collections.sort(results);
        return (Result[]) results.toArray(new Result[0]);
    }


    public void reproduce(String example) throws TopsStringFormatException {
        String[] ex = {example};

        this.m.setDiagrams(ex);

        Pattern p = this.extendAndMatch();

        p.rename(example.substring(0,example.indexOf(" ")));
        System.out.println("hbonds...." + p);
        String[][] ins = m.generateInserts(p);

        String[] fin = Utilities.doInserts(ins);
        String spliced = p.splice(fin);
        System.out.println("spliced..." + spliced);
        Pattern chi = doChirals(new Pattern(spliced));
        chi.sortEdges();
        System.out.println("chirals..." + chi);
    }

    public Pattern extendAndMatch() {
    	Pattern p = new Pattern();
    	p.addTermini();
    	Pattern newSheet = this.startNewSheet(p);
    	while (newSheet != null) {
    		newSheet = this.startNewSheet(newSheet);
    	}
    	return this.largestCommonPattern;
    }
    
    public void store(Pattern p) {
    	if (p.esize() > this.largestCommonPattern.esize()) {
    		this.largestCommonPattern = p;
    		System.out.println("storing " + p);
    	}
    }
    
    public Pattern startNewSheet(Pattern p) {
    	int lhe = 1;
    	int s = p.sizeOfCurrentSheet();
    	int rhe = (s == 0)? 2 : s;
    	
        for (int i = lhe; i <= rhe; i++) {
            for (int j = i + 1; j <= rhe + 1; j++) {
                for (int t = 0; t < hbond_types.length; t++) {
                    Pattern next = new Pattern(p);
                    next.addNewSheet(i, j, hbond_types[t]);

                    if (m.runsSuccessfully(next)) {
                    	this.store(next);
                    	return this.extendAllPossibleWays(next);
                    } 
                }
            }
        }
        return null;
    }
    
    public Pattern extendAllPossibleWays(Pattern p) {
    	Pattern next;
    	
    	next = this.extendToRight(p);
    	//System.out.println("extended to right : " + next);
        if (next != null) { 
        	extendAllPossibleWays(next); 
        }
        
        next = this.extendFromLeft(p);
        //System.out.println("extended from left : " + next);
        if (next != null) { 
        	extendAllPossibleWays(next); 
        }
        
        next = this.extendCycle(p);
        //System.out.println("extended cyclically : " + next);
        if (next != null) {
        	extendAllPossibleWays(next);
        }
        return next;
    }
    
    public Pattern extendToRight(Pattern p) {
    	int rhe = p.sizeOfCurrentSheet();
    	
        for (int i = 1; i <= rhe; i++) {    
            for (int j = i + 1; j <= rhe; j++) {
            	for (int t = 0; t < hbond_types.length ; t++) {
            		Edge ed = hbond_types[t];
            		char l = ed.getLType();
            		char r = ed.getRType();
            		
            		if (p.canExtend(j - 1, r)) {
            			Pattern next = new Pattern(p);
            			next.insertBefore(i, l);
            			next.extend(i, j, ed);
            			if (this.m.runsSuccessfully(next)) {
            				this.store(next);
            				return next;
            			}
            		}
            	}
            }
        }
        return null;
    }
    
    public Pattern extendFromLeft(Pattern p) {
    	int lhe = p.getLEndpoint();
    	int rhe = p.sizeOfCurrentSheet();
    	
    	for (int i = lhe; i <= rhe; i++) {
            for (int j = i + 1; j <= rhe; j++) {
            	for (int t = 0; t < hbond_types.length ; t++) {
            		Edge ed = hbond_types[t];
            		char l = ed.getLType();
            		char r = ed.getRType();

            		if (p.canExtend(i, l)) {
            			Pattern next = new Pattern(p);
            			next.insertBefore(i, r);
            			next.extend(i, j, ed);
            			if (this.m.runsSuccessfully(next)) {
            				this.store(next);
            				return next;
            			}
            		}
            	}
            }
        }
    	return null;
    }
    
    public Pattern extendCycle(Pattern p) {
    	int lhe = p.getLEndpoint();
    	int rhe = p.sizeOfCurrentSheet();
    	
    	for (int i = lhe; i < rhe; i++) {
            for (int j = lhe + 1; j <= rhe; j++) {
            	for (int t = 0; t < hbond_types.length ; t++) {
            		Edge ed = hbond_types[t];

            		if (p.canCyclise(i, j, ed)) {
            			Pattern next = new Pattern(p);
            			next.extend(i, j, ed);
            			if (this.m.runsSuccessfully(next)) {
            				this.store(next);
            				return next;
            			}
            		}
                }
            }
        }
    	return null;
    }
  
    public Pattern doChirals(Pattern p) throws TopsStringFormatException {
        int end = p.getCTermPosition();
        for (int i = 1; i < end - 2; i++) {
        	int lastPos = Math.min(end, i + 7);
            for (int j = i + 2; j < lastPos; j++) {
            	char vleft = p.getVertex(i).getType();
            	if (vleft == p.getVertex(j).getType()) {
            		for (int t = 0; t < chiral_types.length; t++) {
            			char ltyp = chiral_types[t].getLType();
            			if (vleft == ltyp) {
            				Pattern next = new Pattern(p);
            				if (next.addChiral(i, j, chiral_types[t].getType())) {
            					if (m.runChiral(next)) {
            						break;
            					} else {
            						next.removeLastChiral();
            					}
            				}
            			}
            		}
            	}
            }
        }
        return p;
    }

}


