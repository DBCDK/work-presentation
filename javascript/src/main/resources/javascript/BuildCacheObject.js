use("Log");

function buildManifestationInformation(manifestationId, xmlObjects) {
    Log.debug("buildManifestationInformation(", manifestationId, ")");
    return {
        pid: manifestationId,
        title: 'Nothing',
        fullTitle: 'The incredible long and tedious story about nothing',
        creator: 'That developer character',
        description: 'It is not really about anything',
        subject: ['borring', 'another'],
        type: 'book'
    };
}
