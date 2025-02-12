package eu.isygoit.helper;

import org.springframework.util.ResourceUtils;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;

/**
 * The interface Xml helper.
 */
public interface XmlHelper {

    /**
     * Xml to object t.
     *
     * @param <E>       the type parameter
     * @param xmlString the xml string
     * @param objClass  the obj class
     * @return the t
     * @throws JAXBException the jaxb exception
     */
    static <E> E xmlToObject(String xmlString, Class<E> objClass) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(objClass);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (E) jaxbUnmarshaller.unmarshal(new StringReader(xmlString));
    }

    /**
     * Xml to object t.
     *
     * @param <E>            the type parameter
     * @param file           the file
     * @param unmarshalClass the unmarshal class
     * @return the t
     * @throws JAXBException the jaxb exception
     */
    static <E> E xmlToObject(InputStream file, Class<E> unmarshalClass) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(unmarshalClass);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (E) jaxbUnmarshaller.unmarshal(file);
    }

    /**
     * Object to xml string.
     *
     * @param <E>      the type parameter
     * @param object   the object
     * @param objClass the obj class
     * @return the string
     * @throws JAXBException the jaxb exception
     */
    static <E> String objectToXml(E object, Class<E> objClass) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(objClass);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(object, sw);
        return sw.toString();
    }

    /**
     * Object to xml file file.
     *
     * @param <E>      the type parameter
     * @param object   the object
     * @param objClass the obj class
     * @return the file
     * @throws JAXBException the jaxb exception
     */
    static <E> File objectToXmlFile(E object, Class<E> objClass) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(objClass);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        File file = new File("test.xml");
        jaxbMarshaller.marshal(object, file);
        return file;
    }

    /**
     * Validate xml boolean.
     *
     * @param xmlFile        the xml file
     * @param xsdFilePath    the xsd file path
     * @param schemaLanguage the schema language
     * @return the boolean
     * @throws IOException  the io exception
     * @throws SAXException the sax exception
     */
    static boolean validateXml(String xmlFile, String xsdFilePath, String schemaLanguage) throws IOException, SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(schemaLanguage /*XMLConstants.W3C_XML_SCHEMA_NS_URI*/);
        Schema schema = factory.newSchema(ResourceUtils.getFile(xsdFilePath));
        Validator validator = schema.newValidator();
        StringReader reader = new StringReader(xmlFile);
        validator.validate(new StreamSource(reader));
        return true;
    }

    /**
     * Validate xml boolean.
     *
     * @param xmlFile        the xml file
     * @param xsdFile        the xsd file
     * @param schemaLanguage the schema language
     * @return the boolean
     * @throws IOException  the io exception
     * @throws SAXException the sax exception
     */
    static boolean validateXml(String xmlFile, File xsdFile, String schemaLanguage) throws IOException, SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(schemaLanguage /*XMLConstants.W3C_XML_SCHEMA_NS_URI*/);
        Schema schema = factory.newSchema(xsdFile);
        Validator validator = schema.newValidator();
        StringReader reader = new StringReader(xmlFile);
        validator.validate(new StreamSource(reader));
        return true;
    }
}
