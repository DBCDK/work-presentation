use( "UnitTest" );

use( "SelectOwnerPriority" );

UnitTest.addFixture( "SelectOwnerPriority.computeValues", function () {

    var manifestations = {
        a: {
            priorityKeys: {
                identifier: "abc|710101",
                date: "2019",
                version: "1. udgave"
            },
            types: ['Bog']
        },
        b: {
            priorityKeys: {
                identifier: "def|710101",
                date: "2016",
                version: "2. udgave"
            },
            types: ['Bog']
        }
    };

    var expected = [
        'b', // significantly older
        'a'
    ];

    var actual = SelectOwnerPriority.computeValues( manifestations );
    // values are {a:28.675177318965037, b:38.073142980733} at inception time
    // as tuning goes on these are likely to change

    var order = SelectOwnerPriority.getKeysInOrder( actual );

    Assert.equalValue( "computed values", order, expected );
} );

UnitTest.addFixture( "SelectOwnerPriority.addValues", function () {

    var values = {
        a: 2,
        b: 4,
        c: 8
    };
    var additions = {
        b: -2,
        c: 2,
        d: 10
    };

    var expected = {
        a: 2,
        b: 2,
        c: 10
    };

    SelectOwnerPriority.addValues( values, additions );

    Assert.equalValue( "add values", values, expected );
} );

UnitTest.addFixture( "SelectOwnerPriority.multiplyValues", function () {

    var values = {
        a: 2,
        b: 4,
        c: 8
    };
    var multiplications = {
        b: 2,
        c: -2,
        d: 10
    };

    var expected = {
        a: 2,
        b: 8,
        c: -16
    };

    SelectOwnerPriority.multiplyValues( values, multiplications );

    Assert.equalValue( "multiply values", values, expected );
} );

UnitTest.addFixture( "SelectOwnerPriority.getAgeValues", function () {

    var values = {
        c: {
            priorityKeys: {
                date: "1920"
            }
        },
        a: {
            priorityKeys: {
                date: "1910"
            }
        },
        e: {
            priorityKeys: {
                date: "1915-1920"
            }
        },
        d: {
            priorityKeys: {
                date: "191?" //1912.5
            }
        },
        b: {
            priorityKeys: {
                date: "????" // invalid number
            }
        }
    };

    var expected = ['a', 'd', 'e', 'c', 'b'];

    var actual = SelectOwnerPriority.getAgeValues( values );
    var sorted = SelectOwnerPriority.getKeysInOrder( actual );

    Assert.equalValue( "get age values", sorted, expected );
} );

UnitTest.addFixture( "SelectOwnerPriority.getTypePriorities", function () {

    var values = {
        a: {
            types: []
        },
        b: {
            types: ["tegneserie", "Bog", "film"]
        },
        c: {
            types: ["Lydbog CD MP3"]
        },
        d: {
            types: ["xxx"]
        }
    };

    var expected = {
        a: 0.5,
        b: 5,
        c: 4,
        d: 0.5
    };

    var actual = SelectOwnerPriority.getTypePriorities( values );

    Assert.equalValue( "get type values", actual, expected );
} );

UnitTest.addFixture( "SelectOwnerPriority.getEditionPriorities", function () {

    var values = {
        a: {
            priorityKeys: {
                version: "3. udgave, 13. oplag (2018)"
            }
        },
        b: {
            priorityKeys: {
                version: "1. bogklubudgave, 1. oplag (2006)"
            }
        },
        c: {
            priorityKeys: {
                version: "f\u00F8rste udgave"
            }
        },
        d: {
            priorityKeys: {
                version: null
            }
        },
        e: {
            priorityKeys: {
                version: "First ed"
            }
        },
        f: {
            priorityKeys: {
                version: "1st edition"
            }
        },
        g: {
            priorityKeys: {
                version: "2nd edition"
            }
        }
    };

    var expected = [
        'c',
        'e',
        'f',
        'g',
        'a',
        'b', // bogklub is priotitized low
        'd' // no value is very low
    ];
    var actual = SelectOwnerPriority.getEditionPriorities( values );

    Assert.equalValue( "get edition value of 1. udgave", actual.c, 1.0 );
    Assert.equalValue( "get edition value of First edition", actual.e, 1.0 );
    Assert.equalValue( "get edition value of 1st edition", actual.f, 1.0 );

    var order = SelectOwnerPriority.getKeysInOrder( actual );

    Assert.equalValue( "get edition values", order, expected );
} );

UnitTest.addFixture( "SelectOwnerPriority.getIdBonus", function () {

    var values = {
        a: {
            priorityKeys: {
                identifier: "12345678|870970"
            }
        },
        b: {
            priorityKeys: {
                identifier: "12345678__3|777777"
            }
        },
        c: {
            priorityKeys: {
                identifier: "12345678|765432"
            }
        },
        d: {
            priorityKeys: {
                identifier: "12345678__1|777777"
            }
        }
    };

    var expected = {
        a: 5,
        b: -5,
        c: 0,
        d: 0
    };

    var actual = SelectOwnerPriority.getIdBonus( values );
    Assert.equalValue( "get id-bonus values", actual, expected );
} );

UnitTest.addFixture( "SelectOwnerPriority.getDeletedPenalty", function () {

    var values = {
        a: {
            priorityKeys: {
                identifier: "12345678|870970",
                deleted: "false"
            }
        },
        b: {
            priorityKeys: {
                identifier: "12345678__3|777777",
                deleted: "true"
            }
        },
        c: {
            priorityKeys: {
                identifier: "12345678|765432"
            }
        },
        d: {
            priorityKeys: {
                identifier: "12345678__1|777777",
                deleted: "what?"
            }
        }
    };

    var expected = {
        a: 1,
        b: 0.001,
        c: 1,
        d: 1
    };

    var actual = SelectOwnerPriority.getDeletedPenalty( values );
    Assert.equalValue( "get deleted penalty values", actual, expected );
} );

