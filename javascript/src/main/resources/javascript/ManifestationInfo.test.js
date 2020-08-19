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