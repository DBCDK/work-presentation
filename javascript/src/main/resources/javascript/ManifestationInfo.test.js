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
        '<marcx:collection ' +
        'xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '...' +
        '</marcx:collection>' +
        '<ln:links ' +
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
            "title": "Værkstedstekniske beregninger", //from dc stream string, must be present
            "fullTitle": "Værkstedstekniske beregninger. M2, Boring", //from commonData stream or localData stream if title element present, string, must be present
            "series": null, //from commonData stream or localData stream if title/dkdcplus:series is present
            "creators": [], //from commonData stream creator - array, empty array if no data
            "description": null, //from commonData stream dcterms:abstract - string, null if no data
            "subjects": [], //from dc stream if subject element present, array, empty array if no data
            "types": [ "Bog" ] //from DC stream, array, most be present
        };

    Assert.equalValue( "get manifestation info from record with no creators and no subjects", ManifestationInfo.getManifestationInfoFromXmlObjects( manifestationId, xmlObjects ), expected );


    xmlObjects = {
        "DC": dcStream,
        "commonData": commonData
    };
    manifestationId = "870970-basis:08021473-no-local-data";

    expected =
        {
            "pid": "870970-basis:08021473-no-local-data",
            "title": "Værkstedstekniske beregninger",
            "fullTitle": "Værkstedstekniske beregninger. M2, Boring",
            "series": null, //from commonData stream or localData stream if title/dkdcplus:series is present
            "creators": [],
            "description": null,
            "subjects": [],
            "types": [ "Bog" ]
        };

    Assert.equalValue( "get manifestation info from record with no creators and no subjects - no local data", ManifestationInfo.getManifestationInfoFromXmlObjects( manifestationId, xmlObjects ), expected );

    manifestationId = "870970-basis:08021473-no-data-streams";

    xmlObjects = {};

    Assert.exception( "Stop processing if missing dc stream or common data stream", function() {
        ManifestationInfo.getManifestationInfoFromXmlObjects( manifestationId, xmlObjects );
    }, Packages.dk.dbc.javascript.recordprocessing.FailRecord );


} );


UnitTest.addFixture( "ManifestationInfo.getTitle", function() {

    var commonDataString = '<empty/>';
    var localDataString =
            '<ting:localData' +
            ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
            ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
            ' xmlns:dcterms="http://purl.org/dc/terms/"' +
            ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
            ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
            ' xmlns:docbook="http://docbook.org/ns/docbook"' +
            ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
            ' xmlns:ting="http://www.dbc.dk/ting"' +
            ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
            '<dkabm:record>' +
            '<ac:identifier>22023578|870970</ac:identifier>' +
            '<ac:source>Bibliotekskatalog</ac:source>' +
            '<dc:title>Ave Femina!</dc:title>' +
            '<dc:title xsi:type="dkdcplus:full">Ave Femina! : Digte</dc:title>' +
            '</dkabm:record>' +
            '</ting:localData>';

    var localData = XmlUtil.fromString(localDataString);
    var commonData = XmlUtil.fromString(commonDataString);

    var expected = "Ave Femina!";

    Assert.equalValue( "get title from localData ", ManifestationInfo.getTitle( commonData, localData ), expected );

    commonDataString =
            '<ting:container' +
            ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
            ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
            ' xmlns:dcterms="http://purl.org/dc/terms/"' +
            ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
            ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
            ' xmlns:docbook="http://docbook.org/ns/docbook"' +
            ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
            ' xmlns:ting="http://www.dbc.dk/ting"' +
            ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
            '<dkabm:record>' +
            '<ac:identifier>22023578|870970</ac:identifier>' +
            '<ac:source>Bibliotekskatalog</ac:source>' +
            '<dc:title>Ave Femina!</dc:title>' +
            '<dc:title xsi:type="dkdcplus:full">Ave Femina! : Digte</dc:title>' +
            '</dkabm:record>' +
            '</ting:container>';
    localDataString = '<empty/>';

    localData = XmlUtil.fromString(localDataString);
    commonData = XmlUtil.fromString(commonDataString);

    expected = "Ave Femina!";

    Assert.equalValue( "get title from commonData ", ManifestationInfo.getTitle( commonData, localData ), expected );

    commonDataString =
            '<ting:container' +
            ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
            ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
            ' xmlns:dcterms="http://purl.org/dc/terms/"' +
            ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
            ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
            ' xmlns:docbook="http://docbook.org/ns/docbook"' +
            ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
            ' xmlns:ting="http://www.dbc.dk/ting"' +
            ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
            '<dkabm:record>' +
            '<ac:identifier>22023578|870970</ac:identifier>' +
            '<ac:source>Bibliotekskatalog</ac:source>' +
            '<dc:title>Ave Femina! - not this one</dc:title>' +
            '<dc:title xsi:type="dkdcplus:full">Ave Femina! : Digte</dc:title>' +
            '</dkabm:record>' +
            '</ting:container>';

    localDataString =
            '<ting:localData' +
            ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
            ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
            ' xmlns:dcterms="http://purl.org/dc/terms/"' +
            ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
            ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
            ' xmlns:docbook="http://docbook.org/ns/docbook"' +
            ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
            ' xmlns:ting="http://www.dbc.dk/ting"' +
            ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
            '<dkabm:record>' +
            '<ac:identifier>22023578|870970</ac:identifier>' +
            '<ac:source>Bibliotekskatalog</ac:source>' +
            '<dc:title>Ave Femina!</dc:title>' +
            '<dc:title xsi:type="dkdcplus:full">Ave Femina! : Digte</dc:title>' +
            '</dkabm:record>' +
            '</ting:localData>';

    localData = XmlUtil.fromString(localDataString);
    commonData = XmlUtil.fromString(commonDataString);

    expected = "Ave Femina!";

    Assert.equalValue( "get title from localData before commonData ", ManifestationInfo.getTitle( commonData, localData ), expected );

    commonDataString = '<empty/>';
    localDataString =
            '<ting:localData' +
            ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
            ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
            ' xmlns:dcterms="http://purl.org/dc/terms/"' +
            ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
            ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
            ' xmlns:docbook="http://docbook.org/ns/docbook"' +
            ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
            ' xmlns:ting="http://www.dbc.dk/ting"' +
            ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
            '<dkabm:record>' +
            '<ac:identifier>22023578|870970</ac:identifier>' +
            '<ac:source>Bibliotekskatalog</ac:source>' +
/*  TITLE NOT PRESENT '<dc:title>Ave Femina!</dc:title>' + */
            '<dc:title xsi:type="dkdcplus:full">Ave Femina! : Digte</dc:title>' +
            '</dkabm:record>' +
            '</ting:localData>';

    localData = XmlUtil.fromString(localDataString);
    commonData = XmlUtil.fromString(commonDataString);

    Assert.exception( "Stop processing if dc has no title", function() {
        ManifestationInfo.getTitle( commonData, localData );
    }, Packages.dk.dbc.javascript.recordprocessing.FailRecord );

} );


