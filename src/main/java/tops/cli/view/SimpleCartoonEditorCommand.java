package tops.cli.view;

import javax.swing.JFrame;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.web.display.applet.SimpleEditorApplet;

public class SimpleCartoonEditorCommand implements Command {

    @Override
    public String getDescription() {
        return "Run the (simple) cartoon editor"; // XXX simple?
    }

    @Override
    public void handle(String[] args) throws ParseException {
        SimpleEditorApplet applet = new SimpleEditorApplet();
        JFrame frame = new JFrame();
        frame.add(applet);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }
    
    

}
