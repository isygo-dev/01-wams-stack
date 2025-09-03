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
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Abstract Kafka consumer for processing XML data.
 * Extends the abstract consumer to handle any type T, deserializing it from XML.
 * Subclasses must set the topic and XSD schema path (e.g., via @Value or constructor),
 * be annotated with @Service, and implement processMessage.
 *
 * @param <T> the type parameter
 */
@Slf4j
public abstract class KafkaXmlConsumer<T> extends AbstractKafkaConsumer<T> {

    private final Class<T> consumerClassType = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];

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
     * Instantiates a new Kafka xml consumer.
     */
    public KafkaXmlConsumer() {
        this.xmlMapper = new XmlMapper();
        XMLInputFactory factory = xmlMapper.getFactory().getXMLInputFactory();
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false); // Prevent XXE
    }

    @Override
    protected T deserialize(byte[] data) throws Exception {
        String xml = new String(data, StandardCharsets.UTF_8);
        if (enableXmlValidation && xsdSchemaPath != null && !xsdSchemaPath.isEmpty()) {
            validateXmlSchema(xml);
        }
        return xmlMapper.readValue(xml, consumerClassType);
    }

    private void validateXmlSchema(String xml) throws Exception {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(xsdSchemaPath));
        javax.xml.validation.Validator validator = schema.newValidator();
        validator.validate(new StreamSource(new StringReader(xml)));
    }

    @Override
    protected final void processMessage(T message, Map<String, String> headers) throws Exception {
        process(message, headers);
    }

    /**
     * Process.
     *
     * @param message the message
     * @param headers the headers
     * @throws Exception the exception
     */
    protected abstract void process(T message, Map<String, String> headers) throws Exception;
}