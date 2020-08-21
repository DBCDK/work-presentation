/*
 * Copyright (C) 2020 DBC A/S (http://dbc.dk/)
 *
 * This is part of work-presentation-worker
 *
 * work-presentation-worker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * work-presentation-worker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.search.work.presentation.worker.corepo;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
abstract class ElementHandler extends DefaultHandler {

    private static final Logger log = LoggerFactory.getLogger(ElementHandler.class);

    private static final SAXParserFactory FACTORY = makeSAXParserFactory();

    private static SAXParserFactory makeSAXParserFactory() {
        synchronized (SAXParserFactory.class) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            return factory;
        }
    }

    // Date time is in "2018-11-24T23:58:36.175+01:00" format not in DateTimeFormatter.ISO_INSTANT
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    public static Instant parseTimeStamp(String ts) {
        TemporalAccessor time = DATE_TIME_FORMAT.parse(ts);
        return Instant.from(time);
    }

    public final void parse(InputStream is) {
        try {
            SAXParser parser = FACTORY.newSAXParser();
            XMLReader xmlReader = parser.getXMLReader();
            xmlReader.setContentHandler(this);
            xmlReader.parse(new InputSource(is));
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new IllegalStateException("Error parsing XML input", ex);
        }
    }

    private StringBuilder buffer; // if Null buffer has been delivered upon close
    private final HashMap<String, String> lastAttributes = new HashMap<>();
    private String lastUri;
    private String lastLocalName;

    public abstract void element(String uri, String localName, Map<String, String> attributes, String characters);

    @Override
    public final void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (buffer != null) { // Previous open hasn't been closed
            log.trace("element({}, {}, Attributes, null)", lastUri, lastLocalName);
            element(lastUri, lastLocalName, lastAttributes, null);
        }
        lastUri = uri;
        lastLocalName = localName;
        lastAttributes.clear();
        for (int i = 0 ; i < attributes.getLength() ; i++) {
            String value = attributes.getValue(i);
            String attrName = attributes.getLocalName(i);
            String ns = attributes.getURI(i);
            if (ns != null && !ns.isEmpty()) {
                lastAttributes.put('{' + ns + '}' + attrName, value);
            }
            lastAttributes.put(attrName, value);
        }
        buffer = new StringBuilder();
    }

    @Override
    public final void characters(char[] ch, int start, int length) throws SAXException {
        if (buffer != null)
            buffer.append(ch, start, length);
    }

    @Override
    public final void endElement(String uri, String localName, String qName) throws SAXException {
        if (buffer != null) {
            String characters = buffer.toString();
            log.trace("element({}, {}, Attributes, {})", uri, localName, characters);
            element(uri, localName, lastAttributes, characters);
            buffer = null;
        }
    }
}
