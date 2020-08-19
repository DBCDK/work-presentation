/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.dbc.search.work.presentation.worker;

import dk.dbc.opensearch.commons.repository.IRepositoryDAO;
import dk.dbc.opensearch.commons.repository.IRepositoryIdentifier;
import dk.dbc.opensearch.commons.repository.RepositoryStream;
import dk.dbc.opensearch.commons.repository.SysRelationStreamApi;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Access to Corepo Content Service
 * @author thp
 */
@Stateless
public class ContentService {
    private static final Logger log = LoggerFactory.getLogger(ContentService.class);

    private static final String RELS_SYS_STREAM = "RELS-SYS";

    // Date time is in "2018-11-24T23:58:36.175+01:00" format not in DateTimeFormatter.ISO_INSTANT
    private final static DateTimeFormatter dataFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    public static class MetaData {
        public final String modified;
        public final IRepositoryDAO.State state;

        public MetaData(String modified, IRepositoryDAO.State state) {
            this.modified = modified;
            this.state = state;
        }
    }

    @Inject
    Config config;

    private final SAXParserFactory factory = SAXParserFactory.newInstance();

    /**
     * Parser for Corepo Content Service "get" operation for "object"
     * Parses state and modification date of a Corepo object
     */
    private static class ObjectHandler extends DefaultHandler {

        private final static String STATE = "objState";
        private final static String MODIFIED = "objLastModDate";

        private final IRepositoryIdentifier pid;

        private IRepositoryDAO.State state;
        private String modified;

        private final StringBuffer buffer = new StringBuffer();

        private boolean inState = false;
        private boolean inModified = false;

        private ObjectHandler(IRepositoryIdentifier pid) {
            this.pid = pid;
        }

        @Override
        public void startElement(String nsUri, String localName, String tagName, Attributes attribs) throws SAXException {
            log.trace("{} - startElement, tag {}", pid, tagName);
            if (tagName.equals(STATE)) {
                log.trace("{} - in state", pid);
                inState = true;
            }
            if (tagName.equals(MODIFIED)) {
                log.trace("{} - in modified", pid);
                inModified = true;
            }
        }

        @Override
        public void endElement(String nsUri, String localName, String tagName) throws SAXException {
            log.trace("{} - endElement, tag {}", pid, tagName);
            if (tagName.equals(STATE)) {
                log.trace("{} - out of state", pid);
                inState = false;
                if (buffer.toString().equals("A")) {
                    state = IRepositoryDAO.State.ACTIVE;
                } else {
                    state = IRepositoryDAO.State.DELETED;
                }
            }
            if (tagName.equals(MODIFIED)) {
                log.trace("{} - out of modified", pid);
                inModified = false;
                modified = buffer.toString();
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (inState || inModified) {
                buffer.append(ch, start, length);
                log.trace("{} - characters {}", pid, buffer);
            }
        }
    }

    /**
     * Parser for a Corepo Content Service "get" operation for a "stream"
     * Parses state and modification date
     */
    private static class StreamMetaHandler extends DefaultHandler {

        private final static String STATE = "dsState";
        private final static String MODIFIED = "dsCreateDate";

        private final IRepositoryIdentifier pid;
        private final String streamName;

        private IRepositoryDAO.State state;
        private String modified;

        private final StringBuffer buffer = new StringBuffer();

        private boolean inState = false;
        private boolean inModified = false;

        private StreamMetaHandler(IRepositoryIdentifier pid, String streamName) {
            this.pid = pid;
            this.streamName = streamName;
        }

        @Override
        public void startElement(String nsUri, String localName, String tagName, Attributes attribs) throws SAXException {
            log.trace("{} - startElement, tag {}", pid, tagName);
            if (tagName.equals(STATE)) {
                log.trace("{} - {} - in state", pid, streamName);
                inState = true;
            }
            if (tagName.equals(MODIFIED)) {
                log.trace("{} - {} - in modified", pid, streamName);
                inModified = true;
            }
        }

        @Override
        public void endElement(String nsUri, String localName, String tagName) throws SAXException {
            log.trace("{} - {} - endElement, tag {}", pid, streamName, tagName);
            if (tagName.equals(STATE)) {
                log.trace("{} - {} - out of state", pid, streamName);
                inState = false;
                if (buffer.toString().equals("A")) {
                    state = IRepositoryDAO.State.ACTIVE;
                } else {
                    state = IRepositoryDAO.State.DELETED;
                }
            }
            if (tagName.equals(MODIFIED)) {
                log.trace("{} - {} - out of modified", pid, streamName);
                inModified = false;
                modified = buffer.toString();
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (inState || inModified) {
                buffer.append(ch, start, length);
                log.trace("{} - {} - characters {}", pid, streamName, buffer);
            }
        }
    }


