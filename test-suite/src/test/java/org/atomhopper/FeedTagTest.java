package org.atomhopper;

import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Source;
import org.apache.abdera.protocol.client.AbderaClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.util.Date;

import static org.atomhopper.util.TestHelper.assertNotEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;


/**
 * User: sbrayman
 * Date: 10/24/11
 */

@RunWith(Enclosed.class)
public class FeedTagTest extends JettyIntegrationTestHarness {

    public static class WhenCreatingAnEntry {

        private static final XmlUtil xml = new XmlUtil();
        private Entry entry;
        private org.w3c.dom.Document doc;
        private XPath xPath = XPathFactory.newInstance().newXPath();
        Date date = new Date();


        @Before
        public void setUp() throws Exception {
            Abdera abdera = new Abdera();
            AbderaClient abderaClient = new AbderaClient(abdera);
            Factory factory = abdera.getFactory();
            entry = factory.newEntry();
            Source source = abdera.getFactory().newSource();


            /* Constructing the atom entry. */

            //Author elements have sub-elements.
            entry.addAuthor("Entry Author 1 of 2");
            entry.addAuthor("Entry Author 2 of 2");
            //entry.getAuthors().get(0).setName("Author 1 name."); //Removed to allow automatic name tag creation.
            entry.getAuthors().get(0).setEmail("Author 1 email.");
            entry.getAuthors().get(0).setUri("http://author.uri/1");
            entry.getAuthors().get(1).setName("Author 2 name.");
            entry.getAuthors().get(1).setEmail("Author 2 email.");
            entry.getAuthors().get(1).setUri("http://author.uri/2");

            //Category elements have sub-elements.
            entry.addCategory("Entry category 1 of 2.");
            entry.addCategory("Entry category 2 of 2.");
            entry.getCategories().get(0).setTerm("Entry category 1 term.");
            entry.getCategories().get(0).setScheme("Category_Scheme_1");
            entry.getCategories().get(0).setLabel("Entry category 1 label");
            entry.getCategories().get(1).setTerm("Entry category 2 term.");
            entry.getCategories().get(1).setScheme("Category_Scheme_2");
            entry.getCategories().get(1).setLabel("Entry category 2 label.");

            entry.setContent("Entry content, whooo hoooo!");

            //Contributor elements have sub-elements.
            entry.addContributor("Entry contributor 1 of 2.");
            entry.addContributor("Entry contributor 2 of 2.");
            //entry.getContributors().get(0).setName("Contributor 1 name."); //Removed to allow automatic name tag creation.
            entry.getContributors().get(0).setEmail("Contributor 1 email.");
            entry.getContributors().get(0).setUri("http://contributor.uri/1");
            entry.getContributors().get(1).setName("Contributor 2 name.");
            entry.getContributors().get(1).setEmail("Contributor 2 email.");
            entry.getContributors().get(1).setUri("http://contributor.uri/2");

            //entry.setId("Entry_ID"); //ID is set by AH, not but the publisher.
            entry.addLink("http://entry.link/1");
            entry.addLink("http://entry.link/2");
            entry.setPublished(date);
            entry.setRights("Entry copyright info.");

            //Source elements has sub-elements.
            entry.setSource(source);

            //Source author element has sub-elements.
            entry.getSource().addAuthor("Source author 1 of 2.");
            entry.getSource().addAuthor("Source author 1 of 2.");
            entry.getSource().getAuthors().get(0).setName("Source author 1 name.");
            entry.getSource().getAuthors().get(0).setEmail("Source author 1 email.");
            entry.getSource().getAuthors().get(0).setUri("http://source.author.uri/1");
            entry.getSource().getAuthors().get(1).setName("Source author 2 name.");
            entry.getSource().getAuthors().get(1).setEmail("Source author 2 email.");
            entry.getSource().getAuthors().get(1).setUri("http://source.author.uri/2");

            //Source category element has sub-elements.
            entry.getSource().addCategory("Entry category 1 of 2.");
            entry.getSource().addCategory("Entry category 2 of 2.");
            entry.getSource().getCategories().get(0).setTerm("Source category 1 term.");
            entry.getSource().getCategories().get(0).setScheme("Source_Category_Scheme1");
            entry.getSource().getCategories().get(0).setLabel("Source category 1 label");
            entry.getSource().getCategories().get(1).setTerm("Source category 2 term.");
            entry.getSource().getCategories().get(1).setScheme("Source_Category_Scheme2");
            entry.getSource().getCategories().get(1).setLabel("Source category 2 label.");

            entry.getSource().addContributor("Source contributor 1 of 2.");
            entry.getSource().addContributor("Source contributor 2 of 2.");
            entry.getSource().setGenerator("Generator_IRI.", "Generator_version.", "Generator_value.");
            entry.getSource().setIcon("Source_icon_IRI.");
            entry.getSource().setId("Source_ID.");
            entry.getSource().addLink("Source_link_1_of_2");
            entry.getSource().addLink("Source_link_2_of_2");
            entry.getSource().setRights("Source rights.");
            entry.getSource().setSubtitle("Source subtitle.");
            entry.getSource().setTitle("Source Title.");
            entry.getSource().setUpdated(date);

            entry.setSummary("Entry summary.");
            entry.setTitle("Entry title - entryWithComplexAuthorAndContributor.");
            //entry.setUpdated(date); //This needs to be auto-generated.

            report("The Entry to Post", entry.toString());
            String postResponse = abderaClient.post("http://localhost:" + getPort() + "/namespace/feed/", entry).getDocument().getRoot().toString();
            doc = xml.toDOM(postResponse);
            report("The Created Entry", postResponse);
        }

