# Functional tests submodule

This submodule holds code that tests Aggregate through its web client and APIs. 

## Depcrecation notice

This submodule's tests should/will probably be replaced by:

 - Unit & integration tests in `common` submodule
 - An external [puppeteer](https://github.com/GoogleChrome/puppeteer) (or similar) suite.
 
## How to run these tests

1. Launch Aggregate. Have the information to connect to Aggregate ready: host, ip, username, password, etc.
1. Edit `src/test/resources/Integration.properties` file accordingly
1. Launch tests with IntelliJ or with `gradle test` command 