    /**
     * Parser for a Corepo Content Service "get" operation for "streams" list of an object
     * Parses names of the all streams of the object
     */
    private static class DatastreamsListHandler extends DefaultHandler {

        private final static String DATASTREAM = "datastream";
        private final static String DATASTREAM_ID = "dsid";

        private final IRepositoryIdentifier pid;

        private final List<String> streams = new ArrayList<>();
        
        private DatastreamsListHandler(IRepositoryIdentifier pid) {
            this.pid = pid;
        }

        @Override
        public void startElement(String nsUri, String localName, String tagName, Attributes attribs) throws SAXException {
            log.trace("{} - startElement, tag {}, attributes {}", pid, tagName, attribs);
            if (tagName.equals(DATASTREAM)) {
                String id = attribs.getValue(DATASTREAM_ID);
                log.debug("{} - list datastreams: {}", pid, id);
                streams.add(id);
            }
        }
        public String[] getStreamNames() {
            return streams.toArray(new String[streams.size()]);
        }
    }

    /**
     * Get the MetaData of a Corepo object through the Corepo Content Service
     * @param pid the identifier of the object
     * @return The meta data for the object
     * @throws WebApplicationException on error from content service or parsing result
     */
    @Timed
    public MetaData getObjectMetaData(IRepositoryIdentifier pid) throws WebApplicationException {
        log.trace("Entering getObjectMetaData({})", pid);

        Client client = config.getHttpClient();

        URI uri = config.getCorepoContentService()
                .path("/rest/objects/{pid}")
                .build(pid);
        
        Response response = client.target(uri).request(MediaType.APPLICATION_XML_TYPE).get();

        final Response.StatusType statusInfo = response.getStatusInfo();
        log.trace("{} - objectProfile status: {}", pid, statusInfo);

        if (statusInfo.equals(Response.Status.OK)) {
            try (InputStream is =  response.readEntity(InputStream.class)) {
                SAXParser parser = factory.newSAXParser();
                XMLReader xmlReader = parser.getXMLReader();
                ObjectHandler handler = new ObjectHandler(pid);
                xmlReader.setContentHandler(handler);
                xmlReader.parse( new InputSource( is ) );
                return new MetaData(handler.modified, handler.state);
            } catch (SAXException|ParserConfigurationException|IOException ex) {
                throw new WebApplicationException("Error parsing ObjectProfile data from " + pid, ex);
            }
        } else {
            // TODO: If object has been deleted, the work must be deleted from database
            String message = String.format("objectProfile does not exists: %s", pid);
            log.info(message);
            throw new WebApplicationException(message);
        }
    }
    
    /**
     * Get the MetaData of a Corepo datastream through the Corepo Content Service
     * @param pid the identifier of the object
     * @param streamName Name of stream of the get
     * @return The meta data for the stream
     * @throws WebApplicationException on error from content service or parsing result
     */
    @Timed
    public MetaData getDatastreamMetaData(IRepositoryIdentifier pid, String streamName) throws WebApplicationException {
        log.trace("Entering getDatastreamMetaData({}, {})", pid, streamName);

        Client client = config.getHttpClient();

        URI uri = config.getCorepoContentService()
                .path("/rest/objects/{pid}/datastreams/{streamName}")
                .build(pid, streamName);

        Response response = client.target(uri).request(MediaType.APPLICATION_XML_TYPE).get();

        final Response.StatusType statusInfo = response.getStatusInfo();
        log.trace("{} - streeamProfile status: {}", pid, statusInfo);

        if (statusInfo.equals(Response.Status.OK)) {
            try (InputStream is =  response.readEntity(InputStream.class)) {
                SAXParser parser = factory.newSAXParser();
                XMLReader xmlReader = parser.getXMLReader();
                StreamMetaHandler handler = new StreamMetaHandler(pid, streamName);
                xmlReader.setContentHandler(handler);
                xmlReader.parse( new InputSource( is ) );
                return new MetaData(handler.modified, handler.state);
            } catch (SAXException|ParserConfigurationException|IOException ex) {
                String message = String.format("Error parsing datastream meta from %s - %s", pid, streamName);
                throw new WebApplicationException(message, ex);
            }
        } else {
            String message = String.format("Stream does not exists:  %s - %s", pid, streamName);
            log.info(message);
            throw new WebApplicationException(message);
        }
    }

