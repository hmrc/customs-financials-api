
# Customs Financials API

An API service for the CDS Financials project, which serves to:

* proxy requests from the front-end (public) service, with circuit breaker protection, to HoDs (SPS, ETMP, etc.) 

This application lives in the "protected" zone. It integrates with:
* Secure Payments Service (SPS) via Messaging Delivery Group (MDG)
* Enterprise Tax Management Platform (ETMP) via MDG

The MDG integrations are:

* DUD09 (ACC27) Accounts and Balances
* ACC24 Historic document requests
* ACC28 Guarantee transactions
* ACC29 Retrieve standing authorities
* ACC30 Manage standing authorities (change permissions)
* ACC31 Retrieve cash account transaction listings
* ACC37 Amend duty deferment contact details 
* ACC38 Retrieve duty deferment contact details
* SUB09 View account authorities
* SUB21 Retrieve EORI history

In dev/test environments, the upstream services are stubbed out using the [customs-financials-hods-stub](https://github.com/hmrc/customs-financials-hods-stub/).

## Running the application locally

`sbt "run 9878"` to start in `DEV` mode or 
`sbt "start -Dhttp.port=9878"` to run in `PROD` mode.

## Running tests
 
There is just one test source tree in the `test` folder. Use `sbt test` to run them.

To get a unit test coverage report, you can run `sbt clean coverage test coverageReport`,
then open the resulting coverage report `target/scala-2.12/scoverage-report/index.html` in a web browser.

Test coverage threshold is set at 90% - so if you commit any significant amount of implementation code without writing tests, you can expect the build to fail.
