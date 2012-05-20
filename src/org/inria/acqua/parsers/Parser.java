package org.inria.acqua.parsers;

import org.inria.acqua.mjmisc.Misc;


/**
 * Parsers' "template".
 * It is supposed to be inherited by any kind of configuration-file parser.
 * @author mjost
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
