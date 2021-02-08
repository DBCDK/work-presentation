use( "Log" );

EXPORTED_SYMBOLS = ["SelectOwnerPriority"];

var SelectOwnerPriority = ( function () {
    var KEYS = "priorityKeys";

    // These values are selected arbitrarily
    var TYPE_PRIORITY = {
        "_default_": .5,

        "bog": 5,
        "ebog": 4,
        "lydbog": 4,
        "serie": 3,
        "tegneserie": 3,
        "billedbog": 2,
        "bog stor skrift": 2,
        "tegneserie net": 2,

        "dvd": 3.5,
        "video": 3.5,
        "film": 3.5,
        "blu ray": 3.5,

        "cd": 2.5,
        "node": 1.25,
        "grammofonplade": 1.5,
        "musik net": 2.5,
        "lyd": 1.5,
        "baand": 1.5,
        "sang": 1,
        "lyd cd": 1.5,

        "spil": 2.5,
        "cd rom": 2.5,
        "xbox": 2.5,
        "playstation": 2.5,
        "nintendo": 2.5,
        "pc": 2.5,

        "foto": 2,

        "artikel": 1,
        "tidsskrift": 1,
        "netdokument": 1,
        "tidsskriftsartikel": 1,
        "avisartikel": 1,
        "anmeldelse": 1,
        "tidsskrift net": 1,
        "avis": 1
    };


    /**
     * Function that compules relevance of each manifestation as owner
     *
     * @type {function}
     * @syntax SelectOwnerPriority.computeValues( manifestations )
     * @param {Object} manifestations as a map from id to java-type ManifestationInformation
     * @return {Object} map of manifestation id to relevance as owner
     * @function
     * @name SelectOwnerPriority.computeValues
     */

    function computeValues( manifestations ) {
        Log.trace( "Entering: ManifestationInfo.computeValues function" );

        var values = getDefaultValues( manifestations );
        var ageValues = getAgeValues( manifestations );
        multiplyValues( values, ageValues );
        var editionValues = getEditionPriorities( manifestations );
        multiplyValues( values, editionValues );
        var typeValues = getTypePriorities( manifestations );
        multiplyValues( values, typeValues );
        var idBonus = getIdBonus( manifestations );
        addValues( values, idBonus );

        Log.trace( "Leaving: ManifestationInfo.computeValues function" );

        return values;
    }


    /**
     * Function that finds the key with the highest value in a map
     *
     * @type {function}
     * @syntax SelectOwnerPriority.getKeysInOrder( values )
     * @param {Object} map of manifestation id to relevance as owner
     * @return {Array} manifestation id by most relevant
     * @function
     * @name SelectOwnerPriority.getKeysInOrder
     */

    function getKeysInOrder( values ) {
        return Object.keys( values ).sort( function ( l, r ) {
            return values[r] - values[l];
        } );
    }


    /**
     * Function that adds the 2nd maps values to the 1st
     *
     * @type {function}
     * @syntax SelectOwnerPriority.addValues( values , addition )
     * @param {Object} map of manifestation id to relevance as owner
     * @param {Object} map of manifestation id to relevance increment
     * @function
     * @name SelectOwnerPriority.addValues
     */

    function addValues( values, addition ) {
        Log.trace( "Entering: ManifestationInfo.addValues function" );

        for ( var id in values ) {
            if ( id in addition ) {
                values[id] += addition[id];
            }
        }

        Log.trace( "Leaving: ManifestationInfo.addValues function" );
    }


    /**
     * Function that multiplies the 2nd maps values to the 1st
     *
     * If no value for a key is found in 2nd map, then 1 is assumed
     *
     * @type {function}
     * @syntax SelectOwnerPriority.multiplyValues( values , multiplier )
     * @param {Object} map of manifestation id to relevance as owner
     * @param {Object} map of manifestation id to relevance in-/decrease
     * @function
     * @name SelectOwnerPriority.multiplyValues
     */

    function multiplyValues( values, multiplier ) {
        Log.trace( "Entering: ManifestationInfo.multiplyValues function" );

        for ( var id in values ) {
            if ( id in multiplier ) {
                values[id] *= multiplier[id];
            }
        }

        Log.trace( "Leaving: ManifestationInfo.multiplyValues function" );
    }


    /**
     * Function that creates a baseline of relevanve for each manifestation in the map
     *
     * @type {function}
     * @syntax SelectOwnerPriority.getDefaultValues( manifestations )
     * @param {Object} manifestations as a map from id to java-type ManifestationInformation
     * @return {Object} map of manifestation id to relevance as owner
     * @function
     * @name SelectOwnerPriority.getDefaultValues
     */

    function getDefaultValues( manifestations ) {
        Log.trace( "Entering: ManifestationInfo.getDefaultValues function" );

        var values = {};
        for ( var id in manifestations ) {
            values[id] = 10.0;
        }

        Log.trace( "Leaving: ManifestationInfo.getDefaultValues function" );

        return values;
    }


    /**
     * Function that extracts a date as a number from a dc:date value
     *
     * if a date is a range, the first is used
     *
     * if a date has '?' in it, the date returned is 1/4 through the range
     *
     * @type {function}
     * @syntax SelectOwnerPriority.determineYear( date )
     * @param {String} A string representation of the date
     * @return {Number} date as a year (null if date is not valid
     * @function
     * @name SelectOwnerPriority.determineYear
     */

    function determineYear( date ) {
        if ( date ) {
            var match;
            if ( ( match = date.match( /^(\d{4})(-?\d{4})?$/ ) ) ) {
                var year = parseInt( match[1] );
                if ( year >= 1600 ) {
                    return year;
                } else {
                    Log.warn( "Too old year: ", date );
                }
            } else if ( ( match = date.match( /^(\d{3}\?{1}|\d{2}\?{2}|\d{1}\?{3})(-?.{4})?$/ ) ) ) {
                var low = parseInt( match[1].replace( /\?/, '0' ) );
                var high = parseInt( match[1].replace( /\?/, '9' ) ) + 1;
                var diff = high - low;
                var year = low + diff * 0.25;
                return year;
            } else {
                Log.warn( "Cannot handle year: ", date );
            }
        }
        return null;
    }


    /**
     * Function that computes a multiplier for each manifestation based upon dc:date
     *
     * the younger the manifestation, the less relevant it is
     *
     * no valid date returns a relevance of .1
     *
     * @type {function}
     * @syntax SelectOwnerPriority.getAgeValues( manifestations )
     * @param {Object} manifestations as a map from id to java-type ManifestationInformation
     * @return {Object} map of manifestation id to relevance as owner
     * @function
     * @name SelectOwnerPriority.getAgeValues
     */

    function getAgeValues( manifestations ) {
        Log.trace( "Entering: ManifestationInfo.getDefaultValues function" );

        var years = {};
        var min = Number.MAX_VALUE;

        for ( var id in manifestations ) {
            var year = determineYear( manifestations[id][KEYS].date );
            if ( year !== null ) {
                years[id] = year;
                min = Math.min( min, year );
            }
        }

        if ( min === Number.INFINITY ) {
            Log.trace( "Leaving: ManifestationInfo.getDefaultValues function - no years found" );
            return {};
        }

        var values = {};
        for ( var id in manifestations ) {
            if ( id in years ) {
                values[id] = 1 / Math.log( years[id] - min + Math.E ); // The higher age, from the first, the less revevant - quick decay
            } else {
                values[id] = .1; // Could not determine year... probably not relevant as work-owner
            }
        }

        Log.trace( "Leaving: ManifestationInfo.getDefaultValues function" );
        return values;
    }


    /**
     * Function that finds a priority of a type
     *
     * The list TYPE_PRIORITY is used to determine the priority of a manifestation.
     * If multiple types are present, the highest ranked is used. Each type has
     * words removed from the right, until a rank is found. Or if no rank is found,
     * the `_default_`  value is used.
     *
     * @type {function}
     * @syntax SelectOwnerPriority.getTypePrio( name )
     * @param {String} type name
     * @return {Number} priority of the type
     * @function
     * @name SelectOwnerPriority.getTypePrio
     */

    function getTypePrio( name ) {
        name = name.toLowerCase().replace( /[^0-9a-z]/g, ' ' ); // Normalize into lowercase with spaces
        if ( name in TYPE_PRIORITY ) {
            return TYPE_PRIORITY[name];
        }
        var idx;
        for ( ; ; ) {
            var idx = name.lastIndexOf( ' ' );
            if ( idx === -1 ) {
                return TYPE_PRIORITY['_default_'];
            }
            name = name.substring( 0, idx );
            if ( name in TYPE_PRIORITY ) {
                return TYPE_PRIORITY[name];
            }
        }
    }


    /**
     * Function that takes the highest priority from a list of type names
     *
     * @type {function}
     * @syntax SelectOwnerPriority.getTypePrioFromList( manifestations )
     * @param {Array} type names
     * @return {Number} priority of the type
     * @function
     * @name SelectOwnerPriority.getTypePrioFromList
     */

    function getTypePrioFromList( names ) {
        var max = TYPE_PRIORITY['_default_'];
        for ( var i in names ) {
            max = Math.max( max, getTypePrio( names[i] ) );
        }
        return max;
    }

    /**
     * Function that computes a relevance based upon the types tag in the ManifestationInformation
     *
     * The list TYPE_PRIORITY is used to determine the priority of a manifestation.
     * If multiple types are present, the highest ranked is used. Each type has
     * words removed from the right, until a rank is found. Or if no rank is found,
     * the `_default_`  value is used.
     *
     * @type {function}
     * @syntax SelectOwnerPriority.getTypePriorities( manifestations )
     * @param {Object} manifestations as a map from id to java-type ManifestationInformation
     * @return {Object} map of manifestation id to relevance as owner
     * @function
     * @name SelectOwnerPriority.getTypePriorities
     */

    function getTypePriorities( manifestations ) {
        Log.trace( "Entering: ManifestationInfo.getTypePriorities function" );

        var values = {};
        for ( var id in manifestations ) {
            values[id] = getTypePrioFromList( manifestations[id].types );
        }

        Log.trace( "Leaving: ManifestationInfo.getTypePriorities function" );

        return values;
    }

    /**
     * Function that computes a relevance based upon the version 1st edition before 2nd
     *
     * @type {function}
     * @syntax SelectOwnerPriority.getEditionPriorities( manifestations )
     * @param {Object} manifestations as a map from id to java-type ManifestationInformation
     * @return {Object} map of manifestation id to relevance as owner
     * @function
     * @name SelectOwnerPriority.getEditionPriorities
     */

    function getEditionPriorities( manifestations ) {
        Log.trace( "Entering: ManifestationInfo.getEditionPriorities function" );

        var values = {};
        for ( var id in manifestations ) {
            var version = manifestations[id][KEYS].version;
            var match;
            var edition;
            if ( version === null ) {
                edition = 25;
            } else if ( ( match = version.match( /^(\d+)\.? udgave/ ) ) ) {
                var edition = parseInt( match[1] );
            } else if ( ( match = version.match( /^(\d+)\.? bogklubudgave/ ) ) ) {
                var edition = parseInt( match[1] ) + 12;
            } else {
                edition = 25;
            }
            edition = edition - 1; // Editions starts with 1, make then start with 0
            values[id] = 1 / Math.log( edition + Math.E ); // The higher edition number, from the first, the less revevant - quick decay
        }

        Log.trace( "Leaving: ManifestationInfo.getEditionPriorities function" );

        return values;
    }


    /**
     * Function that computes bonus based upon the id of the manifestations
     *
     * Interpids ids with `__` as multi-volume, every subsequent is prioritized down
     *
     * Common library is prioritized up
     *
     * @type {function}
     * @syntax SelectOwnerPriority.getIdBonus( manifestations )
     * @param {Object} manifestations as a map from id to java-type ManifestationInformation
     * @return {Object} map of manifestation id to relevance as owner
     * @function
     * @name SelectOwnerPriority.getIdBonus
     */

    function getIdBonus( manifestations ) {
        Log.trace( "Entering: ManifestationInfo.getIdBonus function" );

        var values = {};
        for ( var id in manifestations ) {
            var identifier = manifestations[id][KEYS].identifier; // of format "id|agency"
            var parts = identifier.split( '|' );
            if ( parts[0].substr( -3 ) === '__1' ) { // 1st in multi-volume
                values[id] = 0;
            } else if ( parts[0].indexOf( '__' ) >= 0 ) {
                values[id] = -5;
            } else if ( parts[1] === '870970' ) {
                values[id] = 5;
            } else {
                values[id] = 0;
            }
        }

        Log.trace( "Leaving: ManifestationInfo.getIdBonus function" );

        return values;
    }


    return {
        computeValues: computeValues,
        getKeysInOrder: getKeysInOrder,
        addValues: addValues,
        multiplyValues: multiplyValues,
        getDefaultValues: getDefaultValues,
        getAgeValues: getAgeValues,
        getEditionPriorities: getEditionPriorities,
        getTypePriorities: getTypePriorities,
        getIdBonus: getIdBonus
    };
} )();
