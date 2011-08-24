package org.atomhopper;

import static junit.framework.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.meterware.httpunit.HttpUnitUtils;
import org.xml.sax.SAXException;

public class XmlUtil {
    private JAXBContext context;
    private XPath xPath;

    public XmlUtil() {
        xPath = XPathFactory.newInstance().newXPath();
    }

    public Document toDOM(String input) throws SAXException {
      try {
          return HttpUnitUtils.parse( new InputSource( new StringReader( input ) ) );
      } catch (IOException e) {
          throw new SAXException( e );
      }
    }

    public void assertHasValue(final String xmlString,final  String xPathExpression, final String value) throws Exception {
        final Document document = HttpUnitUtils.parse(new InputSource(new StringReader(xmlString)));
        this.assertHasValue(document, xPathExpression, value);
    }

    public Map<String, String> getAttributes(final Document document, final String xPathExpression) throws XPathExpressionException {
        final Node node = (Node) xPath.evaluate(xPathExpression, document, XPathConstants.NODE);

        final Map<String, String> map = new HashMap<String, String>();
        final NamedNodeMap nodeMap = node.getAttributes();
        for (int i = 0; i < nodeMap.getLength(); i++) {
            map.put(nodeMap.item(i).getNodeName(), nodeMap.item(i).getNodeValue());
        }
        return map;
    }

    public void assertXPathNumber(final Document document, final String xPathExpression, final Double value) throws Exception {
        final Double number = (Double) this.xPath.evaluate(xPathExpression, document, XPathConstants.NUMBER);

        if (number.equals(value)) {
            return;
        }

        fail(MessageFormat.format("Document does not have a node with value: {0} at {1}, actual: {2}", value,
                xPathExpression, number));
    }

    public void assertXPathNode(final Document document, final String xPathExpression, final String value) throws Exception {
        final Node node = (Node) this.xPath.evaluate(xPathExpression, document, XPathConstants.NODE);

        if (value.equals(node.getNodeValue())) {
            return;
        }

        fail(MessageFormat.format("Document does not have a node with value:  {0} at {1}, values: {2}", value,
                xPathExpression, node.getNodeValue()));
    }

    public void assertHasValue(final Document document, final String xPathExpression, final String value) throws Exception {
        final NodeList nodeList = (NodeList) this.xPath.evaluate(xPathExpression, document, XPathConstants.NODESET);

        // verify the node values
        for (int i = 0; i < nodeList.getLength(); i++) {
            final String nodeValue = nodeList.item(i).getNodeValue();

            if (value.equals(nodeValue)) {
                return;
            }
        }

        // assertion failed, so build a set of values for the response.
        final List<String> values = new ArrayList<String>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            values.add(nodeList.item(i).getNodeValue());
        }

        fail(MessageFormat.format("Document does not have a node with value: {0} at {1}, values: {2}", value,
                xPathExpression, values));
    }

}
