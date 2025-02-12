package eu.isygoit.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.bind.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.Optional;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.*;

/**
 * The interface XmlHelper provides utility methods to convert between XML and objects,
 * validate XML against a schema, and manipulate XML structures.
 */
public interface XmlHelper {

    Logger logger = LoggerFactory.getLogger(XmlHelper.class);

    /**
     * Converts an XML string to an object of type E.
     *
     * @param xmlContent the XML string to convert.
     * @param targetClass the class of the object to convert to.
     * @param <E> the type of object to return.
     * @return the object represented by the XML string.
     * @throws JAXBException if the conversion fails.
     */
    static <E> E convertXmlToObject(String xmlContent, Class<E> targetClass) throws JAXBException {
        logger.debug("Converting XML string to object of class {}", targetClass.getName());
        var jaxbContext = JAXBContext.newInstance(targetClass);
        var jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (E) jaxbUnmarshaller.unmarshal(new StringReader(xmlContent));
    }

    /**
     * Converts an XML file to an object of type E.
     *
     * @param xmlFile the XML file to convert.
     * @param targetClass the class of the object to convert to.
     * @param <E> the type of object to return.
     * @return the object represented by the XML file.
     * @throws JAXBException if the conversion fails.
     */
    static <E> E convertXmlFileToObject(InputStream xmlFile, Class<E> targetClass) throws JAXBException {
        logger.debug("Converting XML file to object of class {}", targetClass.getName());
        var jaxbContext = JAXBContext.newInstance(targetClass);
        var jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (E) jaxbUnmarshaller.unmarshal(xmlFile);
    }

    /**
     * Converts an object to its corresponding XML string representation.
     *
     * @param object the object to convert to XML.
     * @param targetClass the class of the object.
     * @param <E> the type of object.
     * @return the XML string representation of the object.
     * @throws JAXBException if the conversion fails.
     * @throws IOException if writing to the string fails.
     */
    static <E> String convertObjectToXmlString(E object, Class<E> targetClass) throws JAXBException, IOException {
        logger.debug("Converting object of class {} to XML string", targetClass.getName());
        var jaxbContext = JAXBContext.newInstance(targetClass);
        var jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        try (var stringWriter = new StringWriter()) {
            jaxbMarshaller.marshal(object, stringWriter);
            return stringWriter.toString();
        }
    }

    /**
     * Converts an object to an XML file.
     *
     * @param object the object to convert to XML.
     * @param targetClass the class of the object.
     * @param <E> the type of object.
     * @return the generated XML file.
     * @throws JAXBException if the conversion fails.
     */
    static <E> File convertObjectToXmlFile(E object, Class<E> targetClass) throws JAXBException {
        logger.debug("Converting object of class {} to XML file", targetClass.getName());
        var jaxbContext = JAXBContext.newInstance(targetClass);
        var jaxbMarshaller = jaxbContext.createMarshaller();
        var file = new File("output.xml");
        jaxbMarshaller.marshal(object, file);
        return file;
    }

    /**
     * Validates an XML string against the provided XSD schema.
     *
     * @param xmlContent the XML string to validate.
     * @param xsdPath the XSD file path.
     * @param schemaLanguage the schema language.
     * @return true if the XML is valid; false otherwise.
     * @throws IOException if reading the files fails.
     * @throws SAXException if validation fails.
     */
    static boolean validateXmlContent(String xmlContent, String xsdPath, String schemaLanguage) throws IOException, SAXException {
        logger.debug("Validating XML against schema at {}", xsdPath);
        return validateXmlWithSchema(xmlContent, ResourceUtils.getFile(xsdPath), schemaLanguage);
    }

    /**
     * Validates an XML string against the provided XSD schema.
     *
     * @param xmlContent the XML string to validate.
     * @param xsdFile the XSD file.
     * @param schemaLanguage the schema language.
     * @return true if the XML is valid; false otherwise.
     * @throws IOException if reading the files fails.
     * @throws SAXException if validation fails.
     */
    static boolean validateXmlContent(String xmlContent, File xsdFile, String schemaLanguage) throws IOException, SAXException {
        logger.debug("Validating XML against schema from file {}", xsdFile.getPath());
        return validateXmlWithSchema(xmlContent, xsdFile, schemaLanguage);
    }

    /**
     * Helper method to validate XML against the provided schema.
     *
     * @param xmlContent the XML string.
     * @param xsdFile the XSD file.
     * @param schemaLanguage the schema language.
     * @return true if the XML is valid.
     * @throws IOException if reading the files fails.
     * @throws SAXException if validation fails.
     */
    private static boolean validateXmlWithSchema(String xmlContent, File xsdFile, String schemaLanguage) throws IOException, SAXException {
        var schemaFactory = SchemaFactory.newInstance(schemaLanguage);
        var schema = schemaFactory.newSchema(xsdFile);
        var validator = schema.newValidator();
        var reader = new StringReader(xmlContent);
        validator.validate(new StreamSource(reader));
        return true;
    }