UnitTest.addFixture( "ManifestationInfo.getFullTitle", function() {
    var commonDataString =
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
        '</ting:container>';

    var localDataString =
        '<ting:localData ' +
        'xmlns:marcx="info:lc/xmlns/marcxchange-v1" ' +
        'xmlns:ting="http://www.dbc.dk/ting">' +
        '<marcx:record format="danMARC2" type="BibliographicLocal">' +
        '<marcx:datafield ind1="0" ind2="0" tag="d08">' +
        '<marcx:subfield code="a">dj (lfu-post)</marcx:subfield>' +
        '</marcx:datafield>' +
        '</marcx:record>' +
        '</ting:localData>';

    var commonData = XmlUtil.fromString( commonDataString );
    var localData = XmlUtil.fromString( localDataString );

    var expected = "Værkstedstekniske beregninger. M2, Boring";

    Assert.equalValue( "get full title from common data", ManifestationInfo.getFullTitle( commonData, localData ), expected );

    var commonDataString =
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
        '<dc:title xsi:type="dkdcplus:full"> Værkstedstekniske beregninger. M2, Boring </dc:title>' +
        '</dkabm:record>' +
        '</ting:container>';

    var localDataString =
        '<ting:localData ' +
        'xmlns:marcx="info:lc/xmlns/marcxchange-v1" ' +
        'xmlns:ting="http://www.dbc.dk/ting">' +
        '<marcx:record format="danMARC2" type="BibliographicLocal">' +
        '<marcx:datafield ind1="0" ind2="0" tag="d08">' +
        '<marcx:subfield code="a">dj (lfu-post)</marcx:subfield>' +
        '</marcx:datafield>' +
        '</marcx:record>' +
        '</ting:localData>';

    var commonData = XmlUtil.fromString( commonDataString );
    var localData = XmlUtil.fromString( localDataString );

    var expected = "Værkstedstekniske beregninger. M2, Boring";

    Assert.equalValue( "get full title from common data with whitespace ", ManifestationInfo.getFullTitle( commonData, localData ), expected );

    commonDataString =
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
        '<ac:identifier>52568765|870970</ac:identifier>' +
        '<ac:source>Bibliotekskatalog</ac:source>' +
        '<dc:title>Helt ude i HESTEN</dc:title>' +
        '<dc:title xsi:type="dkdcplus:full">Helt ude i HESTEN : mails fra underklassen</dc:title>' +
        '<dc:creator xsi:type="dkdcplus:aut">Karina Pedersen (f. 1976)</dc:creator>' +
        '<dc:creator xsi:type="oss:sort">Pedersen, Karina (f. 1976)</dc:creator>' +
        '<dc:subject xsi:type="dkdcplus:DK5">30.1628</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">Karina Pedersen (f. 1976)</dc:subject>' +
        '<dc:subject xsi:type="oss:sort">Pedersen, Karina (f. 1976)</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DK5-Text">Underklasser</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">barndomserindringer</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">breve</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">erindringer</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">familier</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">mor-datter forholdet</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">socialt udsatte</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">underklassen</dc:subject>' +
        '<dcterms:abstract>I en række mails fortæller Karina Pedersen (f. 1976) historien om sin opvækst i Korskærparken i Fredericia. Med sin egen underklasse-familie som eksempel, tager hun et opgør med velfærdsdanmark og de sociale ydelser</dcterms:abstract>' +
        '<dcterms:audience>alment niveau</dcterms:audience>' +
        '<dcterms:audience>voksenmaterialer</dcterms:audience>' +
        '<dkdcplus:version>1. udgave, 1. oplag (2016)</dkdcplus:version>' +
        '<dc:publisher>Gyldendal</dc:publisher>' +
        '<dc:date>2016</dc:date>' +
        '<dc:type xsi:type="dkdcplus:BibDK-Type">Bog</dc:type>' +
        '<dcterms:extent>195 sider</dcterms:extent>' +
        '<dc:identifier xsi:type="dkdcplus:ISBN">9788702210170</dc:identifier>' +
        '<dc:language xsi:type="dcterms:ISO639-2">dan</dc:language>' +
        '<dc:language>Dansk</dc:language>' +
        '<dcterms:spatial xsi:type="dkdcplus:DBCF">Danmark</dcterms:spatial>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">1980-1989</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">1990-1999</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">2000-2009</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">2010-2019</dcterms:temporal>' +
        '</dkabm:record>' +
        '</ting:container>';


    localDataString =
        '<ting:localData ' +
        'xmlns:ac="http://biblstandard.dk/ac/namespace/" ' +
        'xmlns:dc="http://purl.org/dc/elements/1.1/" ' +
        'xmlns:dcterms="http://purl.org/dc/terms/" ' +
        'xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/" ' +
        'xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/" ' +
        'xmlns:docbook="http://docbook.org/ns/docbook" ' +
        'xmlns:oss="http://oss.dbc.dk/ns/osstypes" ' +
        'xmlns:ting="http://www.dbc.dk/ting" ' +
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"> ' +
        '<dkabm:record>' +
        '<ac:identifier>52568765|761500</ac:identifier>' +
        '<ac:source>Bibliotekskatalog</ac:source>' +
        '<dc:title>Helt ude i hampen</dc:title>' +
        '<dc:title xsi:type="dkdcplus:full">Helt ude i hampen : mails fra underklassen</dc:title>' +
        '<dc:creator xsi:type="dkdcplus:aut">Karina Pedersen (f. 1976)</dc:creator>' +
        '<dc:creator xsi:type="oss:sort">Pedersen, Karina (f. 1976)</dc:creator>' +
        '<dc:subject xsi:type="dkdcplus:DK5">99.4 Pedersen, Karina, f. 1976</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DK5-Text">Biografier af enkelte personer</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">Karina Pedersen (f. 1976)</dc:subject>' +
        '<dc:subject xsi:type="oss:sort">Pedersen, Karina (f. 1976)</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">barndomserindringer</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">breve</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">erindringer</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">familier</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">mor-datter forholdet</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">socialt udsatte</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">underklassen</dc:subject>' +
        '<dcterms:abstract>I en række mails fortæller Karina Pedersen (f. 1976) historien om sin opvækst i Korskærparken i Fredericia. Med sin egen underklasse-familie som eksempel, tager hun et opgør med velfærdsdanmark og de sociale ydelser</dcterms:abstract>' +
        '<dcterms:audience>alment niveau</dcterms:audience>' +
        '<dcterms:audience>voksenmaterialer</dcterms:audience>' +
        '<dkdcplus:version>1. udgave, 1. oplag (2016)</dkdcplus:version>' +
        '<dc:publisher>Gyldendal</dc:publisher>' +
        '<dc:date>2016</dc:date>' +
        '<dc:type xsi:type="dkdcplus:BibDK-Type">Bog</dc:type>' +
        '<dcterms:extent>195 sider</dcterms:extent>' +
        '<dc:identifier xsi:type="dkdcplus:ISBN">9788702210170</dc:identifier>' +
        '<dc:language xsi:type="dcterms:ISO639-2">dan</dc:language>' +
        '<dc:language>Dansk</dc:language>' +
        '<dcterms:spatial xsi:type="dkdcplus:DBCF">Danmark</dcterms:spatial>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">1980-1989</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">1990-1999</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">2000-2009</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">2010-2019</dcterms:temporal>' +
        '</dkabm:record>' +
        '</ting:localData>';

    commonData = XmlUtil.fromString( commonDataString );
    localData = XmlUtil.fromString( localDataString );

    expected = "Helt ude i hampen : mails fra underklassen";

    Assert.equalValue( "get full title from common or local data ", ManifestationInfo.getFullTitle( commonData, localData ), expected );


    commonDataString =
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
        '<ac:identifier>52568765|870970</ac:identifier>' +
        '<ac:source>Bibliotekskatalog</ac:source>' +
        '<dc:title>Helt ude i HESTEN</dc:title>' +
        '<dc:title xsi:type="dkdcplus:full">Helt ude i HESTEN : mails fra underklassen</dc:title>' +
        '<dc:creator xsi:type="dkdcplus:aut">Karina Pedersen (f. 1976)</dc:creator>' +
        '<dc:creator xsi:type="oss:sort">Pedersen, Karina (f. 1976)</dc:creator>' +
        '<dc:subject xsi:type="dkdcplus:DK5">30.1628</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">Karina Pedersen (f. 1976)</dc:subject>' +
        '<dc:subject xsi:type="oss:sort">Pedersen, Karina (f. 1976)</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DK5-Text">Underklasser</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">barndomserindringer</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">breve</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">erindringer</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">familier</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">mor-datter forholdet</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">socialt udsatte</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">underklassen</dc:subject>' +
        '<dcterms:abstract>I en række mails fortæller Karina Pedersen (f. 1976) historien om sin opvækst i Korskærparken i Fredericia. Med sin egen underklasse-familie som eksempel, tager hun et opgør med velfærdsdanmark og de sociale ydelser</dcterms:abstract>' +
        '<dcterms:audience>alment niveau</dcterms:audience>' +
        '<dcterms:audience>voksenmaterialer</dcterms:audience>' +
        '<dkdcplus:version>1. udgave, 1. oplag (2016)</dkdcplus:version>' +
        '<dc:publisher>Gyldendal</dc:publisher>' +
        '<dc:date>2016</dc:date>' +
        '<dc:type xsi:type="dkdcplus:BibDK-Type">Bog</dc:type>' +
        '<dcterms:extent>195 sider</dcterms:extent>' +
        '<dc:identifier xsi:type="dkdcplus:ISBN">9788702210170</dc:identifier>' +
        '<dc:language xsi:type="dcterms:ISO639-2">dan</dc:language>' +
        '<dc:language>Dansk</dc:language>' +
        '<dcterms:spatial xsi:type="dkdcplus:DBCF">Danmark</dcterms:spatial>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">1980-1989</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">1990-1999</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">2000-2009</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">2010-2019</dcterms:temporal>' +
        '</dkabm:record>' +
        '</ting:container>';


    localDataString =
        '<ting:localData ' +
        'xmlns:ac="http://biblstandard.dk/ac/namespace/" ' +
        'xmlns:dc="http://purl.org/dc/elements/1.1/" ' +
        'xmlns:dcterms="http://purl.org/dc/terms/" ' +
        'xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/" ' +
        'xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/" ' +
        'xmlns:docbook="http://docbook.org/ns/docbook" ' +
        'xmlns:oss="http://oss.dbc.dk/ns/osstypes" ' +
        'xmlns:ting="http://www.dbc.dk/ting" ' +
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"> ' +
        '<dkabm:record>' +
        '<ac:identifier>52568765|761500</ac:identifier>' +
        '<ac:source>Bibliotekskatalog</ac:source>' +
        '<dc:title>Helt ude i hampen</dc:title>' +
        '<dc:title xsi:type="dkdcplus:full"> Helt ude i hampen : mails fra underklassen </dc:title>' +
        '<dc:creator xsi:type="dkdcplus:aut">Karina Pedersen (f. 1976)</dc:creator>' +
        '<dc:creator xsi:type="oss:sort">Pedersen, Karina (f. 1976)</dc:creator>' +
        '<dc:subject xsi:type="dkdcplus:DK5">99.4 Pedersen, Karina, f. 1976</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DK5-Text">Biografier af enkelte personer</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">Karina Pedersen (f. 1976)</dc:subject>' +
        '<dc:subject xsi:type="oss:sort">Pedersen, Karina (f. 1976)</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">barndomserindringer</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">breve</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">erindringer</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">familier</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">mor-datter forholdet</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">socialt udsatte</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">underklassen</dc:subject>' +
        '<dcterms:abstract>I en række mails fortæller Karina Pedersen (f. 1976) historien om sin opvækst i Korskærparken i Fredericia. Med sin egen underklasse-familie som eksempel, tager hun et opgør med velfærdsdanmark og de sociale ydelser</dcterms:abstract>' +
        '<dcterms:audience>alment niveau</dcterms:audience>' +
        '<dcterms:audience>voksenmaterialer</dcterms:audience>' +
        '<dkdcplus:version>1. udgave, 1. oplag (2016)</dkdcplus:version>' +
        '<dc:publisher>Gyldendal</dc:publisher>' +
        '<dc:date>2016</dc:date>' +
        '<dc:type xsi:type="dkdcplus:BibDK-Type">Bog</dc:type>' +
        '<dcterms:extent>195 sider</dcterms:extent>' +
        '<dc:identifier xsi:type="dkdcplus:ISBN">9788702210170</dc:identifier>' +
        '<dc:language xsi:type="dcterms:ISO639-2">dan</dc:language>' +
        '<dc:language>Dansk</dc:language>' +
        '<dcterms:spatial xsi:type="dkdcplus:DBCF">Danmark</dcterms:spatial>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">1980-1989</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">1990-1999</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">2000-2009</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">2010-2019</dcterms:temporal>' +
        '</dkabm:record>' +
        '</ting:localData>';

    commonData = XmlUtil.fromString( commonDataString );
    localData = XmlUtil.fromString( localDataString );

    expected = "Helt ude i hampen : mails fra underklassen";

    Assert.equalValue( "get full title from common or local data with whitespace ", ManifestationInfo.getFullTitle( commonData, localData ), expected );


    commonDataString =
        '<ting:container ' +
        'xmlns:ting="http://www.dbc.dk/ting">' +
        '</ting:container>';

    localDataString =
        '<ting:localData ' +
        'xmlns:ting="http://www.dbc.dk/ting">' +
        '</ting:localData>';

    commonData = XmlUtil.fromString( commonDataString );
    localData = XmlUtil.fromString( localDataString );

    Assert.exception( "Stop processing if dc has no title", function() {
        ManifestationInfo.getFullTitle( commonData, localData );
    }, Packages.dk.dbc.javascript.recordprocessing.FailRecord );

} );


