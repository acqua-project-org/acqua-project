package parsers;

import mjmisc.Misc;


/**
 * Parser base.
 * It is supposed to be inherited.
 */
public abstract class Parser {
    
    protected String rawFileContent;

    /** Open a file and give its content in a String. */
    public String readFile(String filename) throws Exception{
        rawFileContent = Misc.readAllFile(filename);
        return rawFileContent;
    }

    /** Write a file with the current raw content. */
    public void writeFile(String filename) throws Exception{
        Misc.writeAllFile(filename, rawFileContent);
    }


}
