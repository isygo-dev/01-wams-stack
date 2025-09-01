package eu.isygoit.com.event;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

/**
 * Abstract Kafka producer for sending XML data.
 * Extends the abstract producer to handle any type T, serializing it to XML.
 * Subclasses must set the topic and XSD schema path (e.g., via @Value or constructor) and be annotated with @Service.
 *
 * @param <T> the type parameter
 */
@Slf4j
public abstract class KafkaXmlProducer<T> extends AbstractKafkaProducer<T> {

    private final XmlMapper xmlMapper;

    /**
     * The Xsd schema path.
     */
    @Getter
    @Setter
    protected String xsdSchemaPath; // Set by concrete classes via @Value

    @Value("${kafka.security.enable-xml-validation:false}")
    private boolean enableXmlValidation;

    /**
     * Instantiates a new Kafka xml producer.
     */
    public KafkaXmlProducer() {
        this.xmlMapper = new XmlMapper();
        XMLInputFactory factory = xmlMapper.getFactory().getXMLInputFactory();
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false); // Prevent XXE
    }

    @Override
    protected byte[] serialize(T message) throws Exception {
        if (message == null) {
            throw new IllegalArgumentException("Cannot serialize null message");
        }
        String xml = xmlMapper.writeValueAsString(message);
        if (enableXmlValidation && xsdSchemaPath != null && !xsdSchemaPath.isEmpty()) {
            validateXmlSchema(xml);
        }
        return xml.getBytes(StandardCharsets.UTF_8);
    }

    private void validateXmlSchema(String xml) throws Exception {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(xsdSchemaPath));
        javax.xml.validation.Validator validator = schema.newValidator();
        validator.validate(new StreamSource(new StringReader(xml)));
    }
}