UnitTest.addFixture( "ManifestationInfo.getSeries", function() {

    var commonDataString = '<empty/>';
    var localDataString =
        '<ting:localData' +
        ' xmlns:ting="http://www.dbc.dk/ting"' +
        ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
        ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
        ' xmlns:dcterms="http://purl.org/dc/terms/"' +
        ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
        ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
        ' xmlns:docbook="http://docbook.org/ns/docbook"' +
        ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
        ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
        '<dkabm:record>' +
        '<ac:identifier>25912233|870970</ac:identifier>' +
        '<ac:source>Bibliotekskatalog</ac:source>' +
        '<dc:title>Djævelens lærling</dc:title>' +
        '<dc:title xsi:type="dkdcplus:full">Djævelens lærling</dc:title>' +
        '<dc:title xsi:type="dkdcplus:series">Den store djævlekrig ; 1</dc:title>' +
        '<dc:creator xsi:type="dkdcplus:aut">Kenneth Bøgh Andersen</dc:creator>' +
        '<dc:creator xsi:type="oss:sort">Bøgh Andersen, Kenneth</dc:creator>' +
        '</dkabm:record>' +
        '</ting:localData>';

    var commonData = XmlUtil.fromString( commonDataString );
    var localData = XmlUtil.fromString( localDataString );

    var expected = {
        "title": "Den store djævlekrig",
        "instalment": "1"
    };

    Assert.equalValue( "get series title from localData ", ManifestationInfo.getSeries( commonData, localData ), expected );


    var commonDataString = '<empty/>';
    var localDataString =
        '<ting:localData' +
        ' xmlns:ting="http://www.dbc.dk/ting"' +
        ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
        ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
        ' xmlns:dcterms="http://purl.org/dc/terms/"' +
        ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
        ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
        ' xmlns:docbook="http://docbook.org/ns/docbook"' +
        ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
        ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
        '<dkabm:record>' +
        '<ac:identifier>22375733|870970</ac:identifier>' +
        '<ac:source>Bibliotekskatalog</ac:source>' +
        '<dc:title>Harry Potter og Hemmelighedernes Kammer</dc:title>' +
        '<dc:title xsi:type="dkdcplus:full">Harry Potter og Hemmelighedernes Kammer</dc:title>' +
        '<dcterms:abstract>Fantasy. Den 12-årige Harry Potter har trolddomsevner. Derfor er han blevet optaget på troldmandsskolen Hogwarts, som ligger i en parallelverden. Men nu indtræffer der uhyggelige og mystiske hændelser på troldmandsskolen</dcterms:abstract>' +
        '<dc:description xsi:type="dkdcplus:series">2. del af: Harry Potter og De Vises Sten</dc:description>' +
        '</dkabm:record>' +
        '</ting:localData>';


    var commonData = XmlUtil.fromString( commonDataString );
    var localData = XmlUtil.fromString( localDataString );

    var expected = {
        "title": "2. del af: Harry Potter og De Vises Sten",
        "instalment": null
    };

    Assert.equalValue( "get series description from localData ", ManifestationInfo.getSeries( commonData, localData ), expected );


    var commonDataString =
        '<ting:container' +
        ' xmlns:ting="http://www.dbc.dk/ting"' +
        ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
        ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
        ' xmlns:dcterms="http://purl.org/dc/terms/"' +
        ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
        ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
        ' xmlns:docbook="http://docbook.org/ns/docbook"' +
        ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
        ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
        '<dkabm:record>' +
        '<ac:identifier>25912233|870970</ac:identifier>' +
        '<ac:source>Bibliotekskatalog</ac:source>' +
        '<dc:title>Djævelens lærling</dc:title>' +
        '<dc:title xsi:type="dkdcplus:full">Djævelens lærling</dc:title>' +
        '<dc:title xsi:type="dkdcplus:series">Den store djævlekrig ; 1</dc:title>' +
        '<dc:creator xsi:type="dkdcplus:aut">Kenneth Bøgh Andersen</dc:creator>' +
        '<dc:creator xsi:type="oss:sort">Bøgh Andersen, Kenneth</dc:creator>' +
        '</dkabm:record>' +
        '</ting:container>';
    var localDataString = '<empty/>';
        
    var commonData = XmlUtil.fromString( commonDataString );
    var localData = XmlUtil.fromString( localDataString );

    var expected = {
        "title": "Den store djævlekrig",
        "instalment": "1"
    };

    Assert.equalValue( "get series title from commonData ", ManifestationInfo.getSeries( commonData, localData ), expected );


    var commonDataString =
        '<ting:container' +
        ' xmlns:ting="http://www.dbc.dk/ting"' +
        ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
        ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
        ' xmlns:dcterms="http://purl.org/dc/terms/"' +
        ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
        ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
        ' xmlns:docbook="http://docbook.org/ns/docbook"' +
        ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
        ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
        '<dkabm:record>' +
        '<ac:identifier>22375733|870970</ac:identifier>' +
        '<ac:source>Bibliotekskatalog</ac:source>' +
        '<dc:title>Harry Potter og Hemmelighedernes Kammer</dc:title>' +
        '<dc:title xsi:type="dkdcplus:full">Harry Potter og Hemmelighedernes Kammer</dc:title>' +
        '<dcterms:abstract>Fantasy. Den 12-årige Harry Potter har trolddomsevner. Derfor er han blevet optaget på troldmandsskolen Hogwarts, som ligger i en parallelverden. Men nu indtræffer der uhyggelige og mystiske hændelser på troldmandsskolen</dcterms:abstract>' +
        '<dc:description xsi:type="dkdcplus:series">2. del af: Harry Potter og De Vises Sten</dc:description>' +
        '</dkabm:record>' +
        '</ting:container>';
    var localDataString = '<empty/>';


    var commonData = XmlUtil.fromString( commonDataString );
    var localData = XmlUtil.fromString( localDataString );

    var expected = {
        "title": "2. del af: Harry Potter og De Vises Sten",
        "instalment": null
    };

    Assert.equalValue( "get series description from commonData ", ManifestationInfo.getSeries( commonData, localData ), expected );


    var commonDataString =
        '<ting:container' +
        ' xmlns:ting="http://www.dbc.dk/ting"' +
        ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
        ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
        ' xmlns:dcterms="http://purl.org/dc/terms/"' +
        ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
        ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
        ' xmlns:docbook="http://docbook.org/ns/docbook"' +
        ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
        ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
        '<dkabm:record>' +
        '<ac:identifier>25912233|870970</ac:identifier>' +
        '<ac:source>Bibliotekskatalog</ac:source>' +
        '<dc:title>Djævelens lærling</dc:title>' +
        '<dc:title xsi:type="dkdcplus:full">Djævelens lærling</dc:title>' +
        '<dc:title xsi:type="dkdcplus:series">NOT THIS Den store djævlekrig ; 1</dc:title>' +
        '<dc:creator xsi:type="dkdcplus:aut">Kenneth Bøgh Andersen</dc:creator>' +
        '<dc:creator xsi:type="oss:sort">Bøgh Andersen, Kenneth</dc:creator>' +
        '</dkabm:record>' +
        '</ting:container>';
    var localDataString = 
        '<ting:localData' +
        ' xmlns:ting="http://www.dbc.dk/ting"' +
        ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
        ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
        ' xmlns:dcterms="http://purl.org/dc/terms/"' +
        ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
        ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
        ' xmlns:docbook="http://docbook.org/ns/docbook"' +
        ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
        ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
        '<dkabm:record>' +
        '<ac:identifier>25912233|870970</ac:identifier>' +
        '<ac:source>Bibliotekskatalog</ac:source>' +
        '<dc:title>Djævelens lærling</dc:title>' +
        '<dc:title xsi:type="dkdcplus:full">Djævelens lærling</dc:title>' +
        '<dc:title xsi:type="dkdcplus:series">THIS Den store djævlekrig ; 1</dc:title>' +
        '<dc:creator xsi:type="dkdcplus:aut">Kenneth Bøgh Andersen</dc:creator>' +
        '<dc:creator xsi:type="oss:sort">Bøgh Andersen, Kenneth</dc:creator>' +
        '</dkabm:record>' +
        '</ting:localData>';

    var commonData = XmlUtil.fromString( commonDataString );
    var localData = XmlUtil.fromString( localDataString );

    var expected = {
        "title": "THIS Den store djævlekrig",
        "instalment": "1"
    };

    Assert.equalValue( "get series title from localData when both local and commonData is present ", ManifestationInfo.getSeries( commonData, localData ), expected );


    var commonDataString = '<empty/>';
    var localDataString =  '<empty/>';

    var commonData = XmlUtil.fromString( commonDataString );
    var localData = XmlUtil.fromString( localDataString );

    var expected = null;

    Assert.equal( "get series title from common or local - none found ", ManifestationInfo.getSeries( commonData, localData ), expected );


    var commonDataString = '<empty/>';
    var localDataString = 
        '<ting:localData' +
        ' xmlns:ting="http://www.dbc.dk/ting"' +
        ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
        ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
        ' xmlns:dcterms="http://purl.org/dc/terms/"' +
        ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
        ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
        ' xmlns:docbook="http://docbook.org/ns/docbook"' +
        ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
        ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
        '<dkabm:record>' +
        '<ac:identifier>25912233|870970</ac:identifier>' +
        '<ac:source>Bibliotekskatalog</ac:source>' +
        '<dc:title>Djævelens lærling</dc:title>' +
        '<dc:title xsi:type="dkdcplus:full">Djævelens lærling</dc:title>' +
        '<dc:title xsi:type="dkdcplus:series">Den store djævlekrig</dc:title>' +
        '<dc:creator xsi:type="dkdcplus:aut">Kenneth Bøgh Andersen</dc:creator>' +
        '<dc:creator xsi:type="oss:sort">Bøgh Andersen, Kenneth</dc:creator>' +
        '</dkabm:record>' +
        '</ting:localData>';

    var commonData = XmlUtil.fromString( commonDataString );
    var localData = XmlUtil.fromString( localDataString );

    var expected = {
        "title": "Den store djævlekrig",
        "instalment": null
    };

    Assert.equalValue( "get series title from common or local - with no instalment ", ManifestationInfo.getSeries( commonData, localData ), expected );

} );


