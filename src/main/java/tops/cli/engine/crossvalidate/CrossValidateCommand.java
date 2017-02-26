package tops.cli.engine.crossvalidate;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.engine.crossvalidate.CrossValidator;
import tops.model.classification.Level;

public class CrossValidateCommand implements Command {

    @Override
    public String getDescription() {
       return "";   // TODO
    }

    @Override
    public void handle(String[] args) throws ParseException {
        String filename = args[0];
        CrossValidator.doSuperfolds(filename);
        
         String tLevelName = args[1];
         
         try { 
             Level tLevel = Level.fromFile(filename, Level.T, tLevelName);
             System.out.println(tLevel); 
             System.out.println("leave one out : ");
             CrossValidator.leaveOneOut(tLevel); 
             CrossValidator.nFold(2, tLevel); 
         } catch(java.io.IOException ioe) {
             System.err.println(ioe); 
         }
    }

}
