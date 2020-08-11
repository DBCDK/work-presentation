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
import static dk.dbc.search.work.presentation.worker.WorkTreeBuilder.RELS_SYS_STREAM;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
/**
 *
 * @author thp
 */
public class ContentService {
    private static final Logger log = LoggerFactory.getLogger(ContentService.class);

    @Inject
    Config config;

    public IRepositoryDAO.State getObjectState(IRepositoryIdentifier workPid) {
        log.trace("Entering ContentService.getObjectState({})", workPid);

        Client client = config.getHttpClient();

        URI uri = config.getCorepoContentService()
                .path("/rest/objects/{pid}")
                .build(workPid);

        try (InputStream is = client.target(uri)
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(InputStream.class)) {
            // Do something with "is" - like parse it or whatever
        } catch (IOException|WebApplicationException ex) {
            log.error("Error getting datastream {}: {}", workPid, ex.getMessage());
            log.debug("Error getting datastream {}", workPid, ex);
        }

        // TODO: ContentRest: getObjectProfile
        return null;
    }

    public String getDatastreamContent(IRepositoryIdentifier pid, String steamName) {
        log.trace("Entering ContentService.getDatastreamContent({}, {})", pid, steamName);
        // TODO: ContentRest: getDatastreamContent
        return "";
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