UnitTest.addFixture( "ManifestationInfo.getCreators", function() {

    var commonDataString = '<empty/>';
    var localDataString =
            '<ting:localData' +
            ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
            ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
            ' xmlns:dcterms="http://purl.org/dc/terms/"' +
            ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
            ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
            ' xmlns:docbook="http://docbook.org/ns/docbook"' +
            ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
            ' xmlns:ting="http://www.dbc.dk/ting"' +
            ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
            '<dkabm:record>' +
            '<ac:identifier>54969562|700400</ac:identifier>' +
            '<ac:source>Bibliotekskatalog</ac:source>' +
            '<dc:title>Vildheks</dc:title>' +
            '<dc:title xsi:type="dkdcplus:full">Vildheks</dc:title>' +
            '<dcterms:alternative>Vildheks</dcterms:alternative>' +
            '<dc:creator xsi:type="dkdcplus:aus">Kaspar Munk</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Munk, Kaspar</dc:creator>' +
            '<dc:creator xsi:type="dkdcplus:drt">Kaspar Munk</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Munk, Kaspar</dc:creator>' +
            '<dc:creator xsi:type="dkdcplus:cng">Adam Wallensten</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Wallensten, Adam</dc:creator>' +
            '<dc:creator xsi:type="dkdcplus:aus">Poul Berg</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Berg, Poul (f. 1970-08-19)</dc:creator>' +
            '<dc:creator xsi:type="dkdcplus:aus">Bo hr. Hansen</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Hansen, Bo hr. (f. 1961)</dc:creator>' +
            '</dkabm:record>' +
            '</ting:localData>';

    var localData = XmlUtil.fromString( localDataString );
    var commonData = XmlUtil.fromString( commonDataString );

    var expected = [
        { type : "aus", value : "Kaspar Munk" },
        { type : "drt", value : "Kaspar Munk" },
        { type : "cng", value : "Adam Wallensten" },
        { type : "aus", value : "Poul Berg" },
        { type : "aus", value : "Bo hr. Hansen" }
    ];

    Assert.equalValue( "get creators from localData ", ManifestationInfo.getCreators( commonData, localData ), expected );

    commonDataString =
            '<ting:container' +
            ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
            ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
            ' xmlns:dcterms="http://purl.org/dc/terms/"' +
            ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
            ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
            ' xmlns:docbook="http://docbook.org/ns/docbook"' +
            ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
            ' xmlns:ting="http://www.dbc.dk/ting"' +
            ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
            '<dkabm:record>' +
            '<ac:identifier>54969562|700400</ac:identifier>' +
            '<ac:source>Bibliotekskatalog</ac:source>' +
            '<dc:title>Vildheks</dc:title>' +
            '<dc:title xsi:type="dkdcplus:full">Vildheks</dc:title>' +
            '<dcterms:alternative>Vildheks</dcterms:alternative>' +
            '<dc:creator xsi:type="dkdcplus:aus">Kaspar Munk</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Munk, Kaspar</dc:creator>' +
            '<dc:creator xsi:type="dkdcplus:drt">Kaspar Munk</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Munk, Kaspar</dc:creator>' +
            '<dc:creator xsi:type="dkdcplus:cng">Adam Wallensten</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Wallensten, Adam</dc:creator>' +
            '<dc:creator xsi:type="dkdcplus:aus">Poul Berg</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Berg, Poul (f. 1970-08-19)</dc:creator>' +
            '<dc:creator xsi:type="dkdcplus:aus">Bo hr. Hansen</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Hansen, Bo hr. (f. 1961)</dc:creator>' +
            '</dkabm:record>' +
            '</ting:container>';
    localDataString = '<empty/>';

    localData = XmlUtil.fromString( localDataString );
    commonData = XmlUtil.fromString( commonDataString );

    expected = [
        { type : "aus", value : "Kaspar Munk" },
        { type : "drt", value : "Kaspar Munk" },
        { type : "cng", value : "Adam Wallensten" },
        { type : "aus", value : "Poul Berg" },
        { type : "aus", value : "Bo hr. Hansen" }
    ];

    Assert.equalValue( "get creators from commonData ", ManifestationInfo.getCreators( commonData, localData ), expected );

    commonDataString =
            '<ting:container' +
            ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
            ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
            ' xmlns:dcterms="http://purl.org/dc/terms/"' +
            ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
            ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
            ' xmlns:docbook="http://docbook.org/ns/docbook"' +
            ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
            ' xmlns:ting="http://www.dbc.dk/ting"' +
            ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
            '<dkabm:record>' +
            '<ac:identifier>54969562|700400</ac:identifier>' +
            '<ac:source>Bibliotekskatalog</ac:source>' +
            '<dc:title>Vildheks</dc:title>' +
            '<dc:title xsi:type="dkdcplus:full">Vildheks</dc:title>' +
            '<dcterms:alternative>Vildheks</dcterms:alternative>' +
            '<dc:creator xsi:type="dkdcplus:cng">Adam Wallensten</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Wallensten, Adam</dc:creator>' +
            '<dc:creator xsi:type="dkdcplus:aus">Poul Berg</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Berg, Poul (f. 1970-08-19)</dc:creator>' +
            '<dc:creator xsi:type="dkdcplus:aus">Bo hr. Hansen</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Hansen, Bo hr. (f. 1961)</dc:creator>' +
            '</dkabm:record>' +
            '</ting:container>';
    localDataString =
            '<ting:localData' +
            ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
            ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
            ' xmlns:dcterms="http://purl.org/dc/terms/"' +
            ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
            ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
            ' xmlns:docbook="http://docbook.org/ns/docbook"' +
            ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
            ' xmlns:ting="http://www.dbc.dk/ting"' +
            ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
            '<dkabm:record>' +
            '<ac:identifier>54969562|700400</ac:identifier>' +
            '<ac:source>Bibliotekskatalog</ac:source>' +
            '<dc:title>Vildheks</dc:title>' +
            '<dc:title xsi:type="dkdcplus:full">Vildheks</dc:title>' +
            '<dcterms:alternative>Vildheks</dcterms:alternative>' +
            '<dc:creator xsi:type="dkdcplus:aus">Kaspar Munk</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Munk, Kaspar</dc:creator>' +
            '<dc:creator xsi:type="dkdcplus:drt">Kaspar Munk</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Munk, Kaspar</dc:creator>' +
            '</dkabm:record>' +
            '</ting:localData>';

    localData = XmlUtil.fromString( localDataString );
    commonData = XmlUtil.fromString( commonDataString );

    expected = [
        { type : "aus", value : "Kaspar Munk" },
        { type : "drt", value : "Kaspar Munk" }
    ];

    Assert.equalValue( "get creators from localData over commonData ", ManifestationInfo.getCreators( commonData, localData ), expected );

    var commonDataString = '<empty/>';
    var localDataString =
            '<ting:localData' +
            ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
            ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
            ' xmlns:dcterms="http://purl.org/dc/terms/"' +
            ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
            ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
            ' xmlns:docbook="http://docbook.org/ns/docbook"' +
            ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
            ' xmlns:ting="http://www.dbc.dk/ting"' +
            ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
            '<dkabm:record>' +
            '<ac:identifier>54969562|700400</ac:identifier>' +
            '<ac:source>Bibliotekskatalog</ac:source>' +
            '<dc:title>Vildheks</dc:title>' +
            '<dc:title xsi:type="dkdcplus:full">Vildheks</dc:title>' +
            '<dcterms:alternative>Vildheks</dcterms:alternative>' +
            '<dc:creator xsi:type="">Kaspar Munk</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Munk, Kaspar</dc:creator>' +
            '<dc:creator>Kaspar Munk</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Munk, Kaspar</dc:creator>' +
            '</dkabm:record>' +
            '</ting:localData>';

    var localData = XmlUtil.fromString( localDataString );
    var commonData = XmlUtil.fromString( commonDataString );

    var expected = [
        { type : null, value : "Kaspar Munk" },
        { type : null, value : "Kaspar Munk" }
    ];

    Assert.equalValue( "get creators with no type ", ManifestationInfo.getCreators( commonData, localData ), expected );

} );


