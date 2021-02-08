use("UnitTest");

use("SelectOwnerPriority");

UnitTest.addFixture("SelectOwnerPriority.addValues", function () {

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

    SelectOwnerPriority.addValues(values, additions);

    Assert.equalValue("add values", values, expected);
});

UnitTest.addFixture("SelectOwnerPriority.multiplyValues", function () {

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

    SelectOwnerPriority.multiplyValues(values, multiplications);

    Assert.equalValue("multiply values", values, expected);
});

UnitTest.addFixture("SelectOwnerPriority.getAgeValues", function () {

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

    var actual = SelectOwnerPriority.getAgeValues(values);
    var sorted = SelectOwnerPriority.getKeysInOrder(actual);

    Assert.equalValue("get age values", sorted, expected);
});

UnitTest.addFixture("SelectOwnerPriority.getTypePriorities", function () {

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

    var actual = SelectOwnerPriority.getTypePriorities(values);

    Assert.equalValue("get type values", actual, expected);
});

UnitTest.addFixture("SelectOwnerPriority.getIdBonus", function () {

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

    var actual = SelectOwnerPriority.getIdBonus(values);
    Assert.equalValue("get type values", actual, expected);
});

