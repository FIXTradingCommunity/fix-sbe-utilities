package io.fixprotocol.sbe.validator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Objects;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import io.fixprotocol.orchestra.event.EventListener;
import io.fixprotocol.orchestra.event.EventListenerFactory;
import io.fixprotocol.orchestra.event.TeeEventListener;

public class SbeSchemaValidator {
  
  public static class Builder {
    private String eventFile;
    private String inputFile;
    private String schemaFile = "xsd/sbe.xsd";

    public SbeSchemaValidator build() {
      return new SbeSchemaValidator(this);
    }

    public Builder eventLog(String eventFile) {
      this.eventFile = eventFile;
      return this;
    }

    public Builder inputFile(String inputFilename) {
      this.inputFile = inputFilename;
      return this;
    }
    
    public Builder schemaFile(String schemaFilename) {
      this.schemaFile = schemaFilename;
      return this;
    }
  }
  private final class ErrorListener implements ErrorHandler {

    @Override
    public void error(SAXParseException exception) throws SAXException {
      eventLogger.error("SbeSchemaValidator: XML error at line {0} col {1} {2}",
          exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
      errors++;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
      eventLogger.fatal("SbeSchemaValidator: XML fatal error at line {0} col {1} {2}",
          exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
      fatalErrors++;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
      eventLogger.warn("SbeSchemaValidator: XML warning at line {0} col {1} {2}",
          exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
      warnings++;
    }
  }
  public static Builder builder() {
    return new Builder();
  }

  public static EventListener createLogger(OutputStream jsonOutputStream) {
    final Logger logger = LogManager.getLogger(SbeSchemaValidator.class);
    final EventListenerFactory factory = new EventListenerFactory();
    TeeEventListener eventListener = null;
    try {
      eventListener = new TeeEventListener();
      final EventListener logEventLogger = factory.getInstance("LOG4J");
      logEventLogger.setResource(logger);
      eventListener.addEventListener(logEventLogger);
      if (jsonOutputStream != null) {
        final EventListener jsonEventLogger = factory.getInstance("JSON");
        jsonEventLogger.setResource(jsonOutputStream);
        eventListener.addEventListener(jsonEventLogger);
      }
    } catch (Exception e) {
      logger.error("Error creating event listener", e);
    }
    return eventListener;
  }
  
  /*public static void main(String[] args) {
    // TODO Auto-generated method stub

  }*/
  
  private int errors = 0;
  private final String eventFile;
  private EventListener eventLogger;
  private int fatalErrors = 0; 
  private final String inputFile;
  private final String schemaFile;
  private int warnings = 0;

  private SbeSchemaValidator(Builder builder) {
    this.eventFile = builder.eventFile;
    this.inputFile = builder.inputFile;
    this.schemaFile = builder.schemaFile;
  }
  
  public int getErrors() {
    return errors;
  }

  public int getFatalErrors() {
    return fatalErrors;
  }

  public int getWarnings() {
    return warnings;
  }
  
  public boolean validate() {
    try {
      eventLogger = createLogger(eventFile != null ? new FileOutputStream(eventFile) : null);
      return validate(new FileInputStream(inputFile), schemaFile);
    } catch (final Exception e) {
      System.err.println(e.getMessage());
      return false;
    } finally {
      try {
        eventLogger.close();
      } catch (Exception e) {

      }
    }
  }
  
  private boolean validate(InputStream inputStream, String schemaFilename) {
    final ErrorListener errorHandler = new ErrorListener();
    Document xmlDocument;
    try {
      xmlDocument = validateSchema(inputStream, errorHandler, schemaFilename);
    } catch (final Exception e) {
      eventLogger.fatal("Failed to validate SBE schema, {0}", e.getMessage());
      fatalErrors++;
    }

    if (getErrors() + getFatalErrors() > 0) {
      eventLogger.fatal(
          "SbeSchemaValidator complete; fatal errors={0,number,integer} errors={1,number,integer} warnings={2,number,integer}",
          getFatalErrors(), getErrors(), getWarnings());
      return false;
    } else {
      eventLogger.info(
          "SbeSchemaValidator complete; fatal errors={0,number,integer} errors={1,number,integer} warnings={2,number,integer}",
          getFatalErrors(), getErrors(), getWarnings());
      return true;
    }
  }
  
  private Document validateSchema(InputStream inputStream, ErrorListener errorHandler, String schemaFilename)
      throws ParserConfigurationException, SAXException, IOException {
    // parse an XML document into a DOM tree
    final DocumentBuilderFactory parserFactory = DocumentBuilderFactory.newInstance();
    parserFactory.setNamespaceAware(true);
    parserFactory.setXIncludeAware(true);
    final DocumentBuilder parser = parserFactory.newDocumentBuilder();
    final Document document = parser.parse(inputStream);

    // create a SchemaFactory capable of understanding WXS schemas
    final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    final ResourceResolver resourceResolver = new ResourceResolver();
    factory.setResourceResolver(resourceResolver);

    // load a WXS schema, represented by a Schema instance
    final URL resourceUrl = this.getClass().getClassLoader().getResource(schemaFilename);
    final String path = Objects.requireNonNull(resourceUrl).getPath();
    final String parentPath = path.substring(0, path.lastIndexOf('/'));
    final URL baseUrl = new URL(resourceUrl.getProtocol(), null, parentPath);
    resourceResolver.setBaseUrl(baseUrl);

    final Source schemaFile = new StreamSource(resourceUrl.openStream());
    final Schema schema = factory.newSchema(schemaFile);

    // create a Validator instance, which can be used to validate an instance document
    final Validator validator = schema.newValidator();

    validator.setErrorHandler(errorHandler);

    // validate the DOM tree
    validator.validate(new DOMSource(document));
    return document;
  }


}
