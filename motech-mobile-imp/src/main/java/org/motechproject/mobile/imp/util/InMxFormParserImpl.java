package org.motechproject.mobile.imp.util;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.motechproject.mobile.core.model.IncMessageStatus;
import org.motechproject.mobile.core.model.IncomingMessage;
import org.motechproject.mobile.core.model.IncomingMessageImpl;
import org.motechproject.mobile.core.util.ConfigurationException;
import org.motechproject.mobile.imp.util.exception.MotechParseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Implementation of the InMessageParser Interface for parsing Motech incoming xForms
 *  in serialized form  generated by mForms application
 *
 * Date Created: 26-Nov-2010
 * @author Igor Opushnyev (iopushnyev@2paths.com)
 */
public class InMxFormParserImpl implements InMessageParser {

    private final Logger log = Logger.getLogger(this.getClass());
    private String separator;
    private String delimiter;
    private String formTypeTagName;
    private String formNameTagName;
    private SimpleDateFormat impDateFormat;
    private SimpleDateFormat oxdDateFormat;
    private Pattern oxdDatePattern;
    private Map<String, String> formTypeLookup;

    private final String messageType = "MxForms"; // Type of the incoming messages this implementation is designed to parse


  /**
     * Parse a Motech incoming message in serialized form
     *
     * @param incomingMessageStringRepresentation
     *         - String representation of a populated with data xForm generated by mForms application
     * @return IInstance of incoming Message class populated with data from the serialized incoming message
     * @throws MotechParseException - if the incoming message cannot be parsed
     * @throws IllegalArgumentException if the incoming message is null
     */
    public IncomingMessage parseIncomingMessage(final String incomingMessageStringRepresentation) throws MotechParseException {

        IncomingMessage incomingMessage = new IncomingMessageImpl();
        incomingMessage.setContent(getIncomingMessageContext(incomingMessageStringRepresentation));
        incomingMessage.setDateCreated(new Date());
        incomingMessage.setMessageStatus(IncMessageStatus.PROCESSING);

        return incomingMessage;

    }

   /**
     * Parse a Motech incoming messages in serialized form
     *
     * @param incomingXmlStringMessage
     *         - String representation of a populated with data xForm generated by mForms application
     * @return IInstance of incoming Message class populated with data from the serialized incoming message
    *  @throws MotechParseException - if the incoming message cannot be parsed
    * @throws IllegalArgumentException if the incoming message is null
     */
    protected String getIncomingMessageContext(final String incomingXmlStringMessage) throws MotechParseException {

        if (incomingXmlStringMessage == null) {
            throw new IllegalArgumentException("Incoming message can not be null");
        }


        StringBuilder incomingMessageContextBulder = new StringBuilder();


        InputStream in = new ByteArrayInputStream(incomingXmlStringMessage.getBytes());
        SAXBuilder saxb = new SAXBuilder();
        Document doc;

        try {
            doc = saxb.build(in);
        } catch (JDOMException e) {

            throw new MotechParseException("Invalid xForm XML:\n" + incomingXmlStringMessage + "\n" + e.getMessage(), e);

        } catch (IOException e) {

            throw new MotechParseException("Can not read incoming message" + e.getMessage(), e);
        }

        Element root = doc.getRootElement();
        Element formTypeElement = root.getChild(formTypeTagName);
        String formType = formTypeElement == null ? null : formTypeElement.getText();

        if (formType == null || formType.trim().isEmpty()) {
            String error = "Empty or No form type defined in xml with root element: " + root.getName() + " and id: " + root.getAttributeValue("id");
            log.error(error);
            throw new MotechParseException(error);
        }

        String formTypeFieldName = formTypeLookup.get(formType);

        if (formTypeFieldName == null || formTypeFieldName.trim().isEmpty()) {
            String error = "Could not find a valid (non-null-or-white-space) form type field name associated with form type: " + formType;
            log.error(error);
            throw new MotechParseException(error);
        }

        Element formNameElement = root.getChild(formNameTagName);


        if (formNameElement == null) {
            throw new MotechParseException("No element (representing the Form Name) found by name " + formNameTagName);
        }

        String formName = formNameElement.getText();

        incomingMessageContextBulder.append(formTypeFieldName);
        incomingMessageContextBulder.append(separator);
        incomingMessageContextBulder.append(formName);


        List children = root.getChildren();
        children.remove(formTypeElement);
        children.remove(formNameElement);

        for (Object o : children) {

            Element child = (Element) o;

            String value;

            try {
                value = formatData(child.getText());
            } catch (ConfigurationException e) {
                //TODO - send notification rerding invalid configuration
                throw new MotechParseException(e.getMessage());
            }

            incomingMessageContextBulder.append(delimiter);
            incomingMessageContextBulder.append(child.getName());
            incomingMessageContextBulder.append(separator);
            incomingMessageContextBulder.append(value);

        }

        return incomingMessageContextBulder.toString();
    }

    /**
     *  Formats data string that matches the given regular expression. into specified format, If the incoming data string does not much
     * the regular expression returns the not formatted incoming string.  
     * @param data - the data string to be formatted
     * @return  formatted data string.
     * @exception ConfigurationException  if a string that matches a regular expression set in the  imp.oxd.dateExpression configuration parameter
                       can not be parsed into a Date using the Date Format defined in the imp.oxd.dateFormat configuration parameter
     */
    protected String formatData(final String data) throws ConfigurationException {

        if (data == null) {
            return null;
        }

        String formattedData;

        if (oxdDatePattern.matcher(data).matches()) {
            try {
                Date date = oxdDateFormat.parse(data);
                formattedData = impDateFormat.format(date);
            } catch (ParseException e) {
               throw new ConfigurationException("Invalid configuration in imp-config.xml.\n" +
                       "Mismatch between the imp.oxd.dateExpression and  imp.oxd.dateFormat.\n" +
                       "A string that matches  a regular expression set in the  imp.oxd.dateExpression configuration parameter " + data +
                       "\n can not be parsed into a Date using the Date Format defined in the imp.oxd.dateFormat configuration parameter");
            }
        } else {
            formattedData = data;
        }

        return formattedData;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public void setFormTypeTagName(String formTypeTagName) {
        this.formTypeTagName = formTypeTagName;
    }

    public void setFormNameTagName(String formNameTagName) {
        this.formNameTagName = formNameTagName;
    }

    public void setImpDateFormat(SimpleDateFormat impDateFormat) {
        this.impDateFormat = impDateFormat;
    }

    protected SimpleDateFormat getImpDateFormat() {
        return impDateFormat;
    }

    public void setOxdDateFormat(SimpleDateFormat oxdDateFormat) {
        this.oxdDateFormat = oxdDateFormat;
    }

    protected SimpleDateFormat getOxdDateFormat() {
        return oxdDateFormat;
    }


    public void setOxdDatePattern(Pattern oxdDatePattern) {
        this.oxdDatePattern = oxdDatePattern;
    }

    public void setFormTypeLookup(Map<String, String> formTypeLookup) {
        this.formTypeLookup = formTypeLookup;
    }

    public String getMessageType() {
        return messageType;
    }
}