    /**
     * Adds a new element to the given XML file.
     *
     * @param xmlFile the XML file.
     * @param tagName the name of the tag.
     * @param tagValue the value of the tag.
     * @throws Exception if writing to the file fails.
     */
    static void addElementToXmlFile(File xmlFile, String tagName, String tagValue) throws Exception {
        logger.debug("Adding element with tagName {} and value {}", tagName, tagValue);
        var document = getDocumentFromFile(xmlFile);
        var newElement = document.createElement(tagName);
        newElement.appendChild(document.createTextNode(tagValue));
        document.getDocumentElement().appendChild(newElement);
        writeDocumentToXmlFile(document, xmlFile);
    }

    /**
     * Updates an existing element's value in the given XML file.
     *
     * @param xmlFile the XML file.
     * @param tagName the name of the tag.
     * @param newTagValue the new value of the tag.
     * @throws Exception if writing to the file fails.
     */
    static void updateElementInXmlFile(File xmlFile, String tagName, String newTagValue) throws Exception {
        logger.debug("Updating element with tagName {} to new value {}", tagName, newTagValue);
        var document = getDocumentFromFile(xmlFile);
        var nodes = document.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            var element = (Element) nodes.item(0);
            element.setTextContent(newTagValue);
            writeDocumentToXmlFile(document, xmlFile);
        } else {
            logger.warn("Element with tagName {} not found", tagName);
        }
    }

    /**
     * Deletes an element from the given XML file.
     *
     * @param xmlFile the XML file.
     * @param tagName the name of the tag to delete.
     * @throws Exception if writing to the file fails.
     */
    static void deleteElementFromXmlFile(File xmlFile, String tagName) throws Exception {
        logger.debug("Deleting element with tagName {}", tagName);
        var document = getDocumentFromFile(xmlFile);
        var nodes = document.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            var element = (Element) nodes.item(0);
            element.getParentNode().removeChild(element);
            writeDocumentToXmlFile(document, xmlFile);
        } else {
            logger.warn("Element with tagName {} not found", tagName);
        }
    }

    /**
     * Retrieves the value of an element by its tag name.
     *
     * @param xmlFile the XML file.
     * @param tagName the name of the tag.
     * @return the value of the element, or Optional.empty if not found.
     * @throws Exception if reading the file fails.
     */
    static Optional<String> getElementValueFromXmlFile(File xmlFile, String tagName) throws Exception {
        logger.debug("Getting value of element with tagName {}", tagName);
        var document = getDocumentFromFile(xmlFile);
        var nodes = document.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            var element = (Element) nodes.item(0);
            return Optional.ofNullable(element.getTextContent());
        }
        return Optional.empty();
    }

    /**
     * Helper method to read XML content from a file and return as a Document.
     *
     * @param xmlFile the XML file to read.
     * @return the Document representation of the XML.
     * @throws Exception if reading the file fails.
     */
    private static Document getDocumentFromFile(File xmlFile) throws Exception {
        var documentBuilderFactory = DocumentBuilderFactory.newInstance();
        var documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(xmlFile);
    }

    /**
     * Helper method to write a Document back to the XML file.
     *
     * @param document the Document to write.
     * @param xmlFile the XML file.
     * @throws Exception if writing to the file fails.
     */
    private static void writeDocumentToXmlFile(Document document, File xmlFile) throws Exception {
        var transformerFactory = TransformerFactory.newInstance();
        var transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty("indent", "yes");
        var source = new DOMSource(document);
        var result = new StreamResult(xmlFile);
        transformer.transform(source, result);
    }

    /**
     * Pretty prints the XML file content.
     *
     * @param xmlFile the XML file.
     * @throws Exception if reading or writing the file fails.
     */
    static void prettyPrintXmlFile(File xmlFile) throws Exception {
        logger.debug("Pretty printing XML file: {}", xmlFile.getAbsolutePath());
        var document = getDocumentFromFile(xmlFile);
        writeDocumentToXmlFile(document, xmlFile);
    }

    /**
     * Moves an element to a new position within the XML file.
     *
     * @param xmlFile the XML file.
     * @param tagName the name of the tag to move.
     * @param targetTagName the target element where the tag will be moved.
     * @throws Exception if the operation fails.
     */
    static void moveElementWithinXml(File xmlFile, String tagName, String targetTagName) throws Exception {
        logger.debug("Moving element with tagName {} to new position before {}", tagName, targetTagName);
        var document = getDocumentFromFile(xmlFile);
        var nodes = document.getElementsByTagName(tagName);
        var targetNodes = document.getElementsByTagName(targetTagName);

        if (nodes.getLength() > 0 && targetNodes.getLength() > 0) {
            var element = (Element) nodes.item(0);
            var targetElement = (Element) targetNodes.item(0);
            targetElement.getParentNode().insertBefore(element, targetElement);
            writeDocumentToXmlFile(document, xmlFile);
        } else {
            logger.warn("Element(s) with tagName {} or {} not found", tagName, targetTagName);
        }
    }

    /**
     * Renames an element's tag in the XML file.
     *
     * @param xmlFile the XML file.
     * @param oldTagName the current name of the tag.
     * @param newTagName the new name of the tag.
     * @throws Exception if the operation fails.
     */
    static void renameElementTagInXml(File xmlFile, String oldTagName, String newTagName) throws Exception {
        logger.debug("Renaming element with tagName {} to {}", oldTagName, newTagName);
        var document = getDocumentFromFile(xmlFile);
        var nodes = document.getElementsByTagName(oldTagName);

        if (nodes.getLength() > 0) {
            var element = (Element) nodes.item(0);
            var newElement = document.createElement(newTagName);
            newElement.setTextContent(element.getTextContent());
            element.getParentNode().replaceChild(newElement, element);
            writeDocumentToXmlFile(document, xmlFile);
        } else {
            logger.warn("Element with tagName {} not found", oldTagName);
        }
    }

    /**
     * Deeply clones an element and appends it to the XML document.
     *
     * @param xmlFile the XML file.
     * @param tagName the tag name of the element to clone.
     * @throws Exception if the operation fails.
     */
    static void cloneElementInXml(File xmlFile, String tagName) throws Exception {
        logger.debug("Cloning element with tagName {}", tagName);
        var document = getDocumentFromFile(xmlFile);
        var nodes = document.getElementsByTagName(tagName);

        if (nodes.getLength() > 0) {
            var element = (Element) nodes.item(0);
            var clonedElement = (Element) element.cloneNode(true);
            document.getDocumentElement().appendChild(clonedElement);
            writeDocumentToXmlFile(document, xmlFile);
        } else {
            logger.warn("Element with tagName {} not found", tagName);
        }
    }

    /**
     * Replaces the value of a specific element's attribute.
     *
     * @param xmlFile the XML file.
     * @param tagName the tag name of the element.
     * @param attributeName the name of the attribute.
     * @param newValue the new value of the attribute.
     * @throws Exception if the operation fails.
     */
    static void updateElementAttributeInXml(File xmlFile, String tagName, String attributeName, String newValue) throws Exception {
        logger.debug("Updating attribute {} of element with tagName {} to new value {}", attributeName, tagName, newValue);
        var document = getDocumentFromFile(xmlFile);
        var nodes = document.getElementsByTagName(tagName);

        if (nodes.getLength() > 0) {
            var element = (Element) nodes.item(0);
            element.setAttribute(attributeName, newValue);
            writeDocumentToXmlFile(document, xmlFile);
        } else {
            logger.warn("Element with tagName {} not found", tagName);
        }
    }

    /**
     * Retrieves the attribute value of an element.
     *
     * @param xmlFile the XML file.
     * @param tagName the tag name of the element.
     * @param attributeName the name of the attribute.
     * @return the attribute value, or Optional.empty if not found.
     * @throws Exception if the operation fails.
     */
    static Optional<String> getElementAttributeFromXml(File xmlFile, String tagName, String attributeName) throws Exception {
        logger.debug("Getting value of attribute {} from element with tagName {}", attributeName, tagName);
        var document = getDocumentFromFile(xmlFile);
        var nodes = document.getElementsByTagName(tagName);

        if (nodes.getLength() > 0) {
            var element = (Element) nodes.item(0);
            return Optional.ofNullable(element.getAttribute(attributeName));
        }
        return Optional.empty();
    }

    /**
     * Adds an attribute to an element in the XML file.
     *
     * @param xmlFile the XML file.
     * @param tagName the tag name of the element.
     * @param attributeName the name of the attribute.
     * @param attributeValue the value of the attribute.
     * @throws Exception if the operation fails.
     */
    static void addAttributeToElementInXml(File xmlFile, String tagName, String attributeName, String attributeValue) throws Exception {
        logger.debug("Adding attribute {} to element with tagName {}", attributeName, tagName);
        var document = getDocumentFromFile(xmlFile);
        var nodes = document.getElementsByTagName(tagName);

        if (nodes.getLength() > 0) {
            var element = (Element) nodes.item(0);
            element.setAttribute(attributeName, attributeValue);
            writeDocumentToXmlFile(document, xmlFile);
        } else {
            logger.warn("Element with tagName {} not found", tagName);
        }
    }

    /**
     * Removes an attribute from an element in the XML file.
     *
     * @param xmlFile the XML file.
     * @param tagName the tag name of the element.
     * @param attributeName the name of the attribute to remove.
     * @throws Exception if the operation fails.
     */
    static void removeAttributeFromElementInXml(File xmlFile, String tagName, String attributeName) throws Exception {
        logger.debug("Removing attribute {} from element with tagName {}", attributeName, tagName);
        var document = getDocumentFromFile(xmlFile);
        var nodes = document.getElementsByTagName(tagName);

        if (nodes.getLength() > 0) {
            var element = (Element) nodes.item(0);
            element.removeAttribute(attributeName);
            writeDocumentToXmlFile(document, xmlFile);
        } else {
            logger.warn("Element with tagName {} not found", tagName);
        }
    }
}