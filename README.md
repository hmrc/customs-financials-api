
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
* DEC64 Submit file upload

In dev/test environments, the upstream services are stubbed out using the [customs-financials-hods-stub](https://github.com/hmrc/customs-financials-hods-stub/).

| Path                                                                       | Description                                                                                       |
|----------------------------------------------------------------------------|------------------------------------------------------------------------------------------------- |
| POST   /customs-financials-api/eori/accounts                               | Request to retrieve accounts & balances                                                     |                
| POST   /customs-financials-api/historic-document-request                   | Request to retrieve historic document                                                                    |                
| POST   /customs-financials-api/account/guarantee/open-transactions         | Request to retrieve guarantee transactions summary                                                                         |                
| POST   /customs-financials-api/account/guarantee/open-transactions-detail  | Request to retrieve guarantee transactions detail                                                                          |                
| POST   /customs-financials-api/account/cash/transactions                   | Request to retrieve cash transactions summary                                                                         |                
| POST   /customs-financials-api/account/cash/transactions-detail            | Request to retrieve cash transactions detail                                                                         |                
| GET    /customs-financials-api/account-authorities                         | Request to retrieve account authorities                                                                         |                
| POST   /customs-financials-api/account-authorities/grant                   | Request to grant authority on account                                                                         |                
| POST   /customs-financials-api/account-authorities/revoke                  | Request to revoke authority on account                                                                         |                
| POST   /customs-financials-api/duty-deferment/update-contact-details       | Request to update duty deferment contact details                                                                        |                
| POST   /customs-financials-api/duty-deferment/contact-details              | Request to retrieve duty deferment contact details                                                                         |                
| GET    /customs-financials-api/eori/:eori/validate                         | Request to validate EORI                                                                         |                
| POST   /customs-financials-api/eori/validate                               | Request to validate EORI                                                                         |                
| GET    /customs-financials-api/eori/:eori/notifications                    | Request to retrieve notifications for given EORI                                                                         |                
| DELETE /customs-financials-api/eori/:eori/notifications/:fileRole          | Request to delete non requested notifications for given EORI                                                                         |                
| DELETE /customs-financials-api/eori/:eori/requested-notifications/:fileRole | Request to delete requested notifications for given EORI                                                                         |                

### POST  /customs-financials-api/eori/accounts

#### Request headers specification:
HTTP Header | Acceptable value
------------|-----------------
Content-Type | application/json
Accept | application/vnd.hmrc.1.0+json


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
HTTP Header | Acceptable value
------------|-----------------
Content-Type | application/json
Accept | application/vnd.hmrc.1.0+json

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
HTTP Header | Acceptable value
------------|-----------------
Content-Type | application/json
Accept | application/vnd.hmrc.1.0+json

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
HTTP Header | Acceptable value
------------|-----------------
Content-Type | application/json
Accept | application/vnd.hmrc.1.0+json

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
HTTP Header | Acceptable value
------------|-----------------
Content-Type | application/json
Accept | application/vnd.hmrc.1.0+json

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
HTTP Header | Acceptable value
------------|-----------------
Content-Type | application/json
Accept | application/vnd.hmrc.1.0+json

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
HTTP Header | Acceptable value
------------|-----------------
Content-Type | application/json
Accept | application/vnd.hmrc.1.0+json

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
HTTP Header | Acceptable value
------------|-----------------
Content-Type | application/json
Accept | application/vnd.hmrc.1.0+json
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
HTTP Header | Acceptable value
------------|-----------------
Content-Type | application/json
Accept | application/vnd.hmrc.1.0+json
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
HTTP Header | Acceptable value
------------|-----------------
Content-Type | application/json
Accept | application/vnd.hmrc.1.0+json
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
HTTP Header | Acceptable value
------------|-----------------
Content-Type | application/json
Accept | application/vnd.hmrc.1.0+json
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
HTTP Header | Acceptable value
------------|-----------------
Content-Type | application/json
Accept | application/vnd.hmrc.1.0+json

### Response body
```json
 {"getCorrespondenceAddressResponse":{"responseCommon":{"status":"OK","processingDate":""}}}
```

### POST /customs-financials-api/eori/validate

#### Request headers specification:
HTTP Header | Acceptable value
------------|-----------------
Content-Type | application/json
Accept | application/vnd.hmrc.1.0+json

### Request body
```json
{"eori":"testEORI"}
```

### Response body
```json
 {"getCorrespondenceAddressResponse":{"responseCommon":{"status":"OK","processingDate":""}}}
```

#### Response code specification:
* **200** If the request is processed successful and a resource is created
* **400** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request

### GET /customs-financials-api/eori/:eori/notifications/

#### Request headers specification:
HTTP Header | Acceptable value
------------|-----------------
Content-Type | application/json
Accept | application/vnd.hmrc.1.0+json
### Response body
```json
 {"eori":"123456789","notifications":[{"eori":"123456789","fileRole":"C79Certificate","fileName":"abc.csv","fileSize":1000,"metadata":{"downloadURL":"http://localhost/abc.csv","fileRole":"C79Certificate","fileName":"abc.csv","periodStartMonth":"1","periodStartYear":"2019","fileType":"csv","fileSize":"1000"}}],"lastUpdated":{"$date":{"$numberLong":"1625652329352"}}}
```
#### Response code specification:
* **200** If the request is processed successful and a resource is created
* **400** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request


### DELETE /customs-financials-api/eori/:eori/notifications/:fileRole

#### Request headers specification:
HTTP Header | Acceptable value
------------|-----------------
Content-Type | application/json
Accept | application/vnd.hmrc.1.0+json

#### Response code specification:
* **200** If the request is processed successful and a resource is created
* **403** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request


### DELETE /customs-financials-api/eori/:eori/requested-notifications/:fileRole

#### Request headers specification:
HTTP Header | Acceptable value
------------|-----------------
Content-Type | application/json
Accept | application/vnd.hmrc.1.0+json

#### Response code specification:
* **200** If the request is processed successful and a resource is created
* **403** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request

## Running the application locally

`sbt "run 9878"` to start in `DEV` mode or 
`sbt "start -Dhttp.port=9878"` to run in `PROD` mode.
`sm --start CUSTOMS_FINANCIALS_TESTS` for dependent services to be ran via service manager.

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