UnitTest.addFixture( "ManifestationInfo.getTypes", function() {

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

    var expected = [ "Bog" ];

    Assert.equalValue( "get one type from dc stream ", ManifestationInfo.getTypes( dcStream ), expected );

    var dcStreamString =
        '<oai_dc:dc xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
        '<dc:title>værkstedstekniske beregninger</dc:title>' +
        '<dc:language>Dansk</dc:language>' +
        '<dc:type> Bog </dc:type>' +
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

    var expected = [ "Bog" ];

    Assert.equalValue( "get one type from dc stream with whitespace ", ManifestationInfo.getTypes( dcStream ), expected );

    dcStreamString =
        '<oai_dc:dc ' +
        'xmlns:dc="http://purl.org/dc/elements/1.1/" ' +
        'xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" ' +
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
        '<dc:title>askepot</dc:title>' +
        '<dc:title>MATCHSTRING:askepotaskepot</dc:title>' +
        '<dc:title>MATCH:askepo</dc:title>' +
        '<dc:language>Dansk</dc:language>' +
        '<dc:subject>eventyr</dc:subject>' +
        '<dc:type>Lyd (cd)</dc:type>' +
        '<dc:type>Bog</dc:type>' +
        '<dc:type>Sammensat materiale</dc:type>' +
        '<dc:type>WORK:literature</dc:type>' +
        '<dc:publisher>sesam</dc:publisher>' +
        '<dc:publisher>MATCHSTRING:sesam</dc:publisher>' +
        '<dc:contributor>walt disney firma</dc:contributor>' +
        '<dc:contributor>MATCHSTRING:walt disney</dc:contributor>' +
        '<dc:source>cinderella</dc:source>' +
        '<dc:date>2004</dc:date>' +
        '<dc:identifier>870970-basis:25503244</dc:identifier>' +
        '<dc:identifier>ISBN:8711213175</dc:identifier>' +
        '<dc:identifier>MATCH:ISBN:8711213175</dc:identifier>' +
        '<dc:identifier>NUMBER:1043-150</dc:identifier>' +
        '</oai_dc:dc>';


    dcStream = XmlUtil.fromString( dcStreamString );

    expected = [ "Lyd (cd)", "Bog" ];

    Assert.equalValue( "get multiple types  but no sammensat ", ManifestationInfo.getTypes( dcStream ), expected );


    dcStreamString =
        '<oai_dc:dc ' +
        'xmlns:dc="http://purl.org/dc/elements/1.1/" ' +
        'xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" ' +
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
        '<dc:title>askepot</dc:title>' +
        '<dc:type>Sammensat materiale</dc:type>' +
        '<dc:type>WORK:literature</dc:type>' +
        '<dc:publisher>sesam</dc:publisher>' +
        '<dc:publisher>MATCHSTRING:sesam</dc:publisher>' +
        '<dc:contributor>walt disney firma</dc:contributor>' +
        '</oai_dc:dc>';


    dcStream = XmlUtil.fromString( dcStreamString );

    expected = [ "Sammensat materiale" ];

    Assert.equalValue( "get type sammensat if its the only one (constructed) ", ManifestationInfo.getTypes( dcStream ), expected );

    dcStreamString =
        '<oai_dc:dc xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
        '<dc:title>vildheks</dc:title>' +
        '<dc:title>MATCHSTRING:vildheksildproevenbind1oevenbind1</dc:title>' +
        '<dc:title>MATCH:vildhe</dc:title>' +
        '<dc:creator>lene kꜳberbøl</dc:creator>' +
        '<dc:creator>NOBIRTH:lene kꜳberbøl</dc:creator>' +
        '<dc:creator>MATCHSTRING:kꜳberbøll</dc:creator>' +
        '<dc:creator>MATCHSTRING:kꜳberbøll</dc:creator>' +
        '<dc:language>Dansk</dc:language>' +
        '<dc:subject>dyr</dc:subject>' +
        '<dc:subject>fantasy</dc:subject>' +
        '<dc:subject>for 10 år</dc:subject>' +
        '<dc:subject>for 11 år</dc:subject>' +
        '<dc:subject>for 12 år</dc:subject>' +
        '<dc:subject>for 13 år</dc:subject>' +
        '<dc:subject>for 14 år</dc:subject>' +
        '<dc:subject>hekse</dc:subject>' +
        '<dc:subject>piger</dc:subject>' +
        '<dc:type>Bog</dc:type>' +
        '<dc:type>WORK:literature</dc:type>' +
        '<dc:type>RECORD:multivolume</dc:type>' +
        '<dc:type>RECORD:vol1</dc:type>' +
        '</oai_dc:dc>';

    dcStream = XmlUtil.fromString( dcStreamString );

    expected = [ "Bog" ];

    Assert.equalValue( "get one type from dc stream, no volume infofrom type ", ManifestationInfo.getTypes( dcStream ), expected );

    dcStreamString =
        '<oai_dc:dc ' +
        'xmlns:dc="http://purl.org/dc/elements/1.1/" ' +
        'xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" ' +
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
        '</oai_dc:dc>';

    dcStream = XmlUtil.fromString( dcStreamString );

    expected = [ ];

    Assert.equalValue( "accept no types ", ManifestationInfo.getTypes( dcStream ), expected );

} );


