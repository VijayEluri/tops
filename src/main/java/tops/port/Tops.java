package tops.port;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tops.port.model.Cartoon;
import tops.port.model.DsspReader;
import tops.port.model.PlotFragInformation;
import tops.port.model.Protein;
import tops.port.model.SSE;

/*
	Program:         TOPS
	Description: Program to automatically generate protein topology cartoons
	Version:         3
	Author: T. Flores (original) with further development by D. Westhead (EBI), 
	ported to Java by Gilleain Torrance.
*/

public class Tops {
    
    private double ProgramVersion = 2.0;
    
    private SSE Root = null; /* Pointer to link list of structures */ // XXX TODO
    private String defaultsFile = "tops.def";
    private String TOPS_HOME = null;
    

    /* --------------------- */
    /* Main control function */
    /* --------------------- */

    public static void main(String[] args) throws IOException {
        new Tops().run(args);
    }

    private void log(String string, Object... args) {
        System.out.println(String.format(string, args));
    }
    
    /* Parse defaults file */
    private void readDefaults(Options options, OptimiseOptions optimiseOptions) throws IOException {
        File defsFile = null;
        BufferedReader reader = null;
        try {
            if (TOPS_HOME != null) {
                defaultsFile = TOPS_HOME + "/" + defaultsFile;
                defsFile = new File(defaultsFile);
            }
            if (!defsFile.canRead()) {
                log("Cannot read file%s\n", defaultsFile);
                return;
            }
            reader = new BufferedReader(new FileReader(defsFile));
            options.readDefaults(reader);
            // XXX different files!
            optimiseOptions.readDefaults(reader);
        } catch (Exception e) {
            log("Unable to open defaults file %s\n", defaultsFile);
            return;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public void run(String[] args) throws IOException {
        String proteinCode;

        /* get necessary env. vars. */
        TOPS_HOME = System.getenv("TOPS_HOME");
        Options options = new Options();
        OptimiseOptions optimiseOptions = new OptimiseOptions();

        readDefaults(options, optimiseOptions);

        /* Parse command line arguments */
        proteinCode = options.parseArguments(args);
        optimiseOptions.parseArguments(args);
        
        if (proteinCode == null) {
            log("Tops error: No protein specified on command line\n");
            System.exit(1);
        }
        if (proteinCode.length() != 4) {
            log("Tops error: Protein code %s must be exactly 4 characters\n", proteinCode);
            System.exit(1);
        }

        /* check runtime options are reasonable */
        try {
            options.checkOptions();
            optimiseOptions.checkOptions();
        } catch (Exception e) {
            log("ERROR: checking runtime options\n");
            System.exit(1);
        }

        if (options.isVerbose()) {
            System.out.println("Starting TOPS\n");
            System.out.println(
                    "Copyright � 1996 by European Bioinformatics Institute, Cambridge, UK.\n\n");
            options.printRunParams(System.out);
        }

        /* set global variables which are derived from inputs */
        optimiseOptions.setGridUnitSize(options.getRadius());

        /* call main driver */
        runTops(proteinCode, options);
        if (options.isVerbose()) {
            System.out.println("TOPS completed successfully\n");
        }
    }

    public void runTops(String proteinCode, Options options) throws IOException {
        Protein protein = null;

        /* Read secondary structure file ( DSSP or STRIDE ) or old save file */
        if (options.getFileType().equals("dssp")) {

            if (options.isVerbose())
                System.out.println("Reading dssp file\n");

            String dsspFile = new File(options.getDsspFilePath(), options.getDSSPFileName(proteinCode)).getAbsolutePath();
            protein = new DsspReader().readDsspFile(dsspFile);
            if (protein == null) {
                log("Tops error: processing DSSP information\n");
                return;
            }
        } else if (options.getFileType().equals("stride")) { // TODO
//
//            if (options.isVerbose())
//                System.out.println("Reading pdb file\n");
//
//            protein = ReadPDBFile(new File(PDBFilePath, GetPDBFileName(Pcode)));
//            if (protein != null) {
//                log("Tops error: reading pdbfile %s\n", GetPDBFileName(Pcode));
//            }
//            RemoveACEResidues(protein);
//
//            if (options.isVerbose())
//                System.out.println("Reading stride file\n");
//            if (!ReadSTRIDEFile(
//                    new File(STRIDEFilePath, GetSTRIDEFileName(Pcode)),
//                    protein)) {
//                log("Tops error: getting secondary structure from %s\n",
//                        GetSTRIDEFileName(Pcode));
//            }
//
//            protein.BridgePartFromHBonds();

        } else if (options.getFileType().equals("tops")) {
            log("Tops error: this bit not implemented yet\n");
        } else {
            log("Tops error: Unrecognized file type %s\n", options.getFileType());
        }

        /* Assign protein name and code from input file name */
        protein.setName(proteinCode); 

        /* Initialise the chirality code at this point */
        // InitialiseChirality( protein, Error ); // XXX TODO

        readDomainBoundaryFile(protein, options);
        
        /* add default domains */
        if (options.isVerbose()) {
            System.out.println("Setting default domains\n");
        }
        protein.defaultDomains(options.getChainToPlot().charAt(0));

        /* check domain definitions */
        Protein.DomDefError ddep  = protein.checkDomainDefs();
        if (ddep != null) {
            if (ddep.ErrorType != null) {
                log("Tops warning: problem with domain definitions type %d\n", ddep.ErrorType);
                log("%s\n", ddep.ErrorString);
            }
        }
        
        /*
         * information on plotted chain fragments is held in PlotFragInf for use
         * in output postscript
         */
        PlotFragInformation plotFragInf = new PlotFragInformation();

        /* set up the domain breaks and PlotFragInfo */
        if (options.isVerbose()) {
            System.out.println("Setting domain breaks and domains to plot\n");
        }
        protein.setDomBreaks(Root, plotFragInf);    // TODO - Root will be null here!! XXX

        List<Integer> domainsToPlot = protein.fixDomainsToPlot(options.getChainToPlot().charAt(0), options.getDomainToPlot());
        if (domainsToPlot.isEmpty()) {
            log("Tops error: fixing domains to plot\n");
            return;
        }

        /* Loop over domains to plot */
        List<Cartoon> cartoons = new ArrayList<Cartoon>();
        for (int domainToPlot : domainsToPlot) {

            if (options.isVerbose()) {
                log("\nPlotting domain %d\n", domainToPlot + 1);
            }

            /* Set the domain to plot */
            Cartoon cartoon = protein.setDomain(Root, protein.getDomain(domainToPlot));

            new Optimise().optimise(cartoon);
            cartoon.calculateConnections(options.getRadius());
            cartoons.add(cartoon);
        }

        /* temporary write out of TOPS file */
        if (options.isVerbose()) {
            System.out.println("\nWriting tops file\n");
        }
        
        String topsFilename;
        if (options.getTopsFilename() != null) {
            topsFilename = options.getTopsFilename();
        } else {
            topsFilename = options.getTOPSFileName(proteinCode, options.getChainToPlot(), options.getDomainToPlot());
        }
        
        new TopsFileWriter().writeTOPSFile(topsFilename, cartoons, protein, domainsToPlot);

        /* Create postscript files for each Cartoon */
        new PostscriptFileWriter().makePostscript(cartoons, protein, domainsToPlot.size(), plotFragInf, options);

        if (options.isVerbose()) {
            System.out.println("\n");
        }
    }
    
    private void readDomainBoundaryFile(Protein protein, Options options) throws IOException {
        String dbf = null;
        /*
         * Read the domain boundary file if ChainToPlot was not specified as ALL
         */
        if (options.getChainToPlot() != null && !options.getChainToPlot().equals("ALL")) {
            if (options.isVerbose())
                System.out.println("Reading domain boundary file\n");

            /*
             * first check if specified file exists, otherwise look for one in
             * TOPS_HOME
             */
            if (options.getDomBoundaryFile() != null
                    && new File(options.getDomBoundaryFile()).canRead()) {
                dbf = options.getDomBoundaryFile();
            } else if (TOPS_HOME != null) {
                dbf = TOPS_HOME + "/" + "DomainFile";

            }

            if (dbf != null) {
                new DomainBoundaryFileReader().readDomBoundaryFile(dbf, protein);
            } else {
                log("TOPS warning: no domain file was found\n");
            }
        }
    }
}
