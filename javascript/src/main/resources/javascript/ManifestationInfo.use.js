use( "Log" );
use( "RecordProcessing" );
use( "XmlUtil" );


EXPORTED_SYMBOLS = [ "ManifestationInfo" ];

var ManifestationInfo = (function() {

    /**
     * Function that extracts manifestation info like title, full title, creators, description, subject and types
     * from the provided datastreams from a corepo record (manifestation)
     *
     * @type {function}
     * @syntax ManifestationInfo.getManifestationInfoFromXmlObjects( manifestationId, xmlObjects )
     * @param {String} manifestationId the pid of the corepo record
     * @param {Object} dataStreamObject and object with the xml stringed datastreams from the corepo record as properties
     * @return {JSON} the extracted manifestation info as a JSON object
     * @function
     * @name ManifestationInfo.getManifestationInfoFromXmlObjects
     */

    function getManifestationInfoFromXmlObjects( manifestationId, dataStreamObject ) {

        Log.trace( "Entering: ManifestationInfo.getManifestationInfoFromXmlObjects function" );

        var manifestationObject =
            {
                "pid": manifestationId,
                "title": null, //from dc stream string, must be present
                "fullTitle": null, //from commonData stream or localData stream if title element present, string, must be present
                "creators": [], //from commonData stream creator - array, empty array if no data
                "description": null, //from commonData stream dcterms:abstract - string, null if no data
                "subjects": [], //commonData or localData if subject element present, array, empty array if no data
                "types": [] //from DC stream, array, most be present
            };
        var dcStreamXml = XmlUtil.fromString( dataStreamObject.DC );
        var commonDataXml = XmlUtil.fromString( dataStreamObject.commonData );
        var localDataXml = XmlUtil.fromString( dataStreamObject.localData );
        manifestationObject.title = getTitle( dcStreamXml );
        manifestationObject.fullTitle = getFullTitle( commonDataXml, localDataXml );

        Log.trace( "Leaving: ManifestationInfo.getManifestationInfoFromXmlObjects function" );

        return manifestationObject;
    }


    /**
     * Function that extracts title from the provided DC stram
     *
     * @type {function}
     * @syntax ManifestationInfo.getTitle( dcStreamXml )
     * @param {Document} dcStreamXml the dc stream as xml
     * @return {String} the extracted title
     * @function
     * @name ManifestationInfo.getTitle
     */

    function getTitle( dcStreamXml ) {

        Log.trace( "Entering: ManifestationInfo.getTitle function" );

        var title = XPath.selectText( "/oai_dc:dc/dc:title", dcStreamXml );

        if ( "" === title ) {
            RecordProcessing.terminateProcessingAndFailRecord(
                "ManifestationInfo.getTitle no title was found in dc stream\n" + dcStreamXml );
        }


        Log.trace( "Leaving: ManifestationInfo.getTitle function" );

        return title;
    }

    /**
     * Function that extracts title from the provided DC stram
     *
     * @type {function}
     * @syntax ManifestationInfo.getFullTitle( commonData, localData )
     * @param {Document} commonData the common stream as xml
     * @param {Document} localData the local stream as xml
     * @return {String} the extracted full title
     * @function
     * @name ManifestationInfo.getFullTitle
     */

    function getFullTitle( commonData, localData ) {

        Log.trace( "Entering: ManifestationInfo.getFullTitle function" );

        //first check if localData has a full title
        var titleFull = XPath.selectText( '/ting:localData/dkabm:record/dc:title[@xsi:type="dkdcplus:full"]', localData );

        if ( "" === titleFull ) {
            titleFull = XPath.selectText( '/ting:container/dkabm:record/dc:title[@xsi:type="dkdcplus:full"]', commonData );
        }

        if ( "" === titleFull ) {
            RecordProcessing.terminateProcessingAndFailRecord(
                "ManifestationInfo.getFullTitle no full title was found in local or common stream" );
        }


        Log.trace( "Leaving: ManifestationInfo.getFullTitle function" );

        return titleFull;
    }


    return {
        getManifestationInfoFromXmlObjects: getManifestationInfoFromXmlObjects,
        getTitle: getTitle,
        getFullTitle: getFullTitle

    };
})();