UnitTest.addFixture( "ManifestationInfo.getAbstract", function() {
    var commonDataString =
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
        '</ting:container>';

    var localDataString =
        '<ting:localData ' +
        'xmlns:marcx="info:lc/xmlns/marcxchange-v1" ' +
        'xmlns:ting="http://www.dbc.dk/ting">' +
        '<marcx:record format="danMARC2" type="BibliographicLocal">' +
        '<marcx:datafield ind1="0" ind2="0" tag="d08">' +
        '<marcx:subfield code="a">dj (lfu-post)</marcx:subfield>' +
        '</marcx:datafield>' +
        '</marcx:record>' +
        '</ting:localData>';

    var commonData = XmlUtil.fromString( commonDataString );
    var localData = XmlUtil.fromString( localDataString );

    var expected = null;

    Assert.equalValue( "get abstract - no abstract in common or local data", ManifestationInfo.getAbstract( commonData, localData ), expected );

    commonDataString =
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
        '<ac:identifier>52568765|870970</ac:identifier>' +
        '<ac:source>Bibliotekskatalog</ac:source>' +
        '<dc:title>Helt ude i HESTEN</dc:title>' +
        '<dc:title xsi:type="dkdcplus:full">Helt ude i hampen : mails fra underklassen</dc:title>' +
        '<dc:creator xsi:type="dkdcplus:aut">Karina Pedersen (f. 1976)</dc:creator>' +
        '<dc:creator xsi:type="oss:sort">Pedersen, Karina (f. 1976)</dc:creator>' +
        '<dc:subject xsi:type="dkdcplus:DK5">30.1628</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">Karina Pedersen (f. 1976)</dc:subject>' +
        '<dc:subject xsi:type="oss:sort">Pedersen, Karina (f. 1976)</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DK5-Text">Underklasser</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">barndomserindringer</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">breve</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">erindringer</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">familier</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">mor-datter forholdet</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">socialt udsatte</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">underklassen</dc:subject>' +
        '<dcterms:abstract>I en række mails fortæller Karina Pedersen (f. 1976) historien om sin opvækst i Korskærparken i Fredericia. Med sin egen underklasse-familie som eksempel, tager hun et opgør med velfærdsdanmark og de sociale ydelser</dcterms:abstract>' +
        '<dcterms:audience>alment niveau</dcterms:audience>' +
        '<dcterms:audience>voksenmaterialer</dcterms:audience>' +
        '<dkdcplus:version>1. udgave, 1. oplag (2016)</dkdcplus:version>' +
        '<dc:publisher>Gyldendal</dc:publisher>' +
        '<dc:date>2016</dc:date>' +
        '<dc:type xsi:type="dkdcplus:BibDK-Type">Bog</dc:type>' +
        '<dcterms:extent>195 sider</dcterms:extent>' +
        '<dc:identifier xsi:type="dkdcplus:ISBN">9788702210170</dc:identifier>' +
        '<dc:language xsi:type="dcterms:ISO639-2">dan</dc:language>' +
        '<dc:language>Dansk</dc:language>' +
        '<dcterms:spatial xsi:type="dkdcplus:DBCF">Danmark</dcterms:spatial>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">1980-1989</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">1990-1999</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">2000-2009</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">2010-2019</dcterms:temporal>' +
        '</dkabm:record>' +
        '</ting:container>';


    localDataString =
        '<ting:localData ' +
        'xmlns:ac="http://biblstandard.dk/ac/namespace/" ' +
        'xmlns:dc="http://purl.org/dc/elements/1.1/" ' +
        'xmlns:dcterms="http://purl.org/dc/terms/" ' +
        'xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/" ' +
        'xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/" ' +
        'xmlns:docbook="http://docbook.org/ns/docbook" ' +
        'xmlns:oss="http://oss.dbc.dk/ns/osstypes" ' +
        'xmlns:ting="http://www.dbc.dk/ting" ' +
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"> ' +
        '<dkabm:record>' +
        '<ac:identifier>52568765|761500</ac:identifier>' +
        '<ac:source>Bibliotekskatalog</ac:source>' +
        '<dc:title>Helt ude i hampen</dc:title>' +
        '<dc:title xsi:type="dkdcplus:full">Helt ude i hampen : mails fra underklassen</dc:title>' +
        '<dc:creator xsi:type="dkdcplus:aut">Karina Pedersen (f. 1976)</dc:creator>' +
        '<dc:creator xsi:type="oss:sort">Pedersen, Karina (f. 1976)</dc:creator>' +
        '<dc:subject xsi:type="dkdcplus:DK5">99.4 Pedersen, Karina, f. 1976</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DK5-Text">Biografier af enkelte personer</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">Karina Pedersen (f. 1976)</dc:subject>' +
        '<dc:subject xsi:type="oss:sort">Pedersen, Karina (f. 1976)</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">barndomserindringer</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">breve</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">erindringer</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">familier</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">mor-datter forholdet</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">socialt udsatte</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">underklassen</dc:subject>' +
//        '<dcterms:abstract>I en række mails fortæller Karina Pedersen (f. 1976) historien om sin opvækst i Korskærparken i Fredericia. Med sin egen underklasse-familie som eksempel, tager hun et opgør med velfærdsdanmark og de sociale ydelser</dcterms:abstract>' +
        '<dcterms:audience>alment niveau</dcterms:audience>' +
        '<dcterms:audience>voksenmaterialer</dcterms:audience>' +
        '<dkdcplus:version>1. udgave, 1. oplag (2016)</dkdcplus:version>' +
        '<dc:publisher>Gyldendal</dc:publisher>' +
        '<dc:date>2016</dc:date>' +
        '<dc:type xsi:type="dkdcplus:BibDK-Type">Bog</dc:type>' +
        '<dcterms:extent>195 sider</dcterms:extent>' +
        '<dc:identifier xsi:type="dkdcplus:ISBN">9788702210170</dc:identifier>' +
        '<dc:language xsi:type="dcterms:ISO639-2">dan</dc:language>' +
        '<dc:language>Dansk</dc:language>' +
        '<dcterms:spatial xsi:type="dkdcplus:DBCF">Danmark</dcterms:spatial>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">1980-1989</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">1990-1999</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">2000-2009</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">2010-2019</dcterms:temporal>' +
        '</dkabm:record>' +
        '</ting:localData>';

    commonData = XmlUtil.fromString( commonDataString );
    localData = XmlUtil.fromString( localDataString );

    expected = "I en række mails fortæller Karina Pedersen (f. 1976) historien om sin opvækst i Korskærparken i Fredericia. Med sin egen underklasse-familie som eksempel, tager hun et opgør med velfærdsdanmark og de sociale ydelser";

    Assert.equalValue( "get abstract from common data ", ManifestationInfo.getAbstract( commonData, localData ), expected );


    commonDataString =
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
        '<ac:identifier>52568765|870970</ac:identifier>' +
        '<ac:source>Bibliotekskatalog</ac:source>' +
        '<dc:title>Helt ude i HESTEN</dc:title>' +
        '<dc:title xsi:type="dkdcplus:full">Helt ude i hampen : mails fra underklassen</dc:title>' +
        '<dc:creator xsi:type="dkdcplus:aut">Karina Pedersen (f. 1976)</dc:creator>' +
        '<dc:creator xsi:type="oss:sort">Pedersen, Karina (f. 1976)</dc:creator>' +
        '<dc:subject xsi:type="dkdcplus:DK5">30.1628</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">Karina Pedersen (f. 1976)</dc:subject>' +
        '<dc:subject xsi:type="oss:sort">Pedersen, Karina (f. 1976)</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DK5-Text">Underklasser</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">barndomserindringer</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">breve</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">erindringer</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">familier</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">mor-datter forholdet</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">socialt udsatte</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">underklassen</dc:subject>' +
        '<dcterms:abstract>  I en række mails fortæller Karina Pedersen (f. 1976) historien om sin opvækst i Korskærparken i Fredericia. Med sin egen underklasse-familie som eksempel, tager hun et opgør med velfærdsdanmark og de sociale ydelser  </dcterms:abstract>' +
        '<dcterms:audience>alment niveau</dcterms:audience>' +
        '<dcterms:audience>voksenmaterialer</dcterms:audience>' +
        '<dkdcplus:version>1. udgave, 1. oplag (2016)</dkdcplus:version>' +
        '<dc:publisher>Gyldendal</dc:publisher>' +
        '<dc:date>2016</dc:date>' +
        '<dc:type xsi:type="dkdcplus:BibDK-Type">Bog</dc:type>' +
        '<dcterms:extent>195 sider</dcterms:extent>' +
        '<dc:identifier xsi:type="dkdcplus:ISBN">9788702210170</dc:identifier>' +
        '<dc:language xsi:type="dcterms:ISO639-2">dan</dc:language>' +
        '<dc:language>Dansk</dc:language>' +
        '<dcterms:spatial xsi:type="dkdcplus:DBCF">Danmark</dcterms:spatial>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">1980-1989</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">1990-1999</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">2000-2009</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">2010-2019</dcterms:temporal>' +
        '</dkabm:record>' +
        '</ting:container>';


    localDataString =
        '<ting:localData ' +
        'xmlns:ac="http://biblstandard.dk/ac/namespace/" ' +
        'xmlns:dc="http://purl.org/dc/elements/1.1/" ' +
        'xmlns:dcterms="http://purl.org/dc/terms/" ' +
        'xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/" ' +
        'xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/" ' +
        'xmlns:docbook="http://docbook.org/ns/docbook" ' +
        'xmlns:oss="http://oss.dbc.dk/ns/osstypes" ' +
        'xmlns:ting="http://www.dbc.dk/ting" ' +
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"> ' +
        '<dkabm:record>' +
        '<ac:identifier>52568765|761500</ac:identifier>' +
        '<ac:source>Bibliotekskatalog</ac:source>' +
        '<dc:title>Helt ude i hampen</dc:title>' +
        '<dc:title xsi:type="dkdcplus:full">Helt ude i hampen : mails fra underklassen</dc:title>' +
        '<dc:creator xsi:type="dkdcplus:aut">Karina Pedersen (f. 1976)</dc:creator>' +
        '<dc:creator xsi:type="oss:sort">Pedersen, Karina (f. 1976)</dc:creator>' +
        '<dc:subject xsi:type="dkdcplus:DK5">99.4 Pedersen, Karina, f. 1976</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DK5-Text">Biografier af enkelte personer</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">Karina Pedersen (f. 1976)</dc:subject>' +
        '<dc:subject xsi:type="oss:sort">Pedersen, Karina (f. 1976)</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">barndomserindringer</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">breve</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCO">erindringer</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">familier</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">mor-datter forholdet</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">socialt udsatte</dc:subject>' +
        '<dc:subject xsi:type="dkdcplus:DBCF">underklassen</dc:subject>' +
//        '<dcterms:abstract>I en række mails fortæller Karina Pedersen (f. 1976) historien om sin opvækst i Korskærparken i Fredericia. Med sin egen underklasse-familie som eksempel, tager hun et opgør med velfærdsdanmark og de sociale ydelser</dcterms:abstract>' +
        '<dcterms:audience>alment niveau</dcterms:audience>' +
        '<dcterms:audience>voksenmaterialer</dcterms:audience>' +
        '<dkdcplus:version>1. udgave, 1. oplag (2016)</dkdcplus:version>' +
        '<dc:publisher>Gyldendal</dc:publisher>' +
        '<dc:date>2016</dc:date>' +
        '<dc:type xsi:type="dkdcplus:BibDK-Type">Bog</dc:type>' +
        '<dcterms:extent>195 sider</dcterms:extent>' +
        '<dc:identifier xsi:type="dkdcplus:ISBN">9788702210170</dc:identifier>' +
        '<dc:language xsi:type="dcterms:ISO639-2">dan</dc:language>' +
        '<dc:language>Dansk</dc:language>' +
        '<dcterms:spatial xsi:type="dkdcplus:DBCF">Danmark</dcterms:spatial>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">1980-1989</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">1990-1999</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">2000-2009</dcterms:temporal>' +
        '<dcterms:temporal xsi:type="dkdcplus:DBCP">2010-2019</dcterms:temporal>' +
        '</dkabm:record>' +
        '</ting:localData>';

    commonData = XmlUtil.fromString( commonDataString );
    localData = XmlUtil.fromString( localDataString );

    expected = "I en række mails fortæller Karina Pedersen (f. 1976) historien om sin opvækst i Korskærparken i Fredericia. Med sin egen underklasse-familie som eksempel, tager hun et opgør med velfærdsdanmark og de sociale ydelser";

    Assert.equalValue( "get abstract from common data with whitespace ", ManifestationInfo.getAbstract( commonData, localData ), expected );


    commonDataString =
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
        '<dcterms:abstract>I en række mails fortæller Karina Pedersen (f. 1976) historien om sin opvækst i Korskærparken i Fredericia. Med sin egen underklasse-familie som eksempel, tager hun et opgør med velfærdsdanmark og de sociale ydelser</dcterms:abstract>' +
        '</dkabm:record>' +
        '</ting:container>';

    localDataString =
        '<ting:localData ' +
        'xmlns:ac="http://biblstandard.dk/ac/namespace/" ' +
        'xmlns:dc="http://purl.org/dc/elements/1.1/" ' +
        'xmlns:dcterms="http://purl.org/dc/terms/" ' +
        'xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/" ' +
        'xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/" ' +
        'xmlns:docbook="http://docbook.org/ns/docbook" ' +
        'xmlns:oss="http://oss.dbc.dk/ns/osstypes" ' +
        'xmlns:ting="http://www.dbc.dk/ting" ' +
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"> ' +
        '<dkabm:record>' +
        '<dcterms:abstract>Her står der en fin beskrivelse i local data</dcterms:abstract>' +
        '</dkabm:record>' +

        '</ting:localData>';

    commonData = XmlUtil.fromString( commonDataString );
    localData = XmlUtil.fromString( localDataString );

    expected = "Her står der en fin beskrivelse i local data";

    Assert.equalValue( "get abstract from local data ", ManifestationInfo.getAbstract( commonData, localData ), expected );


    commonDataString =
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
        '<dcterms:abstract>I en række mails fortæller Karina Pedersen (f. 1976) historien om sin opvækst i Korskærparken i Fredericia. Med sin egen underklasse-familie som eksempel, tager hun et opgør med velfærdsdanmark og de sociale ydelser</dcterms:abstract>' +
        '</dkabm:record>' +
        '</ting:container>';

    localDataString =
        '<ting:localData ' +
        'xmlns:ac="http://biblstandard.dk/ac/namespace/" ' +
        'xmlns:dc="http://purl.org/dc/elements/1.1/" ' +
        'xmlns:dcterms="http://purl.org/dc/terms/" ' +
        'xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/" ' +
        'xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/" ' +
        'xmlns:docbook="http://docbook.org/ns/docbook" ' +
        'xmlns:oss="http://oss.dbc.dk/ns/osstypes" ' +
        'xmlns:ting="http://www.dbc.dk/ting" ' +
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"> ' +
        '<dkabm:record>' +
        '<dcterms:abstract>  Her står der en fin beskrivelse i local data  </dcterms:abstract>' +
        '</dkabm:record>' +

        '</ting:localData>';

    commonData = XmlUtil.fromString( commonDataString );
    localData = XmlUtil.fromString( localDataString );

    expected = "Her står der en fin beskrivelse i local data";

    Assert.equalValue( "get abstract from local data with whitespace ", ManifestationInfo.getAbstract( commonData, localData ), expected );


} );


