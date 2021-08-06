use( "Log" );
use( "ManifestationInfo" );

function buildManifestationInformation( manifestationId, xmlObjects ) {
    Log.debug( "buildManifestationInformation(", manifestationId, ")" );

    return JSON.stringify(ManifestationInfo.getManifestationInfoFromXmlObjects( manifestationId, xmlObjects ));

}
