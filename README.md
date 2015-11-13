# Executing tests

`gradle test` executes all tests on each application server that is configured for the test

append `-Ptest=spring-petclinic` to only execute the integration test for the folder spring-petclinic

append `-PnoShutdown` to not shut down the server after the test. Use this option if you want to debug the running server.

# Writing tests
Add a subdirectory that contains a `stagemonitor-integration-test.properties` file.
In this file you can, for example, configure which java versions and which application servers this test is compatible with.

See the available tests for examples.