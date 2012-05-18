
package org.inria.acqua.mjmisc.parser;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

/**
 * Class that deals with XML concepts. Uses XPATH for queries.
 */
public class XMLParser {
    private Document documentDOM;
    private XPathFactory xpathFactory;
    private String filename; 


    public XMLParser(String base, String nul) throws Exception
    {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        documentDOM = builder.newDocument();
        xpathFactory = XPathFactory.newInstance();

        Document doc = this.documentDOM;

        // Insert the root element node
        Element element = doc.createElement(base);
        doc.appendChild(element);
    }


    public XMLParser(String filename) throws Exception
    {
        this.filename = filename;
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        documentDOM = builder.parse(filename);
        xpathFactory = XPathFactory.newInstance();
    }

    public XMLParser(InputStream is) throws Exception
    {
        filename = null;
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        documentDOM = builder.parse(is);
        xpathFactory = XPathFactory.newInstance();
    }

    public void setFilename(String str){
        filename = str;
    }

    /** Query all coincidences using XPATH in the current open document. */
    public NodeList queryAllAnswers(String query) throws Exception{
        XPathExpression expr = xpathFactory.newXPath().compile(query);
        NodeList nodes = (NodeList) expr.evaluate(documentDOM, XPathConstants.NODESET);
        return nodes;
    }

    /** Query once coincidence using XPATH in the current open document. */
    public Node queryOneAnswer(String query) throws Exception{
        Node res = null;
        NodeList nodes = this.queryAllAnswers(query);
        if (nodes.getLength()>0){
            res = nodes.item(0);
        }
        return res;
    }

    public void addNode(String name, String data){

        Document doc = this.documentDOM;

        Element element2 = doc.createElement(name);
        doc.getDocumentElement().appendChild(element2);//insertBefore(element2, element.getFirstChild().getNextSibling());

        element2.appendChild(doc.createTextNode(data));

    }

    /** Write the XML according to the DOM model. */
    public void writeXML() {
        try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(documentDOM);

            // Prepare the output file
            File file = new File(filename);
            Result result = new StreamResult(file);

            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Write the XML according to the DOM model. */
    public void writeXML(OutputStream os) {
        try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(documentDOM);

            // Prepare the output file
            Result result = new StreamResult(os);

            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Testing. */
    public static void main(String[] args) throws Exception{
        XMLParser xmlp = new XMLParser("config.xml");
        Node node = xmlp.queryOneAnswer(
                    "/ACQUA_CONFIG_FILE/significance_level/@value"
                );
        String res = node.getNodeValue();
        System.out.println(res);
        node.setNodeValue("995");
        xmlp.writeXML();
        
    }

}
