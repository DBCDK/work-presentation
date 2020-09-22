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
                "subjects": [], //from common and local stream if subject element present, array, empty array if no data
                "types": [] //from DC stream, array, must be present
            };

        //check that we have necessary data streams
        if ( dataStreamObject.DC && dataStreamObject.commonData ) {
            var dcStreamXml = XmlUtil.fromString( dataStreamObject.DC );
            var commonDataXml = XmlUtil.fromString( dataStreamObject.commonData );
        } else {
            RecordProcessing.terminateProcessingAndFailRecord(
                "ManifestationInfo.getManifestationInfoFromXmlObjects missing either commonData or DC stream" );
        }

        if ( dataStreamObject.localData ) {
            var localDataXml = XmlUtil.fromString( dataStreamObject.localData );
        } else {
            localDataXml = XmlUtil.fromString( "<empty/>" );
        }
        manifestationObject.title = getTitle( dcStreamXml );
        manifestationObject.fullTitle = getFullTitle( commonDataXml, localDataXml );
        manifestationObject.creators = getCreators( commonDataXml, localDataXml );
        manifestationObject.description = getAbstract( commonDataXml, localDataXml );
        manifestationObject.subjects = getSubjects( commonDataXml, localDataXml );
        manifestationObject.types = getTypes( dcStreamXml );

        Log.trace( "Leaving: ManifestationInfo.getManifestationInfoFromXmlObjects function" );

        return manifestationObject;
    }


    /**
     * Function that extracts title from the provided DC stream
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
        title = title.trim();
        Log.debug( "ManifestationInfo.getTitle title found=", title );

        if ( "" === title ) {
            RecordProcessing.terminateProcessingAndFailRecord(
                "ManifestationInfo.getTitle no title was found in dc stream\n" + dcStreamXml );
        }

        Log.trace( "Leaving: ManifestationInfo.getTitle function" );

        return title;
    }

    /**
     * Function that extracts the full title from the either the local data stream
     * or if not present there the common data stream
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
        titleFull = titleFull.trim();

        if ( "" === titleFull ) {
            titleFull = XPath.selectText( '/ting:container/dkabm:record/dc:title[@xsi:type="dkdcplus:full"]', commonData );
            titleFull = titleFull.trim();
        }

        if ( "" === titleFull ) {
            RecordProcessing.terminateProcessingAndFailRecord(
                "ManifestationInfo.getFullTitle no full title was found in local or common stream" );
        }

        Log.debug( "ManifestationInfo.getFullTitle title full found=", titleFull );

        Log.trace( "Leaving: ManifestationInfo.getFullTitle function" );

        return titleFull;
    }

    /**
     * Function that extracts creators from the provided DC stream
     *
     * @type {function}
     * @syntax ManifestationInfo.getCreators( dcStreamXml )
     * @param {Document} commonData the common stream as xml
     * @param {Document} localData the local stream as xml
     * @return {Array} the extracted creators
     * @function
     * @name ManifestationInfo.getCreators
     */

    function getCreators( commonData, localData ) {

        Log.trace( "Entering: ManifestationInfo.getCreators function" );

        var creators = XPath.select( "/ting:localData/dkabm:record/dc:creator[not(@xsi:type = 'oss:sort')]", localData );
        if ( creators.length === 0 ) {
            creators = XPath.select( "/ting:container/dkabm:record/dc:creator[not(@xsi:type = 'oss:sort')]", commonData );
        }

        for ( var i in creators ) {

            var type = XmlUtil.getAttribute( creators[i], "type", XmlNamespaces.xsi );
            var value = XmlUtil.getText( creators[i] ).trim();

            if ( type === undefined ) {
                type = null;
            } else if ( type === '' ) {
                type = null;
            } else if ( type.startsWith( "dkdcplus:" ) ) {
                Log.trace( "ManifestationInfo.getSubjects type is: dkdcplus" );
                type = type.substr( 9 );
            } else {
                Log.warn( "ManifestationInfo.getSubjects type is: " + type );
            }

            creators[i] = {
                "type": type,
                "value": value
            };

        }

        Log.debug( "ManifestationInfo.getCreators creators found=", creators );

        Log.trace( "Leaving: ManifestationInfo.getCreators function" );

        return creators;
    }


    /**
     * Function that extracts types from the provided DC stream
     *
     * @type {function}
     * @syntax ManifestationInfo.getTypes( dcStreamXml )
     * @param {Document} dcStreamXml the dc stream as xml
     * @return {Array} the extracted types
     * @function
     * @name ManifestationInfo.getTypes
     */

    function getTypes( dcStreamXml ) {

        Log.trace( "Entering: ManifestationInfo.getTypes function" );

        var types = [];
        var allTypes = XPath.selectMultipleText( "/oai_dc:dc/dc:type", dcStreamXml );
        var foundSammensat = false;
        for ( var i = 0; i < allTypes.length; i++ ) {
            var type = allTypes[ i ];
            type = type.trim();
            //set sammensat boolean for now, we might have to add it if its the only one
            if ( "Sammensat materiale" === type ) {
                foundSammensat = true;
            } //dont add sammensat materiale and work or record type
            if ( "Sammensat materiale" !== type && !type.match( /WORK:|RECORD:/ ) ) {
                types.push( type );
            }
        } //end for loop
        if ( 0 === types.length && foundSammensat ) {
            types.push( "Sammensat materiale" );
        }

        Log.debug( "ManifestationInfo.getTypes types found=", types );

        Log.trace( "Leaving: ManifestationInfo.getTypes function" );

        return types;
    }

    /**
     * Function that extracts the abstract from the either the local data stream
     * or if not present there the common data stream
     *
     * @type {function}
     * @syntax ManifestationInfo.getAbstract( commonData, localData )
     * @param {Document} commonData the common stream as xml
     * @param {Document} localData the local stream as xml
     * @return {String|null} the extracted abstract or null if no abstract
     * @function
     * @name ManifestationInfo.getAbstract
     */

    function getAbstract( commonData, localData ) {

        Log.trace( "Entering: ManifestationInfo.getAbstract function" );

        //first check if localData has an abstract
        var abstract = XPath.selectText( '/ting:localData/dkabm:record/dcterms:abstract', localData );
        abstract = abstract.trim();

        if ( "" === abstract ) {
            abstract = XPath.selectText( '/ting:container/dkabm:record/dcterms:abstract', commonData );
            abstract = abstract.trim();
        }


        abstract = ("" === abstract) ? null : abstract;

        Log.debug( "ManifestationInfo.getAbstract abstract found=", String( abstract ) );

        Log.trace( "Leaving: ManifestationInfo.getAbstract function" );

        return abstract;
    }


    /**
     * Function that extracts subjects from the provided DC stream
     *
     * @type {function}
     * @syntax ManifestationInfo.getSubjects( tingStreamXml )
     * @param {Document} commonData the common stream as xml
     * @param {Document} localData the local stream as xml
     * @return {Array} the extracted subjects
     * @function
     * @name ManifestationInfo.getSubjects
     */

    function getSubjects( commonData, localData ) {

        Log.trace( "Entering: ManifestationInfo.getSubjects function" );

        var subjects = XPath.select( "/ting:localData/dkabm:record/dc:subject", localData );
        if ( subjects.length === 0 ) {
            subjects = XPath.select( "/ting:container/dkabm:record/dc:subject", commonData );
        }
        for ( var i in subjects ) {

            var type = XmlUtil.getAttribute( subjects[i], "type", XmlNamespaces.xsi );
            var value = XmlUtil.getText( subjects[i] ).trim();

            if ( type === undefined ) {
                type = null;
            } else if ( type === '' ) {
                type = null;
            } else if ( type.startsWith( "dkdcplus:" ) ) {
                Log.trace( "ManifestationInfo.getSubjects type is: dkdcplus" );
                type = type.substr( 9 );
            } else {
                Log.warn( "ManifestationInfo.getSubjects type is: " + type );
            }

            subjects[i] = {
                "type": type,
                "value": value
            };

        }

        Log.debug( "ManifestationInfo.getSubjects subjects found=", subjects );

        Log.trace( "Leaving: ManifestationInfo.getSubjects function" );

        return subjects;
    }


    return {
        getManifestationInfoFromXmlObjects: getManifestationInfoFromXmlObjects,
        getTitle: getTitle,
        getFullTitle: getFullTitle,
        getCreators: getCreators,
        getTypes: getTypes,
        getAbstract: getAbstract,
        getSubjects: getSubjects
    };
})();