        @Test
        public void shouldReturnAuthorInfo() throws Exception {
            String s = xPath.evaluate("/entry/author[1]/name", doc);
            assertEquals("First author should return name:", "Entry Author 1 of 2", s);
            s = xPath.evaluate("/entry/author[2]/name", doc);
            assertEquals("Second author should return name:", "Author 2 name.", s);
            s = xPath.evaluate("/entry/author[2]/email", doc);
            assertEquals("Second author should return email:", "Author 2 email.", s);
            s = xPath.evaluate("/entry/author[2]/uri", doc);
            assertEquals("Second author should return URL:", "http://author.uri/2", s);
        }

        @Test
        public void shouldReturnCategoryInfo() throws Exception {
            String s = xPath.evaluate("/entry/category[1]/@term", doc);
            assertEquals("Category 1 should have a term attribute:", "Entry category 1 term.", s);
            s = xPath.evaluate("/entry/category[2]/@scheme", doc);
            assertEquals("Category 2 should have a scheme attribute:", "Category_Scheme_2", s);
            s = xPath.evaluate("/entry/category[2]/@label", doc);
            assertEquals("Category 2 should have a label attribute:", "Entry category 2 label.", s);
        }

        @Test
        public void shouldReturnContent() throws Exception {
            String s = xPath.evaluate("/entry/content", doc);
            assertFalse("Content should return data.", s.isEmpty());
        }

        @Test
        public void shouldReturnContributorInfo() throws Exception {
            String s = xPath.evaluate("/entry/contributor[1]/name", doc);
            assertEquals("First author should return name:", "Entry contributor 1 of 2.", s);
            s = xPath.evaluate("/entry/contributor[2]/name", doc);
            assertEquals("Second author should return name:", "Contributor 2 name.", s);
            s = xPath.evaluate("/entry/contributor[2]/email", doc);
            assertEquals("Second author should return email:", "Contributor 2 email.", s);
            s = xPath.evaluate("/entry/contributor[2]/uri", doc);
            assertEquals("Second author should return URL:", "http://contributor.uri/2", s);
        }

        @Test
        public void shouldReturnID() throws Exception {
            String s = xPath.evaluate("/entry/id", doc);
            assertFalse("ID should be present", s.isEmpty());
        }

        @Test
        public void shouldReturnLinks() throws Exception {
            String s = xPath.evaluate("/entry/link[1]/@href", doc);
            assertEquals("Link tag 1 href attribute should return:", "http://entry.link/1", s);
        }

        @Test
        public void shouldReturnPublished() throws Exception {
            String s = xPath.evaluate("/entry/published", doc);
            assertFalse("ID tag should not be empty.", s.isEmpty());
        }

