package eu.isygoit.helper;

import eu.isygoit.helper.bo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The type Xml helper test part 1.
 */
class XmlHelperTest {

    /**
     * The constant BOOKSTORE_XML.
     */
    public static final String BOOKSTORE_XML = "Bookstore.xml";
    /**
     * The constant BOOKSTORE_XSD.
     */
    public static final String BOOKSTORE_XSD = "Bookstore.xsd";
    /**
     * The Temp dir.
     */
    @TempDir
    Path tempDir;
    private File xmlFile;
    private File xsdFile;

    /**
     * Sets up.
     *
     * @throws Exception the exception
     */
    @BeforeEach
    void setUp() throws Exception {
        // Copy test files from resources to temp directory
        xmlFile = tempDir.resolve(BOOKSTORE_XML).toFile();
        xsdFile = tempDir.resolve(BOOKSTORE_XSD).toFile();

        copyResourceToFile("/xml/" + BOOKSTORE_XML, xmlFile);
        copyResourceToFile("/xml/" + BOOKSTORE_XSD, xsdFile);
    }

    private void copyResourceToFile(String resourcePath, File destinationFile) throws IOException {
        try (var inputStream = getClass().getResourceAsStream(resourcePath)) {
            assertNotNull(inputStream, "Resource not found: " + resourcePath);
            Files.copy(inputStream, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * The type Xml to object conversion tests.
     */
    @Nested
    @DisplayName("XML to Object Conversion Tests")
    class XmlToObjectConversionTests {

        /**
         * Fail to convert malformed xml string.
         */
        @Test
        @DisplayName("Should fail to convert malformed XML string to object")
        void failToConvertMalformedXmlString() {
            String invalidXml = "<bookstore><book><title>Malformed";
            assertThrows(Exception.class, () -> XmlHelper.convertXmlToObject(invalidXml, Bookstore.class));
        }

        /**
         * Convert xml file to bookstore success.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should convert XML file to Bookstore object")
        void convertXmlFileToBookstoreSuccess() throws Exception {
            Bookstore bookstore = XmlHelper.convertXmlFileToObject(
                    Files.newInputStream(xmlFile.toPath()),
                    Bookstore.class
            );

            assertNotNull(bookstore);
            assertFalse(bookstore.getBooks().isEmpty());
            assertEquals(2, bookstore.getBooks().size());

            Book firstBook = bookstore.getBooks().get(0);
            assertEquals("B001", firstBook.getId());
            assertEquals("The Great Gatsby", firstBook.getTitle());
            assertEquals("fiction", firstBook.getCategory());
        }

        /**
         * Convert xml string to bookstore success.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should convert XML string to Bookstore object")
        void convertXmlStringToBookstoreSuccess() throws Exception {
            String xmlContent = Files.readString(xmlFile.toPath());

            Bookstore bookstore = XmlHelper.convertXmlToObject(xmlContent, Bookstore.class);

            assertNotNull(bookstore);
            assertEquals(2, bookstore.getBooks().size());
            assertEquals("A Brief History of Time", bookstore.getBooks().get(1).getTitle());
        }
    }

    /**
     * The type Object to xml conversion tests.
     */
    @Nested
    @DisplayName("Object to XML Conversion Tests")
    class ObjectToXmlConversionTests {

        /**
         * Convert bookstore to xml string success.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should convert Bookstore object to XML string")
        void convertBookstoreToXmlStringSuccess() throws Exception {
            Bookstore bookstore = createSampleBookstore();

            String xml = XmlHelper.convertObjectToXmlString(bookstore, Bookstore.class);

            assertNotNull(xml);
            assertTrue(xml.contains("<title>Test Book</title>"));
            assertTrue(xml.contains("currency=\"USD\""));
        }

        /**
         * Convert bookstore to xml file success.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should convert Bookstore object to XML file")
        void convertBookstoreToXmlFileSuccess() throws Exception {
            Bookstore bookstore = createSampleBookstore();
            File outputFile = tempDir.resolve("output.xml").toFile();

            File result = XmlHelper.convertObjectToXmlFile(outputFile, bookstore, Bookstore.class);

            assertTrue(result.exists());
            String content = Files.readString(result.toPath());
            assertTrue(content.contains("<title>Test Book</title>"));
        }

        private Bookstore createSampleBookstore() {
            Bookstore bookstore = new Bookstore();
            Book book = new Book();
            book.setId("TEST001");
            book.setCategory("fiction");
            book.setTitle("Test Book");

            Author author = new Author();
            author.setFirstName("John");
            author.setLastName("Doe");
            book.setAuthor(author);

            Price price = new Price();
            price.setValue(new BigDecimal("29.99"));
            price.setCurrency("USD");
            book.setPrice(price);

            book.setPublicationYear(2024);
            book.setDescription("Test description");

            Reviews reviews = new Reviews();
            Review review = new Review();
            review.setRating(5);
            review.setReviewer("Test Reviewer");
            review.setComment("Great book!");
            review.setDate(new Date());
            reviews.getReviewList().add(review);
            book.setReviews(reviews);

            bookstore.getBooks().add(book);
            return bookstore;
        }
    }

    /**
     * The type Xml validation tests.
     */
    @Nested
    @DisplayName("XML Validation Tests")
    class XmlValidationTests {

        /**
         * Validate xml with missing elements failure.
         */
        @Test
        @DisplayName("Should fail validation for missing required elements")
        void validateXmlWithMissingElementsFailure() {
            String invalidXml = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <bookstore>
                        <book>
                            <title>Missing Required Fields</title>
                        </book>
                    </bookstore>
                    """;

            assertThrows(org.xml.sax.SAXException.class, () ->
                    XmlHelper.validateXmlContent(invalidXml, xsdFile, "http://www.w3.org/2001/XMLSchema")
            );
        }

        /**
         * Validate valid xml success.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should validate valid XML against schema")
        void validateValidXmlSuccess() throws Exception {
            String xmlContent = Files.readString(xmlFile.toPath());

            boolean isValid = XmlHelper.validateXmlContent(
                    xmlContent,
                    xsdFile,
                    "http://www.w3.org/2001/XMLSchema"
            );

            assertTrue(isValid);
        }

        /**
         * Validate invalid xml failure.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should fail validation for invalid XML")
        void validateInvalidXmlFailure() throws Exception {
            String invalidXml = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <bookstore>
                        <book>
                            <title>Invalid Book</title>
                        </book>
                    </bookstore>
                    """;

            assertThrows(org.xml.sax.SAXException.class, () ->
                    XmlHelper.validateXmlContent(
                            invalidXml,
                            xsdFile,
                            "http://www.w3.org/2001/XMLSchema"
                    )
            );
        }
    }

    /**
     * The type Xml element manipulation tests.
     */
    @Nested
    @DisplayName("XML Element Manipulation Tests")
    class XmlElementManipulationTests {

        /**
         * Add multiple book elements success.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should add multiple book elements at once")
        void addMultipleBookElementsSuccess() throws Exception {
            XmlHelper.addElementToXmlFile(xmlFile, "book", """
                    <book id="B004" category="science">
                        <title>Physics Basics</title>
                        <author>
                            <firstName>Albert</firstName>
                            <lastName>Einstein</lastName>
                        </author>
                    </book>
                    """
            );
            XmlHelper.addElementToXmlFile(xmlFile, "book", """
                    <book id="B005" category="history">
                        <title>World History</title>
                        <author>
                            <firstName>Jane</firstName>
                            <lastName>Doe</lastName>
                        </author>
                    </book>
                    """
            );

            Document doc = XmlHelper.getDocumentFromFile(xmlFile);
            NodeList books = doc.getElementsByTagName("book");
            assertEquals(4, books.getLength());
        }

        /**
         * Add new book element success.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should add new book element")
        void addNewBookElementSuccess() throws Exception {
            XmlHelper.addElementToXmlFile(xmlFile, "book", """
                    <book id="B003" category="reference">
                        <title>New Book</title>
                        <author>
                            <firstName>New</firstName>
                            <lastName>Author</lastName>
                        </author>
                        <publicationYear>2024</publicationYear>
                        <price currency="USD">29.99</price>
                        <description>New book description</description>
                        <reviews/>
                    </book>
                    """);

            Document doc = XmlHelper.getDocumentFromFile(xmlFile);
            NodeList books = doc.getElementsByTagName("book");
            assertEquals(3, books.getLength());
        }

        /**
         * Update book title success.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should update book title")
        void updateBookTitleSuccess() throws Exception {
            String newTitle = "Updated Title";
            XmlHelper.updateElementInXmlFile(xmlFile, "title", newTitle);

            Optional<String> result = XmlHelper.getElementValueFromXmlFile(xmlFile, "title");
            assertTrue(result.isPresent());
            assertEquals(newTitle, result.get());
        }

        /**
         * Delete review element success.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should delete review element")
        void deleteReviewElementSuccess() throws Exception {
            Document originalDoc = XmlHelper.getDocumentFromFile(xmlFile);
            int originalReviewCount = originalDoc.getElementsByTagName("review").getLength();

            XmlHelper.deleteElementFromXmlFile(xmlFile, "review");

            Document updatedDoc = XmlHelper.getDocumentFromFile(xmlFile);
            int newReviewCount = updatedDoc.getElementsByTagName("review").getLength();
            assertEquals(originalReviewCount - 1, newReviewCount);
        }
    }

    /**
     * The type Xml attribute manipulation tests.
     */
    @Nested
    @DisplayName("XML Attribute Manipulation Tests")
    class XmlAttributeManipulationTests {

        /**
         * Add multiple attributes to book success.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should add multiple attributes to book element")
        void addMultipleAttributesToBookSuccess() throws Exception {
            XmlHelper.addAttributeToElementInXml(xmlFile, "book", "language", "en");
            XmlHelper.addAttributeToElementInXml(xmlFile, "book", "edition", "2nd");

            Optional<String> langAttr = XmlHelper.getElementAttributeFromXml(xmlFile, "book", "language");
            Optional<String> editionAttr = XmlHelper.getElementAttributeFromXml(xmlFile, "book", "edition");

            assertEquals("en", langAttr.get());
            assertEquals("2nd", editionAttr.get());
        }

        /**
         * Add attribute to book success.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should add new attribute to book element")
        void addAttributeToBookSuccess() throws Exception {
            XmlHelper.addAttributeToElementInXml(xmlFile, "book", "language", "en");

            Optional<String> result = XmlHelper.getElementAttributeFromXml(xmlFile, "book", "language");
            assertTrue(result.isPresent());
            assertEquals("en", result.get());
        }

        /**
         * Update book category success.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should update book category attribute")
        void updateBookCategorySuccess() throws Exception {
            XmlHelper.updateElementAttributeInXml(xmlFile, "book", "category", "reference");

            Optional<String> result = XmlHelper.getElementAttributeFromXml(xmlFile, "book", "category");
            assertTrue(result.isPresent());
            assertEquals("reference", result.get());
        }

        /**
         * Remove attribute from book success.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should remove attribute from book element")
        void removeAttributeFromBookSuccess() throws Exception {
            XmlHelper.addAttributeToElementInXml(xmlFile, "book", "testAttr", "test");
            XmlHelper.removeAttributeFromElementInXml(xmlFile, "book", "testAttr");

            Optional<String> result = XmlHelper.getElementAttributeFromXml(xmlFile, "book", "testAttr");
            assertTrue(result.isEmpty());
        }
    }

    /**
     * The type Xml structure modification tests.
     */
    @Nested
    @DisplayName("XML Structure Modification Tests")
    class XmlStructureModificationTests {

        /**
         * Move review element success.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should move review element within reviews")
        void moveReviewElementSuccess() throws Exception {
            Optional<String> firstReviewerBefore = XmlHelper.getElementValueFromXmlFile(xmlFile, "review");

            XmlHelper.moveElementWithinXml(xmlFile, "date", "reviewer");

            Optional<String> firstReviewerAfter = XmlHelper.getElementValueFromXmlFile(xmlFile, "review");
            assertNotEquals(firstReviewerBefore.get(), firstReviewerAfter.get());
        }

        /**
         * Rename element success.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should rename description element to summary")
        void renameElementSuccess() throws Exception {
            String originalDescription = XmlHelper.getElementValueFromXmlFile(xmlFile, "description")
                    .orElseThrow();

            XmlHelper.renameElementTagInXml(xmlFile, "description", "summary");

            Optional<String> newSummary = XmlHelper.getElementValueFromXmlFile(xmlFile, "summary");
            assertTrue(newSummary.isPresent());
            assertEquals(originalDescription, newSummary.get());
        }

        /**
         * Clone book element success.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should clone book element")
        void cloneBookElementSuccess() throws Exception {
            Document originalDoc = XmlHelper.getDocumentFromFile(xmlFile);
            int originalBookCount = originalDoc.getElementsByTagName("book").getLength();

            XmlHelper.cloneElementInXml(xmlFile, "book");

            Document updatedDoc = XmlHelper.getDocumentFromFile(xmlFile);
            assertEquals(originalBookCount + 1, updatedDoc.getElementsByTagName("book").getLength());
        }
    }
}