use("Log");
use("SelectOwnerPriority")

function selectWorkOwner( manifestations ) {
    Log.debug("selectWorkOwner()");

    for ( var id in manifestations ) {
        var object = manifestations[id];
        Log.trace(id, " -> " , object);
    }
    var values = SelectOwnerPriority.computeValues( manifestations );
    var sorted = SelectOwnerPriority.getKeysInOrder( values );

    return sorted[0];
}