    /**
     * Get the content of a Corepo datastream through the Corepo Content Service
     * @param pid the identifier of the object
     * @param streamName Name of stream of the get
     * @return The content of the stream
     */
    @Timed
    public String getDatastreamContent(IRepositoryIdentifier pid, String streamName) {
        log.trace("Entering getDatastreamContent({}, {})", pid, streamName);
        Client client = config.getHttpClient();

        URI uri = config.getCorepoContentService()
                .path("/rest/objects/{pid}/datastreams/{stream}/content")
                .build(pid, streamName);
        Response response = client.target(uri).request(MediaType.APPLICATION_XML_TYPE).get();
        return response.readEntity(String.class);
    }

    /**
     * Get content and metadata of a datastream through the Corepo Content Service
     * @param pid the identifier of the object
     * @param streamName Name of stream of the get
     * @return An object containing content and metadata of the stream
     */
    @Timed
    public RepositoryStream getDataStream(IRepositoryIdentifier pid, String streamName) {
        String datastreamContent = getDatastreamContent(pid, streamName);
        MetaData datastreamMetaData = getDatastreamMetaData(pid, streamName);

        TemporalAccessor time = dataFormat.parse(datastreamMetaData.modified);
        Instant instant = Instant.from( time );
        Date modified = Date.from(instant);
        return new RepositoryStream(streamName, datastreamContent.getBytes(StandardCharsets.UTF_8), datastreamMetaData.state, pid, modified);
    }

    /**
     * Get content and metadata of all of the datastream of an object through the Corepo Content Service
     * @param pid the identifier of the object
     * @return A list of object containing content and metadata of the streams
     */
    @Timed
    public RepositoryStream[] getDatastreams(IRepositoryIdentifier pid) {
        log.trace("Entering getDatastreams {}", pid);
        Client client = config.getHttpClient();

        URI uri = config.getCorepoContentService()
                .path("/rest/objects/{pid}/datastreams")
                .build(pid);

        Response response = client.target(uri).request(MediaType.APPLICATION_XML_TYPE).get();

        final Response.StatusType statusInfo = response.getStatusInfo();
        log.trace("{} listDatastreams status: {}", pid, statusInfo);

        if (statusInfo.equals(Response.Status.OK)) {
            try (InputStream is =  response.readEntity(InputStream.class)) {
                SAXParser parser = factory.newSAXParser();
                XMLReader xmlReader = parser.getXMLReader();
                DatastreamsListHandler handler = new DatastreamsListHandler(pid);
                xmlReader.setContentHandler(handler);
                xmlReader.parse( new InputSource( is ) );
                String[] streamNames = handler.getStreamNames();
                log.debug("getDatastreams({}) found streams {}", pid, streamNames);

                List<RepositoryStream> streams = new ArrayList<>(streamNames.length);
                for (String streamName : streamNames) {
                    streams.add(getDataStream(pid, streamName));
                }
                return streams.toArray(new RepositoryStream[streamNames.length]);
            } catch (SAXException|ParserConfigurationException|IOException ex) {
                throw new WebApplicationException("Error parsing ObjectProfile data from " + pid, ex);
            }
        } else {
            // TODO: If object has been deleted, the work must be deleted from database
            log.info("Object does not exists:  {}", pid);
            throw new WebApplicationException("Object does not exists: " + pid);
        }
    }

    /**
     * Get system relations of an object through the Corepo Content Service
     * @param pid the identifier of the object
     * @return
     * @throws SAXException If parsing stream fails
     * @throws ParserConfigurationException If parser creation fails
     * @throws IOException if reading data fails
     */
    @Timed
    public SysRelationStreamApi getRelations(IRepositoryIdentifier pid) throws SAXException, ParserConfigurationException, IOException {
        log.trace("Entering getRelations");
        String stream = getDatastreamContent(pid, RELS_SYS_STREAM);
        SysRelationStreamApi api = new SysRelationStreamApi(pid, stream.getBytes(StandardCharsets.UTF_8));
        return api;
    }

}
