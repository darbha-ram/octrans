# octranpso
Spring Boot Application to track OC Transpo Bus Cancellations

(c) 2019- Ram Darbha. All Rights Reserved.

OC Transpo is the public transit system of Ottawa, Canada.

This application uses JDBC to interface to an Azure SQL DB containing data about cancelled OC Transpo buses.  (The Azure DB is populated in realtime by another Azure application that is triggered by tweets from the OC Transpo account, outside the scope of this project.)  This application presents a REST API in the frontend to allow a user to determine cancellations of specific bus routes,  the most or least cancelled buses.  Each API call can be restricted to AM or PM or all day, and to a specific time interval (fenced by start date and end date).  Leaving the start and end date parameters unspecified results in a query over all the DB contents from time of inception (August 2018) to the latest available data.

## REST API

Below are examples of API invocations and corresponding responses.  Note the API only supports HTTP GET, that is PUT, POST, DELETE are not supported.

Base URL: `/ocapi/v1/`

### Specified Bus

To find all cancellations of bus route 265:
 
`ocapi/v1/bus/265/all`

To find morning cancellations of bus route 265:
 
`ocapi/v1/bus/265/am`

To find evening cancellations of bus route 265:
 
`ocapi/v1/bus/265/pm`

To find all evening cancellations between 1-Nov-18 and 31-Dec-18:
 
`ocapi/v1/bus/265/pm?sd=1-11-2018&ed=31-12-2018`

For all above invocations, the response body is a JSON array, each element of which has information fields about a specific cancellation of the requested route: bus number, bus name, startdate, start location and delay to the next bus.

    [
      {
        "busnumber": 256,
        "busname": "Bridlewood",
        "starttime": "2018-10-09T16:36:00",
        "startloc": "Mackenzie King bridge",
        "nextminutes": 9
      },
      {
        "busnumber": 256,
        "busname": "Bridlewood",
        "starttime": "2018-10-01T15:35:00",
        "startloc": "Mackenzie King bridge",
        "nextminutes": 15
      }
    ]

### Most Cancellations

To find 8 routes with the most cancellations:
 
`ocapi/v1/most/8/all`

To find 6 routes with the most morning cancellations:
 
`ocapi/v1/most/6/am`

To find 10 routes with the most afternoon cancellations between 1-Nov-18 and 31-Dec-18:
 
`ocapi/v1/most/10/pm?sd=1-11-2018&ed=31-12-2018`


### Least Cancellations

To find 5 routes with the least cancellations:
 
`ocapi/v1/least/5/all`

To find 3 routes with the least morning cancellations between 1-Nov-18 and 31-Dec-18:
 
`ocapi/v1/least/3/am?sd=1-11-2018&ed=31-12-2018`



## Dev/Build Environment

- Spring Tool Suite 3.9.7
- Gradle 5.1.1
- JDK 1.8

## To Build and Deploy

This project is setup to build with gradle.

    $ git clone 
    $ cd octranspo
    $ ./gradlew build
    $ java -jar build/libs/ramd-octrans-0.2.0.jar

After service is started, issue HTTP request to port 8080, e.g. `http://localhost:8080/ocapi/v1/bus/95/all`.
