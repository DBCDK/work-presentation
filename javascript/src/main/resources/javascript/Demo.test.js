use("UnitTest");

use("Demo")

UnitTest.addFixture("Demo", function () {
    Assert.equalValue("one is 1", Demo.example(), "1");
});

