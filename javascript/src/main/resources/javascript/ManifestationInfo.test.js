use( "UnitTest" );

use( "ManifestationInfo" );
use( "XmlUtil" );


UnitTest.addFixture( "ManifestationInfo.getManifestationInfoFromXmlObjects", function() {
    var dcStream =
        '<oai_dc:dc ' +
        'xmlns:dc="http://purl.org/dc/elements/1.1/" ' +
        'xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" ' +
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
        '<dc:title>værkstedstekniske beregninger</dc:title>' +
        '<dc:language>Dansk</dc:language>' +
        '<dc:type>Bog</dc:type>' +
        '<dc:publisher>jernindustriensforlag</dc:publisher>' +
        '<dc:date>1972</dc:date>' +
        '<dc:identifier>870970-basis:08021473</dc:identifier>' +
        '<dc:identifier>NUMBER:1020</dc:identifier>' +
        '<dc:identifier>NUMBER:1022</dc:identifier>' +
        '<dc:identifier>NUMBER:1020</dc:identifier>' +
        '<dc:identifier>NUMBER:1022</dc:identifier>' +
        '<dc:relation>50378705</dc:relation>' +
        '</oai_dc:dc>';


    var commonData =
        '<ting:container ' +
        'xmlns:ac="http://biblstandard.dk/ac/namespace/" ' +
        'xmlns:dc="http://purl.org/dc/elements/1.1/" ' +
        'xmlns:dcterms="http://purl.org/dc/terms/" ' +
        'xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/" ' +
        'xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/" ' +
        'xmlns:docbook="http://docbook.org/ns/docbook" ' +
        'xmlns:oss="http://oss.dbc.dk/ns/osstypes" ' +
        'xmlns:ting="http://www.dbc.dk/ting" ' +
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
        '<dkabm:record>' +
        '<ac:identifier>08021473|870970</ac:identifier>' +
        '<ac:source>Bibliotekskatalog</ac:source>' +
        '<dc:title>Værkstedstekniske beregninger</dc:title>' +
        '<dc:title xsi:type="dkdcplus:full">Værkstedstekniske beregninger. M2, Boring</dc:title>' +
        '<dc:subject xsi:type="dkdcplus:DK5">51.8</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DK5-Text">Regning</dc:subject>' +
        '<dcterms:audience>voksenmaterialer</dcterms:audience>' +
        '<dkdcplus:version>2. udgave</dkdcplus:version>' +
        '<dc:publisher>Jernindustriens Forlag</dc:publisher>' +
        '<dc:date>1972</dc:date>' +
        '<dc:type xsi:type="dkdcplus:BibDK-Type">Bog</dc:type>' +
        '<dcterms:extent>16 sider</dcterms:extent>' +
        '<dcterms:extent>11 bind</dcterms:extent>' +
        '<dc:language xsi:type="dcterms:ISO639-2">dan</dc:language>' +
        '<dc:language>Dansk</dc:language>' +
        '</dkabm:record>' +
        '<marcx:collection' +
        'xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '...' +
        '</marcx:collection>' +
        '<ln:links' +
        'xmlns:ln="http://oss.dbc.dk/ns/links">' +
        '<ln:link>' +
        '<ln:access>remote</ln:access>' +
        '<ln:accessType/>' +
        '<ln:linkTo>resolver</ln:linkTo>' +
        '<ln:relationType>dbcaddi:hasOpenUrl</ln:relationType>' +
        '<ln:url>REDACTED</ln:url>' +
        '<ln:collectionIdentifier>870970-basis</ln:collectionIdentifier>' +
        '<ln:collectionIdentifier>870970-danbib</ln:collectionIdentifier>' +
        '<ln:collectionIdentifier>870970-bibdk</ln:collectionIdentifier>' +
        '</ln:link>' +
        '</ln:links>' +
        '<adminData>' +
        '<recordStatus>active</recordStatus>' +
        '<creationDate>2016-07-09</creationDate>' +
        '<libraryType>none</libraryType>' +
        '<genre>nonfiktion</genre>' +
        '<indexingAlias>danmarcxchange</indexingAlias>' +
        '<accessType>physical</accessType>' +
        '<workType>literature</workType>' +
        '<collectionIdentifier>870970-basis</collectionIdentifier>' +
        '<collectionIdentifier>870970-danbib</collectionIdentifier>' +
        '<collectionIdentifier>870970-bibdk</collectionIdentifier>' +
        '</adminData>' +
        '</ting:container>';

    var localData =
        '<ting:localData ' +
        'xmlns:marcx="info:lc/xmlns/marcxchange-v1" ' +
        'xmlns:ting="http://www.dbc.dk/ting">' +
        '<marcx:record format="danMARC2" type="BibliographicLocal">' +
        '<marcx:datafield ind1="0" ind2="0" tag="d08">' +
        '<marcx:subfield code="a">dj (lfu-post)</marcx:subfield>' +
        '</marcx:datafield>' +
        '<marcx:datafield ind1="0" ind2="0" tag="d08">' +
        '<marcx:subfield code="a">dj (lfu-post)</marcx:subfield>' +
        '</marcx:datafield>' +
        '<marcx:datafield ind1="0" ind2="0" tag="d08">' +
        '<marcx:subfield code="a">dj (lfu-post)</marcx:subfield>' +
        '</marcx:datafield>' +
        '<marcx:datafield ind1="0" ind2="0" tag="d08">' +
        '<marcx:subfield code="a">dj (lfu-post)</marcx:subfield>' +
        '</marcx:datafield>' +
        '</marcx:record>' +
        '</ting:localData>';

    var xmlObjects = {
        "DC": dcStream,
        "commonData": commonData,
        "localData": localData
    };
    var manifestationId = "870970-basis:08021473";

    var expected =
        {
            "pid": "870970-basis:08021473",
            "title": "værkstedstekniske beregninger", //from dc stream string, must be present
            "fullTitle": "Værkstedstekniske beregninger. M2, Boring", //from commonData stream or localData stream if title element present, string, must be present
            "creators": [], //from commonData stream creator - array, empty array if no data
            "description": null, //from commonData stream dcterms:abstract - string, null if no data
            "subjects": [], //commonData or localData if subject element present, array, empty array if no data
            "types": [ "Bog" ] //from DC stream, array, most be present
        };

    Assert.equalValue( "get manifestation info from record with no creators and no subjects", ManifestationInfo.getManifestationInfoFromXmlObjects( manifestationId, xmlObjects ), expected );
} );


