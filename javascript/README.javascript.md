# Javascript development in work-presentation
This is a README specific for working with the javascript part of this work-presentation project. 

##Prerequisites
You need to have installed the dbc-jsshell and postgresql


## update environment
When you've checked this project out from git the first thing you should do is go to the root and run 'mvn install'
	> user@machine:~/work-presentation$ mvn install

In the following development you should run this command each time you switch branches or the branch has been updated:

	> user@machine:~/work-presentation$ mvn clean verify -DskipITs

This builds the work-presentation jar file with embedded javascript files, but skips the unit tests for the javacode.


## File structure
├── javascript
│   ├── jsshell.sh
│   ├── js-unittest.log
│   ├── pom.xml
│   ├── src
│   └── target


In the javascript folder there is a shell script jsshell.sh with the correct paths set up for a dbc-jsshell. So use that to access a dbc-jsshell or to run single unit tests. 

The relevant javascripts are found in 
src/main/resources/javascript/
├── BuildCacheObject.js
├── ManifestationInfo.test.js
└── ManifestationInfo.use.js

where BuildCacheObject.js contains the main entry function that is called from the Java code. 

More modules may have been added since. 

##Run tests
To run acceptance tests and all unit tests use mvn test in the javascript folder

	> user@machine:~/work-presentation/javascript$ mvn test

The log from the unit tests will then be generated in the javascript folder
js-unittest.log


To run unit test for a single module you can use the dbc-shell wrapper jsshell.sh

	> user@machine:~/work-presentation/javascript$ ./jsshell.sh src/main/resources/javascript/ModuleName.test.js


To save a log file from this you can use the normal -l flag, e.g.

        > user@machine:~/work-presentation/javascript$ ./jsshell.sh src/main/resources/javascript/ModuleName.test.js -l js.log

## Acceptance tests
The acceptance tests are found in 

src/test/resources/accept-test/
├── example
│   ├── commonData.xml
│   ├── DC.xml
│   ├── expected.json
│   └── localData.xml
├── koraan-empty-local-data
│   ├── commonData.xml
│   ├── DC.xml
│   ├── expected.json
│   └── localData.xml
└── vildheks
    ├── commonData.xml
    ├── DC.xml
    ├── expected.json
    └── localData.xml

More tests may have been added later. 

So each acceptance test is defined as a folder, where the folder name will function as manifestationId for the record. 

The folder then contains three input files and the expected json result. 

An acceptance can have empty content in a file if needed for the test. This for example is used in test 'koraan-empty-local-data'. 
But all file names should be present and named as:
    ├── commonData.xml
    ├── DC.xml
    ├── expected.json
    └── localData.xml

The input files correspond to the data streams found in Corepo on a record, the common data stream, the DC stream and the local data stream. 

The expected output looks like this: 

{
  "title" : "værkstedstekniske beregninger",
  "fullTitle" : "Værkstedstekniske beregninger. M2, Boring",
  "description" : null,
  "pid" : "example",
  "creators" : [ ],
  "subjects" : [ ],
  "types" : [ "Bog" ]
}

The pid corresponds to the folder name for the acceptance test. 





