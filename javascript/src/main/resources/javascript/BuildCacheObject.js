use("Log");

function buildManifestationInformation(manifestationId, xmlObjects) {
    Log.debug("buildManifestationInformation(", manifestationId, ")");
    return {
        pid: manifestationId,
        title: 'Nothing',
        fullTitle: 'The incredible long and tedious story about nothing',
        creators: [ 'That developer character' ],
        description: 'It is not really about anything',
        subjects: ['borring', 'another'],
        types: [ 'book' ]
    };
}