UnitTest.addFixture( "ManifestationInfo.getTitle", function() {
    var dcStreamString =
        '<oai_dc:dc xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
        '<dc:title>værkstedstekniske beregninger</dc:title>' +
        '<dc:language>Dansk</dc:language>' +
        '<dc:type>Bog</dc:type>' +
        '<dc:publisher>jernindustriensforlag</dc:publisher>' +
        '<dc:date>1972</dc:date>' +
        '<dc:identifier>870970-basis:08021473</dc:identifier>' +
        '<dc:identifier>NUMBER:1020</dc:identifier>' +
        '<dc:identifier>NUMBER:1022</dc:identifier>' +
        '<dc:identifier>NUMBER:1020</dc:identifier>' +
        '<dc:identifier>NUMBER:1022</dc:identifier>' +
        '<dc:relation>50378705</dc:relation>' +
        '</oai_dc:dc>';

    var dcStream = XmlUtil.fromString( dcStreamString );

    var expected = "værkstedstekniske beregninger";

    Assert.equalValue( "get title from dc stream ", ManifestationInfo.getTitle( dcStream ), expected );

    dcStreamString =
        '<oai_dc:dc ' +
        'xmlns:dc="http://purl.org/dc/elements/1.1/" ' +
        'xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" ' +
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
        '</oai_dc:dc>';

    dcStream = XmlUtil.fromString( dcStreamString );

    Assert.exception( "Stop processing if dc has no title", function() {
        ManifestationInfo.getTitle( dcStream );
    }, Packages.dk.dbc.javascript.recordprocessing.FailRecord );

} );
