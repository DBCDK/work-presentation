use( "Log" );
use( "ManifestationInfo" );

function buildManifestationInformation( manifestationId, xmlObjects ) {
    Log.debug( "buildManifestationInformation(", manifestationId, ")" );

    return ManifestationInfo.getManifestationInfoFromXmlObjects( manifestationId, xmlObjects );

}
