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
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author thp
 */
@Stateless
public class ContentService {
    private static final Logger log = LoggerFactory.getLogger(ContentService.class);

    private static final String RELS_SYS_STREAM = "RELS-SYS";

    @Inject
    Config config;

    private final SAXParserFactory factory = SAXParserFactory.newInstance();

    private static class ObjectStateHandler extends DefaultHandler {

        private final static String STATE = "objState";

        private IRepositoryDAO.State state;

        private StringBuffer stateValue = new StringBuffer();

        private boolean inState = false;

        @Override
        public void startElement(String nsUri, String localName, String tagName, Attributes attribs) throws SAXException {
            log.trace("startElement, tag {}", tagName);
            if (tagName.equals(STATE)) {
                log.trace("in state");
                inState = true;
            }
        }

        @Override
        public void endElement(String nsUri, String localName, String tagName) throws SAXException {
            log.trace("endElement, tag {}", tagName);
            if (tagName.equals(STATE)) {
                log.trace("out os state");
                inState = false;
                if (stateValue.toString().equals("A")) {
                    state = IRepositoryDAO.State.ACTIVE;
                } else {
                    state = IRepositoryDAO.State.DELETED;
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (inState) {
                stateValue.append(ch, start, length);
                log.trace("characters {}", stateValue);
            }
        }
    }


    public IRepositoryDAO.State getObjectState(IRepositoryIdentifier pid) throws WebApplicationException, IOException {
        log.trace("Entering ContentService.getObjectState({})", pid);

        Client client = config.getHttpClient();

        URI uri = config.getCorepoContentService()
                .path("/rest/objects/{pid}")
                .build(pid);
        
        Response response = client.target(uri).request(MediaType.APPLICATION_XML_TYPE).get();

        final Response.StatusType statusInfo = response.getStatusInfo();
        log.debug("{} objectProfile status: {}", pid, statusInfo);

        if (statusInfo.equals(Response.Status.OK)) {
            try (InputStream is =  response.readEntity(InputStream.class)) {
                SAXParser parser = factory.newSAXParser();
                XMLReader xmlReader = parser.getXMLReader();
                ObjectStateHandler handler = new ObjectStateHandler();
                xmlReader.setContentHandler(handler);
                xmlReader.parse( new InputSource( is ) );
                return handler.state;
            } catch (SAXException|ParserConfigurationException ex) {
                throw new WebApplicationException("Error parsing ObjectProfile data from " + pid, ex);
            }
        } else {
            throw new WebApplicationException("Object does not exists: " + pid);
        }
    }
    
    public String getDatastreamContent(IRepositoryIdentifier pid, String steamName) throws WebApplicationException {
        log.trace("Entering ContentService.getDatastreamContent({}, {})", pid, steamName);
        // TODO: ContentRest: getDatastreamContent
        Client client = config.getHttpClient();

        URI uri = config.getCorepoContentService()
                .path("/rest/objects/{pid}/datastreams/{stream}/content")
                .build(pid, steamName);
        Response response = client.target(uri).request(MediaType.APPLICATION_XML_TYPE).get();
        return response.readEntity(String.class);
    }

    public RepositoryStream[] getDatastreamList(IRepositoryIdentifier pid) {
        log.trace("Entering ContentService.getDatastreamList");
        // TODO: ContentRest: listDatastreams, getDatastreamProfile and getDatastreamContent
        return new RepositoryStream[] {};
    }

    public SysRelationStreamApi getRelations(IRepositoryIdentifier pid) throws SAXException, ParserConfigurationException, IOException {
        log.trace("Entering ContentService.getRelations");
        String stream = getDatastreamContent(pid, RELS_SYS_STREAM);
        SysRelationStreamApi api = new SysRelationStreamApi(pid, stream.getBytes(StandardCharsets.UTF_8));
        return api;
    }


}