UnitTest.addFixture( "ManifestationInfo.getSubjects", function() {

    var localDataString = '<ting:localData' +
            ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
            ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
            ' xmlns:dcterms="http://purl.org/dc/terms/"' +
            ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
            ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
            ' xmlns:docbook="http://docbook.org/ns/docbook"' +
            ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
            ' xmlns:ting="http://www.dbc.dk/ting"' +
            ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
            '<dkabm:record>' +
            '<dc:title>Djævelens lærling</dc:title>' +
            '<dc:title xsi:type="dkdcplus:full">Djævelens lærling</dc:title>' +
            '<dc:creator xsi:type="dkdcplus:aut">Kenneth Bøgh Andersen</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Bøgh Andersen, Kenneth</dc:creator>' +
            '<dc:subject xsi:type="dkdcplus:DBCS">Helvede</dc:subject>' +
            '<dc:subject xsi:type="dkdcplus:DK5-Text">Skønlitteratur</dc:subject>' +
            '<dc:subject xsi:type="dkdcplus:DBCS">fantasy</dc:subject>' +
            '<dc:subject xsi:type="dkdcplus:genre">fantasy</dc:subject>' +
            '<dc:subject xsi:type="dkdcplus:DBCN">for 12 år</dc:subject>' +
            '<dc:subject xsi:type="dkdcplus:DBCN">for 13 år</dc:subject>' +
            '<dc:subject xsi:type="dkdcplus:DBCN">for 14 år</dc:subject>' +
            '<dc:subject xsi:type="dkdcplus:DK5">sk</dc:subject>' +
            '</dkabm:record>' +
            '</ting:localData>';
    var commonDataString = '<empty/>';

    var localData = XmlUtil.fromString( localDataString );
    var commonData = XmlUtil.fromString( commonDataString );

    var expected = [
        { type : "DBCS", value : "Helvede" },
        { type : "DK5-Text", value : "Skønlitteratur" },
        { type : "DBCS", value : "fantasy" },
        { type : "genre", value : "fantasy" },
        { type : "DBCN", value : "for 12 år" },
        { type : "DBCN", value : "for 13 år" },
        { type : "DBCN", value : "for 14 år" },
        { type : "DK5", value : "sk" }
    ];

    Assert.equalValue( "get subjects from local stream ", ManifestationInfo.getSubjects( commonData, localData ), expected );

    localDataString = '<ting:localData' +
            ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
            ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
            ' xmlns:dcterms="http://purl.org/dc/terms/"' +
            ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
            ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
            ' xmlns:docbook="http://docbook.org/ns/docbook"' +
            ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
            ' xmlns:ting="http://www.dbc.dk/ting"' +
            ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
            '<dkabm:record>' +
            '<dc:title>Djævelens lærling</dc:title>' +
            '<dc:title xsi:type="dkdcplus:full">Djævelens lærling</dc:title>' +
            '<dc:creator xsi:type="dkdcplus:aut">Kenneth Bøgh Andersen</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Bøgh Andersen, Kenneth</dc:creator>' +
            '<dc:subject xsi:type="dkdcplus:DBCS">Helvede</dc:subject>' +
            '<dc:subject xsi:type="dkdcplus:DK5-Text">Skønlitteratur</dc:subject>' +
            '<dc:subject xsi:type="dkdcplus:DBCS">fantasy</dc:subject>' +
            '<dc:subject xsi:type="dkdcplus:genre">fantasy</dc:subject>' +
            '<dc:subject xsi:type="dkdcplus:DK5">sk</dc:subject>' +
            '</dkabm:record>' +
            '</ting:localData>';
    commonDataString = '<ting:container' +
            ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
            ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
            ' xmlns:dcterms="http://purl.org/dc/terms/"' +
            ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
            ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
            ' xmlns:docbook="http://docbook.org/ns/docbook"' +
            ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
            ' xmlns:ting="http://www.dbc.dk/ting"' +
            ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
            '<dkabm:record>' +
            '<dc:title>Djævelens lærling</dc:title>' +
            '<dc:title xsi:type="dkdcplus:full">Djævelens lærling</dc:title>' +
            '<dc:creator xsi:type="dkdcplus:aut">Kenneth Bøgh Andersen</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Bøgh Andersen, Kenneth</dc:creator>' +
            '<dc:subject xsi:type="dkdcplus:DBCN">for 12 år</dc:subject>' +
            '<dc:subject xsi:type="dkdcplus:DBCN">for 13 år</dc:subject>' +
            '<dc:subject xsi:type="dkdcplus:DBCN">for 14 år</dc:subject>' +
            '<dc:subject xsi:type="dkdcplus:DK5">sk</dc:subject>' +
            '</dkabm:record>' +
            '</ting:container>';

    localData = XmlUtil.fromString( localDataString );
    commonData = XmlUtil.fromString( commonDataString );

    expected = [
        { type : "DBCS", value : "Helvede" },
        { type : "DK5-Text", value : "Skønlitteratur" },
        { type : "DBCS", value : "fantasy" },
        { type : "genre", value : "fantasy" },
        { type : "DK5", value : "sk" }
    ];

    Assert.equalValue( "get subjects from local stream over common stream ", ManifestationInfo.getSubjects( commonData, localData ), expected );

    localDataString = '<empty/>';
    commonDataString = '<ting:container' +
            ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
            ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
            ' xmlns:dcterms="http://purl.org/dc/terms/"' +
            ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
            ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
            ' xmlns:docbook="http://docbook.org/ns/docbook"' +
            ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
            ' xmlns:ting="http://www.dbc.dk/ting"' +
            ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
            '<dkabm:record>' +
            '<dc:title>Djævelens lærling</dc:title>' +
            '<dc:title xsi:type="dkdcplus:full">Djævelens lærling</dc:title>' +
            '<dc:creator xsi:type="dkdcplus:aut">Kenneth Bøgh Andersen</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Bøgh Andersen, Kenneth</dc:creator>' +
            '<dc:subject xsi:type="dkdcplus:DBCS">Helvede</dc:subject>' +
            '<dc:subject xsi:type="dkdcplus:DK5-Text">Skønlitteratur</dc:subject>' +
            '<dc:subject xsi:type="dkdcplus:DBCS">fantasy</dc:subject>' +
            '<dc:subject xsi:type="dkdcplus:genre">fantasy</dc:subject>' +
            '<dc:subject xsi:type="dkdcplus:DBCN">for 12 år</dc:subject>' +
            '<dc:subject xsi:type="dkdcplus:DBCN">for 13 år</dc:subject>' +
            '<dc:subject xsi:type="dkdcplus:DBCN">for 14 år</dc:subject>' +
            '<dc:subject xsi:type="dkdcplus:DK5">sk</dc:subject>' +
            '</dkabm:record>' +
            '</ting:container>';

    localData = XmlUtil.fromString( localDataString );
    commonData = XmlUtil.fromString( commonDataString );

    expected = [
        { type : "DBCS", value : "Helvede" },
        { type : "DK5-Text", value : "Skønlitteratur" },
        { type : "DBCS", value : "fantasy" },
        { type : "genre", value : "fantasy" },
        { type : "DBCN", value : "for 12 år" },
        { type : "DBCN", value : "for 13 år" },
        { type : "DBCN", value : "for 14 år" },
        { type : "DK5", value : "sk" }
    ];

    Assert.equalValue( "get subjects from common stream ", ManifestationInfo.getSubjects( commonData, localData ), expected );

    localDataString = '<ting:localData' +
            ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
            ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
            ' xmlns:dcterms="http://purl.org/dc/terms/"' +
            ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
            ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
            ' xmlns:docbook="http://docbook.org/ns/docbook"' +
            ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
            ' xmlns:ting="http://www.dbc.dk/ting"' +
            ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
            '<dkabm:record>' +
            '<dc:title>Djævelens lærling</dc:title>' +
            '<dc:title xsi:type="dkdcplus:full">Djævelens lærling</dc:title>' +
            '<dc:creator xsi:type="dkdcplus:aut">Kenneth Bøgh Andersen</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Bøgh Andersen, Kenneth</dc:creator>' +
            '</dkabm:record>' +
            '</ting:localData>';
    commonDataString = '<empty/>';

    localData = XmlUtil.fromString( localDataString );
    commonData = XmlUtil.fromString( commonDataString );

    expected = [ ];

    Assert.equalValue( "get subjects from  stream with no subjects", ManifestationInfo.getSubjects( commonData, localData ), expected );

    localDataString = '<ting:localData' +
            ' xmlns:ac="http://biblstandard.dk/ac/namespace/"' +
            ' xmlns:dc="http://purl.org/dc/elements/1.1/"' +
            ' xmlns:dcterms="http://purl.org/dc/terms/"' +
            ' xmlns:dkabm="http://biblstandard.dk/abm/namespace/dkabm/"' +
            ' xmlns:dkdcplus="http://biblstandard.dk/abm/namespace/dkdcplus/"' +
            ' xmlns:docbook="http://docbook.org/ns/docbook"' +
            ' xmlns:oss="http://oss.dbc.dk/ns/osstypes"' +
            ' xmlns:ting="http://www.dbc.dk/ting"' +
            ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
            '<dkabm:record>' +
            '<dc:title>Djævelens lærling</dc:title>' +
            '<dc:title xsi:type="dkdcplus:full">Djævelens lærling</dc:title>' +
            '<dc:creator xsi:type="dkdcplus:aut">Kenneth Bøgh Andersen</dc:creator>' +
            '<dc:creator xsi:type="oss:sort">Bøgh Andersen, Kenneth</dc:creator>' +
            '<dc:subject xsi:type="">Helvede</dc:subject>' +
            '<dc:subject>Skønlitteratur</dc:subject>' +
            '</dkabm:record>' +
            '</ting:localData>';
    commonDataString = '<empty/>';

    localData = XmlUtil.fromString( localDataString );
    commonData = XmlUtil.fromString( commonDataString );

    expected = [
        { "type" : null, "value" : "Helvede" },
        { "type" : null, "value" : "Skønlitteratur" }
    ];

    Assert.equalValue( "get subjects from ting stream with no types", ManifestationInfo.getSubjects( commonData, localData ), expected );

} );
