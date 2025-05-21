
# Customs Financials API

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![Coverage](https://img.shields.io/badge/test_coverage-90-green.svg)](/target/scala-3.3.5/scoverage-report/index.html) [![Accessibility](https://img.shields.io/badge/WCAG2.2-AA-purple.svg)](https://www.gov.uk/service-manual/helping-people-to-use-your-service/understanding-wcag)

An API service for the CDS Financials project, which serves to:

* proxy requests from the front-end (public) service, with circuit breaker protection, to HoDs (SPS, ETMP, etc.)

This application lives in the "protected" zone. It integrates with:
* Secure Payments Service (SPS) via Messaging Delivery Group (MDG)
* Enterprise Tax Management Platform (ETMP) via MDG

## Running the service

*From the root directory*

`sbt run` - starts the service locally

`sbt runAllChecks` - Will run all checks required for a successful build

Default service port on local - 9878

### Required dependencies

There are a number of dependencies required to run the service.

The easiest way to get started with these is via the service manager CLI - you can find the installation guide [here](https://docs.tax.service.gov.uk/mdtp-handbook/documentation/developer-set-up/set-up-service-manager.html)

## Running the application locally

| Command                              | Description                                                      |
|--------------------------------------|------------------------------------------------------------------|
| `sm2 --start CUSTOMS_FINANCIALS_ALL` | Runs all dependencies                                            |
| `sm2 -s`                             | Shows running services                                           |
| `sm2 --stop CUSTOMS_FINANCIALS_API`  | Stop the micro service                                           |
| `sbt run` or `sbt run 9878`          | (from root dir) to compile the current service with your changes |
| `sbt "start -Dhttp.port=9878" `      | To run in `PROD` mode                                            |


### Runtime Dependencies
(These are subject to change and may not include every dependency)

* `AUTH`
* `AUTH_LOGIN_STUB`
* `AUTH_LOGIN_API`
* `BAS_GATEWAY`
* `CA_FRONTEND`
* `SSO`
* `USER_DETAILS`
* `CUSTOMS_FINANCIALS_SDES_STUB`
* `CUSTOM_DATA_STORE`
* `CUSTOMS_FINANCIALS_HODS_STUB`
* `CUSTOMS_FINANCIALS_EMAIL_THROTTLER`

### Login enrolments

The service can be accessed by using below enrolments and with below sample EORI numbers, via http://localhost:9949/auth-login-stub/gg-sign-in (on local) or https://<host:port>/auth-login-stub/gg-sign-in on DEV/QA/STAGING

Redirect URL - `/customs/payment-records`

| Enrolment Key	 | Identifier Name | Identifier Value |
|----------------|-----------------|------------------|
| `HMRC-CUS-ORG` | `EORINumber`    | `GB744638982000` |
| `HMRC-CUS-ORG` | `EORINumber`    | `GB744638982001` |

## Testing

The minimum requirement for test coverage is 90%. Builds will fail when the project drops below this threshold.

### Unit Tests

| Command                                | Description                  |
|----------------------------------------|------------------------------|
| `sbt test`                             | Runs unit tests locally      |
| `sbt "test/testOnly *TEST_FILE_NAME*"` | runs tests for a single file |

### Coverage

| Command                                  | Description                                                                                                 |
|------------------------------------------|-------------------------------------------------------------------------------------------------------------|
| `sbt clean coverage test coverageReport` | Generates a unit test coverage report that you can find here target/scala-3.3.5/scoverage-report/index.html |

## Feature Switches

> ### Caution!
> There's a risk of WIP features being exposed in production!
> **Don't** enable features in `application.conf`, as this will apply globally by default
### Enable features
| Command                                       | Description                                        |
|-----------------------------------------------|----------------------------------------------------|
| `sbt "run -Dfeatures.some-feature-name=true"` | enables a feature locally without risking exposure |

### Available feature flags
| Flag            | Description                                                                                                                      |
|-----------------|----------------------------------------------------------------------------------------------------------------------------------|
| onlyOpenItems   | It is used to send the 'Open Items' request filter for ACC28. If set to 'true' then results will be filtered by open items only. |
| eu-eori-enabled | enable or disable logic related to EU EORI numbers                                                                               |

Different features can be enabled / disabled per-environment via the `app-config-<env>` project by setting `features.some-feature: true`

## Helpful commands

| Command                                       | Description                                                                                                 |
|-----------------------------------------------|-------------------------------------------------------------------------------------------------------------|
| `sbt runAllChecks`                            | Runs all standard code checks                                                                               |
| `sbt clean`                                   | Cleans code                                                                                                 |
| `sbt compile`                                 | Better to say 'Compiles the code'                                                                           |
| `sbt coverage`                                | Prints code coverage                                                                                        |
| `sbt test`                                    | Runs unit tests                                                                                             |
| `sbt it/test`                                 | Runs integration tests                                                                                      |
| `sbt scalafmtCheckAll`                        | Runs code formatting checks based on .scalafmt.conf                                                         |
| `sbt scalastyle`                              | Runs code style checks based on /scalastyle-config.xml                                                      |
| `sbt Test/scalastyle`                         | Runs code style checks for unit test code /test-scalastyle-config.xml                                       |
| `sbt coverageReport`                          | Produces a code coverage report                                                                             |
| `sbt "test/testOnly *TEST_FILE_NAME*"`        | runs tests for a single file                                                                                |
| `sbt clean coverage test coverageReport`      | Generates a unit test coverage report that you can find here target/scala-3.3.5/scoverage-report/index.html |
| `sbt "run -Dfeatures.some-feature-name=true"` | enables a feature locally without risking exposure                                                          |


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
* DEC64 Submit file upload

In dev/test environments, the upstream services are stubbed out using the [customs-financials-hods-stub](https://github.com/hmrc/customs-financials-hods-stub/).

## Available Routes

You can find a list of microservice specific routes here - `/conf/app.routes`

Application entrypoint:  `/customs/payment-records`

| Path                                                                        | Description                                                  |
|-----------------------------------------------------------------------------|--------------------------------------------------------------|
| POST   /customs-financials-api/eori/accounts                                | Request to retrieve accounts & balances                      |                
| POST   /customs-financials-api/historic-document-request                    | Request to retrieve historic document                        |                
| POST   /customs-financials-api/account/guarantee/open-transactions          | Request to retrieve guarantee transactions summary           |                
| POST   /customs-financials-api/account/guarantee/open-transactions-detail   | Request to retrieve guarantee transactions detail            |                
| POST   /customs-financials-api/account/cash/transactions                    | Request to retrieve cash transactions summary                |                
| POST   /customs-financials-api/account/cash/transactions-detail             | Request to retrieve cash transactions detail                 |                
| GET    /customs-financials-api/account-authorities                          | Request to retrieve account authorities                      |                
| POST   /customs-financials-api/account-authorities/grant                    | Request to grant authority on account                        |                
| POST   /customs-financials-api/account-authorities/revoke                   | Request to revoke authority on account                       |                
| POST   /customs-financials-api/duty-deferment/update-contact-details        | Request to update duty deferment contact details             |                
| POST   /customs-financials-api/duty-deferment/contact-details               | Request to retrieve duty deferment contact details           |                
| GET    /customs-financials-api/eori/:eori/validate                          | Request to validate EORI                                     |                
| POST   /customs-financials-api/eori/validate                                | Request to validate EORI                                     |                
| GET    /customs-financials-api/eori/:eori/notifications                     | Request to retrieve notifications for given EORI             |                
| DELETE /customs-financials-api/eori/:eori/notifications/:fileRole           | Request to delete non requested notifications for given EORI |                
| DELETE /customs-financials-api/eori/:eori/requested-notifications/:fileRole | Request to delete requested notifications for given EORI     |                

### POST  /customs-financials-api/eori/accounts

#### Request headers specification:
| HTTP Header  | Acceptable value              |
|--------------|-------------------------------|
| Content-Type | application/json              |
| Accept       | application/vnd.hmrc.1.0+json |

#### Example Request body:
```json
{
   "accountsAndBalancesRequest":{
      "requestCommon":{
         "receiptDate":"2020-10-05T09:30:47Z",
         "acknowledgementReference":"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
         "regime":"CDS"
      },
      "requestDetail":{
         "EORINo":"GB345129062307218",
         "accountType":"DutyDeferment",
         "referenceDate":"2020-10-05T09:30:47Z"
      }
   }
}
```

#### Example Response body:
```json
{
   "accountsAndBalancesResponse":{
      "responseDetail":{
         "EORINo":"GB345129062307218",
         "accountType":"DutyDeferment",
         "referenceDate":"2020-10-05T09:30:47Z",
         "dutyDefermentAccount":[
            {
               "account":{
                  "number":"452961268",
                  "type":"DutyDeferment",
                  "accountStatus":"Open",
                  "isleOfManFlag":true,
                  "owner":"X20JTM",
                  "viewBalanceIsGranted":true
               },
               "limits":{
                  "periodGuaranteeLimit":"99999999999.99",
                  "periodAccountLimit":"99999999999.99"
               },
               "balances":{
                  "periodAvailableGuaranteeBalance":"99999999999.99",
                  "periodAvailableAccountBalance":"99999999999.99"
               }
            },
            {
               "account":{
                  "number":"652961299",
                  "type":"DutyDeferment",
                  "accountStatus":"Open",
                  "isleOfManFlag":true,
                  "owner":"X20JTM",
                  "viewBalanceIsGranted":true
               },
               "limits":{
                  "periodGuaranteeLimit":"99999999999.99",
                  "periodAccountLimit":"99999999999.99"
               },
               "balances":{
                  "periodAvailableGuaranteeBalance":"99999999999.99",
                  "periodAvailableAccountBalance":"99999999999.99"
               }
            }
         ]
      }
   }
}
```

#### Response code specification:
* **200** If the request is processed successful
* **400** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request
* **500** In case of a system error such as time out, server down etc, thishttps status code will be returned

### POST  /customs-financials-api/historic-document-request

#### Request headers specification:
| HTTP Header  | Acceptable value              |
|--------------|-------------------------------|
| Content-Type | application/json              |
| Accept       | application/vnd.hmrc.1.0+json |

### Request body
```json
 {"documentType":"C79Certificate","from":"2019-01-15","until":"2019-03-16"}

```
### Response body
```json
{
"eoriHistory": [
  {
    "eori": "historicEori1", 
    "validFrom": "2001-01-20T00:00:00Z", 
    "validTo": "2001-01-20T00:00:00Z"
  },
  {
    "eori": "historicEori2",
    "validFrom": "2001-01-20T00:00:00Z",
    "validTo": "2001-01-20T00:00:00Z"
  }
]
}
```

#### Response code specification:
* **200** If the request is processed successful and a resource is created
* **400** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request

### POST /customs-financials-api/account/guarantee/open-transactions

#### Request headers specification:
| HTTP Header  | Acceptable value              |
|--------------|-------------------------------|
| Content-Type | application/json              |
| Accept       | application/vnd.hmrc.1.0+json |

### Request body
```json
 {"gan":"gan"}
```
### Response body
```json
[
   {
      "date":"someDate",
      "movementReferenceNumber":"mrn",
      "balance":"100.00",
      "uniqueConsignmentReference":"UCR",
      "declarantEori":"Declarant EORI",
      "consigneeEori":"Consignee EORI",
      "originalCharge":"200.00",
      "dischargedAmount":"300.00",
      "dueDates":[
         
      ]
   }
]
```

#### Response code specification:
* **200** If the request is processed successful and a resource is created
* **400** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request

### POST /customs-financials-api/account/guarantee/open-transactions-detail

#### Request headers specification:
| HTTP Header  | Acceptable value              |
|--------------|-------------------------------|
| Content-Type | application/json              |
| Accept       | application/vnd.hmrc.1.0+json |

### Request body
```json
 {"gan":"gan"}
```

### Response body
```json
[
   {
      "date":"someDate2",
      "movementReferenceNumber":"mrn",
      "balance":"balance 1",
      "uniqueConsignmentReference":"UCR",
      "declarantEori":"Declarant EORI",
      "consigneeEori":"Consignee EORI",
      "originalCharge":"charge 1",
      "dischargedAmount":"5.12",
      "interestCharge":"interest rate 1",
      "c18Reference":"C18-Ref1",
      "dueDates":[
         {
            "dueDate":"dueDate",
            "reasonForSecurity":"reason1",
            "amounts":{
               "openAmount":"450.00",
               "totalAmount":"600.00",
               "clearedAmount":"150.00",
               "updateDate":"2020-08-03"
            },
            "taxTypeGroups":[
               {
                  "taxTypeGroup":"B",
                  "amounts":{
                     "openAmount":"400.00",
                     "totalAmount":"700.00",
                     "clearedAmount":"300.00",
                     "updateDate":"2020-08-02"
                  },
                  "taxType":{
                     "taxType":"taxType1",
                     "amounts":{
                        "openAmount":"600.00",
                        "totalAmount":"800.00",
                        "clearedAmount":"200.00",
                        "updateDate":"2020-08-01"
                     }
                  }
               }
            ]
         }
      ]
   }
]
```

#### Response code specification:
* **200** If the request is processed successful and a resource is created
* **400** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request


### POST /customs-financials-api/account/cash/transactions

#### Request headers specification:
| HTTP Header  | Acceptable value              |
|--------------|-------------------------------|
| Content-Type | application/json              |
| Accept       | application/vnd.hmrc.1.0+json |

### Request body
```json
 {"can":"can1","from":"2020-01-01","to":"2020-06-01"}
```

### Response body
```json
{
   "pendingTransactions":[
      {
         "movementReferenceNumber":"pendingDeclarationID",
         "declarantEori":"pendingDeclarantEORINumber",
         "declarantReference":"pendingDeclarantReference",
         "date":"pendingPostingDate",
         "amount":"pendingAmount",
         "taxGroups":[
            
         ]
      }
   ],
   "cashDailyStatements":[
      {
         "date":"date",
         "openingBalance":"openingBalance",
         "closingBalance":"closingBalance",
         "declarations":[
            {
               "movementReferenceNumber":"mrn",
               "declarantEori":"declarantEori",
               "declarantReference":"declarantReference",
               "date":"postingDate",
               "amount":"amount",
               "taxGroups":[
                  
               ]
            }
         ],
         "otherTransactions":[
            {
               "amount":"12.34",
               "transactionType":"Payment"
            },
            {
               "amount":"12.34",
               "transactionType":"Withdrawal",
               "bankAccountNumber":"77665544"
            }
         ]
      }
   ]
}
```

#### Response code specification:
* **200** If the request is processed successful and a resource is created
* **400** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request

### POST /customs-financials-api/account/cash/transactions-detail

#### Request headers specification:
| HTTP Header  | Acceptable value              |
|--------------|-------------------------------|
| Content-Type | application/json              |
| Accept       | application/vnd.hmrc.1.0+json |

### Request body
```json
 {"can":"can1","from":"2020-01-01","to":"2020-06-01"}
```

### Response body
```json
{
   "pendingTransactions":[
      {
         "movementReferenceNumber":"pendingDeclarationID",
         "declarantEori":"pendingDeclarantEORINumber",
         "declarantReference":"pendingDeclarantReference",
         "date":"pendingPostingDate",
         "amount":"pendingAmount",
         "taxGroups":[
            
         ]
      }
   ],
   "cashDailyStatements":[
      {
         "date":"date",
         "openingBalance":"openingBalance",
         "closingBalance":"closingBalance",
         "declarations":[
            {
               "movementReferenceNumber":"mrn",
               "declarantEori":"declarantEori",
               "declarantReference":"declarantReference",
               "date":"postingDate",
               "amount":"amount",
               "taxGroups":[
                  {
                     "taxTypeGroup":"VAT",
                     "amount":"-456.78"
                  },
                  {
                     "taxTypeGroup":"Excise",
                     "amount":"-789.01"
                  }
               ]
            }
         ],
         "otherTransactions":[
            {
               "amount":"12.34",
               "transactionType":"Payment"
            },
            {
               "amount":"12.34",
               "transactionType":"Withdrawal",
               "bankAccountNumber":"77665544"
            }
         ]
      }
   ]
}
```

#### Response code specification:
* **200** If the request is processed successful and a resource is created
* **400** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request

### GET /customs-financials-api/account-authorities

#### Request headers specification:
| HTTP Header  | Acceptable value              |
|--------------|-------------------------------|
| Content-Type | application/json              |
| Accept       | application/vnd.hmrc.1.0+json |

### Response body
```json
[
   {
      "accountType":"CDSCash",
      "accountNumber":"123456",
      "accountStatus":"Open",
      "authorities":[
         {
            "authorisedEori":"Agent EORI",
            "authorisedFromDate":"from date",
            "authorisedToDate":"to date",
            "viewBalance":false
         }
      ]
   }
]
```

#### Response code specification:
* **200** If the request is processed successful and a resource is created
* **400** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request

### POST /customs-financials-api/account-authorities/grant

#### Request headers specification:
| HTTP Header  | Acceptable value              |
|--------------|-------------------------------|
| Content-Type | application/json              |
| Accept       | application/vnd.hmrc.1.0+json |

### Request body
```json
 {"accounts":{"cash":"345","dutyDeferments":["123","754"],"guarantee":"54345"},"authority":{"authorisedEori":"authorisedEori","authorisedFromDate":"2018-11-09","viewBalance":true},"authorisedUser":{"userName":"some name","userRole":"some role"},"editRequest":false}
```

### Response body
```
Returns true/false
```

#### Response code specification:
* **200** If the request is processed successful and a resource is created
* **400** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request

### POST /customs-financials-api/account-authorities/revoke

#### Request headers specification:
| HTTP Header  | Acceptable value              |
|--------------|-------------------------------|
| Content-Type | application/json              |
| Accept       | application/vnd.hmrc.1.0+json |

### Request body
```json
 {"accountNumber":"123","accountType":"CDSCash","authorisedEori":"authorisedEori","authorisedUser":{"userName":"some name","userRole":"some role"}}
```
### Response body
```
Returns true/false
```

#### Response code specification:
* **200** If the request is processed successful and a resource is created
* **400** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request

### POST /customs-financials-api/duty-deferment/update-contact-details

#### Request headers specification:
| HTTP Header  | Acceptable value              |
|--------------|-------------------------------|
| Content-Type | application/json              |
| Accept       | application/vnd.hmrc.1.0+json |

### Request body
```json
{"dan":"1234567","eori":"testEORI","name":"CHANGED MYNAME","addressLine1":"New Road","addressLine3":"Edinburgh","postCode":"AB12 3CD","countryCode":"GB","email":"email@email.com"}
```
### Response body
```json
 {"amendCorrespondenceAddressResponse":{"responseCommon":{"status":"OK","processingDate":"2020-10-05T09:30:47Z"}}}
```

#### Response code specification:
* **200** If the request is processed successful and a resource is created
* **400** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request

### POST /customs-financials-api/duty-deferment/contact-details

#### Request headers specification:
| HTTP Header  | Acceptable value              |
|--------------|-------------------------------|
| Content-Type | application/json              |
| Accept       | application/vnd.hmrc.1.0+json |

### Request body
```json
{"dan":"1234567","eori":"testEORI"}
```
### Response body
```json
 {"getCorrespondenceAddressResponse":{"responseCommon":{"status":"OK","processingDate":""}}}
```

#### Response code specification:
* **200** If the request is processed successful and a resource is created
* **400** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request

### GET /customs-financials-api/eori/:eori/validate

#### Request headers specification:
| HTTP Header  | Acceptable value              |
|--------------|-------------------------------|
| Content-Type | application/json              |
| Accept       | application/vnd.hmrc.1.0+json |

### Response body
```json
 {"getCorrespondenceAddressResponse":{"responseCommon":{"status":"OK","processingDate":""}}}
```

### POST /customs-financials-api/eori/validate

#### Request headers specification:
| HTTP Header  | Acceptable value              |
|--------------|-------------------------------|
| Content-Type | application/json              |
| Accept       | application/vnd.hmrc.1.0+json |

### Request body
```json
{"eori":"testEORI"}
```

### Response
200 or 404

### Intermediate SUB09 Response
```json
{
  "subscriptionDisplayResponse": {
    "responseCommon": {
      "status": "OK",
      "processingDate": "2016-08-17T19:33:47Z",
      "statusText": "Optional status text from ETMP",
      "returnParameters": [
        {
          "paramName": "POSITION",
          "paramValue": "LINK"
        }
      ]
    }
  }
}
```

#### Response code specification:
* **200** If the request is processed successful and a resource is created
* **400** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request

### GET /customs-financials-api/eori/:eori/notifications/

#### Request headers specification:
| HTTP Header  | Acceptable value              |
|--------------|-------------------------------|
| Content-Type | application/json              |
| Accept       | application/vnd.hmrc.1.0+json |

### Response body
```json
 {"eori":"123456789","notifications":[{"eori":"123456789","fileRole":"C79Certificate","fileName":"abc.csv","fileSize":1000,"metadata":{"downloadURL":"http://localhost/abc.csv","fileRole":"C79Certificate","fileName":"abc.csv","periodStartMonth":"1","periodStartYear":"2019","fileType":"csv","fileSize":"1000"}}],"lastUpdated":{"$date":{"$numberLong":"1625652329352"}}}
```
#### Response code specification:
* **200** If the request is processed successful and a resource is created
* **400** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request


### DELETE /customs-financials-api/eori/:eori/notifications/:fileRole

#### Request headers specification:
| HTTP Header  | Acceptable value              |
|--------------|-------------------------------|
| Content-Type | application/json              |
| Accept       | application/vnd.hmrc.1.0+json |

#### Response code specification:
* **200** If the request is processed successful and a resource is created
* **403** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request


### DELETE /customs-financials-api/eori/:eori/requested-notifications/:fileRole

#### Request headers specification:
| HTTP Header  | Acceptable value              |
|--------------|-------------------------------|
| Content-Type | application/json              |
| Accept       | application/vnd.hmrc.1.0+json |

#### Response code specification:
* **200** If the request is processed successful and a resource is created
* **403** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request



## Running tests
 
There is just one test source tree in the `test` folder. Use `sbt test` to run them.

To get a unit test coverage report, you can run `sbt clean coverage test coverageReport`,
then open the resulting coverage report `target/scala-2.12/scoverage-report/index.html` in a web browser.

Test coverage threshold is set at 90% - so if you commit any significant amount of implementation code without writing 
tests, you can expect the build to fail.

## All tests and checks
This is a sbt command alias specific to this project. It will run a scala style check, run unit tests, run integration
tests and produce a coverage report:
> `sbt runAllChecks`