        @Test
        public void shouldReturnRights() throws Exception {
            String s = xPath.evaluate("/entry/rights", doc);
            assertEquals("Should return entry copyright info.", "Entry copyright info.", s);
        }

        @Test
        public void shouldReturnSource() throws Exception {
            String s = xPath.evaluate("/entry/source", doc);
            assertFalse("Source tag should not be empty.", s.isEmpty());
        }

        @Test
        public void shouldReturnSourceCategoryInfo() throws Exception {
            String s = xPath.evaluate("/entry/source/category[1]/@term", doc);
            assertEquals("Source category 1 should have a term attribute:", "Source category 1 term.", s);
            s = xPath.evaluate("/entry/source/category[2]/@scheme", doc);
            assertEquals("Source category 2 should have a scheme attribute:", "Source_Category_Scheme2", s);
            s = xPath.evaluate("/entry/source/category[2]/@label", doc);
            assertEquals("Source category 2 should have a label attribute:", "Source category 2 label.", s);
        }

        @Test
        public void shouldReturnSourceContributorInfo() throws Exception {
            String s = xPath.evaluate("/entry/source/contributor[1]", doc);
            assertEquals("Source author 1 should return name:", "Source contributor 1 of 2.", s);
            s = xPath.evaluate("/entry/source/contributor[2]", doc);
            assertEquals("Source author 2 should return name:", "Source contributor 2 of 2.", s);
        }


        @Test
        public void shouldReturnSourceGenerator() throws Exception {
            String s = xPath.evaluate("/entry/source/generator", doc);
            assertEquals("Source generator should return:", "Generator_value.", s);
        }

        @Test
        public void shouldReturnSourceIcon() throws Exception {
            String s = xPath.evaluate("/entry/source/icon", doc);
            assertEquals("Source icon should return:", "Source_icon_IRI.", s);
        }

        @Test
        public void shouldReturnSourceID() throws Exception {
            String s = xPath.evaluate("/entry/source/id", doc);
            assertFalse("Source ID should be present", s.isEmpty());
        }

        @Test
        public void shouldReturnSourceLink() throws Exception {
            String s = xPath.evaluate("/entry/source/link[1]/@href", doc);
            assertEquals("Link tag 1 href attribute should return:", "Source_link_1_of_2", s);
            s = xPath.evaluate("/entry/source/link[2]/@href", doc);
            assertEquals("Link tag 2 href attribute should return:", "Source_link_2_of_2", s);
        }

        @Test
        public void shouldReturnSourceRights() throws Exception {
            String s = xPath.evaluate("/entry/source/rights", doc);
            assertEquals("Should return source copyright info.", "Source rights.", s);
        }

        @Test
        public void shouldReturnSourceSubtitle() throws Exception {
            String s = xPath.evaluate("/entry/source/subtitle", doc);
            assertEquals("Source subtitle should return:", "Source subtitle.", s);
        }

        @Test
        public void shouldReturnSourceTitle() throws Exception {
            String s = xPath.evaluate("/entry/source/title", doc);
            assertEquals("Source title should return:", "Source Title.", s);
        }

        @Test
        public void shouldReturnSourceUpdated() throws Exception {
            String s = xPath.evaluate("/entry/source/updated", doc);
            assertFalse("Source updated date should not be empty.", s.isEmpty());
        }

        @Test
        public void shouldReturnSourceExtension() throws Exception {
        }

        @Test
        public void shouldReturnSummary() throws Exception {
            String s = xPath.evaluate("/entry/summary", doc);
            assertEquals("Summary should return:", "Entry summary.", s);
        }

        @Test
        public void shouldReturnTitle() throws Exception {
            String s = xPath.evaluate("/entry/title", doc);
            assertEquals("Title should return:", "Entry title - entryWithComplexAuthorAndContributor.", s);
        }

        @Test
        public void shouldReturnUpdated() throws Exception {
            String s = xPath.evaluate("/entry/source/updated", doc);
            assertFalse("Updated date should be present.", s.isEmpty());
        }
    }

    private static void report(String title, String message) {
        System.out.println("== " + title + " ==");
        if (message != null) {
            System.out.println(message);
        }
        System.out.println();
    }
}
