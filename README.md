# octrans
Spring Boot Application to track OC Transpo Bus Cancellations
(c) 2019- Ram Darbha. All Rights Reserved.

OC Transpo is the public transit system of Ottawa, Canada.

This application uses JDBC to interface to an Azure SQL DB containing data about cancelled OC Transpo buses.
(The SQL DB is populated in realtime by another Azure application that is triggered by tweets from the OC
Transpo account, outside the scope of this project.)  This application presents a REST API in the frontend to
allow a user to determine cancellations of specific bus routes,  the most or least cancelled buses, or number
of minutes delay per bus route.  Each API call can be  restricted to time of day (AM, PM or all day) and days
(fenced by start date and end date).  Start and end date parameters are optional.  If only start date is
given, query operates from that date to the present.  If only end date is given, query operates from earliest
time to that date.  If both dates are given, query operates within that interval.  If they are both
unspecified, query operates over all DB contents from time of inception (August 2018) to the present.  Note,
start and end date must be in the format "yyyy-mm-dd", with leading zeroes as needed, e.g. `2018-2-17` is
invalid but `2018-02-17` is valid.

## REST API

Base URL: `/ocapi/v1/`

Broadly, the API supports retrieving the following:

- `<base>/bus/...`: list of cancellations for a given bus in given timeframe.
- `<base>/count/(most|least)/...`: #cancellations for each bus route, in given timeframe.
- `<base>/delay/(most|least)/...`: total delay (in minutes) for each bus route, in given timeframe.


Below are examples of API invocations and corresponding responses.  Note the API only supports HTTP GET,
that is PUT, POST, DELETE are not supported.


### Bus API

API: `ocapi/v1/bus/<busnum>/(am|pm|all)[?[sd=yyyy-mm-dd&]ed=yyyy-mm-dd]`


To find all cancellations of bus route 265:
 
`ocapi/v1/bus/265/all`

To find morning cancellations of bus route 265:
 
`ocapi/v1/bus/265/am`

To find evening cancellations of bus route 265 since 1-Feb-2019:
 
`ocapi/v1/bus/265/pm?sd=2019-02-01`

To find evening cancellations of bus route 265 between 1-Nov-18 and 31-Dec-18:
 
`ocapi/v1/bus/265/pm?sd=2018-11-01&ed=2018-12-31`

#### Response JSON

For all `bus` invocations, the response body is a JSON array, each array element having fields about a specific cancellation: bus number, bus name, start date, start location and delay in minutes to the next bus.

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


### Count API

The `count` API is useful to compare bus routes by their #cancellations, e.g. route 269 had 11 cancellations this month, while route 252 had 16 cancellations.  It has 2 flavours: `most` and `least`.

#### Most Cancellations

API: `ocapi/v1/count/most/<num>/(am|pm|all)[?[sd=yyyy-mm-dd&]ed=yyyy-mm-dd]`

To find top 8 routes with the most #cancellations over the entire day:
 
`ocapi/v1/count/most/8/all`

To find top 6 routes with the most #morning cancellations in September 2018:
 
`ocapi/v1/count/most/6/am?sd=2018-09-01&ed=2018-09-30`

To find top 10 routes with the most #afternoon cancellations starting November 2018:
 
`ocapi/v1/count/most/10/pm?sd=2018-11-01`


#### Least Cancellations

API: `ocapi/v1/count/least/<num>/(am|pm|all)[?[sd=yyyy-mm-dd&]ed=yyyy-mm-dd]`

To find bottom 5 routes with the least afternoon cancellations:
 
`ocapi/v1/count/least/5/pm`

To find bottom 5 routes with least #morning cancellations until October 2018:
 
`ocapi/v1/count/least/5/am?ed=2018-10-31`

Note, if `<num>` is zero, response contains _all_ routes, i.e. `count/most` and `count/least` APIs return the
same results although ordered differently.  The am/pm/all flag and start/end dates work
identically for all APIs, the examples above being different only to illustrate the variety of possible
queries.

#### Response JSON

Both `count/most` and `count/least` APIs return JSON structures of #cancellations by route, like so:

    [
      {"busnumber":198, "value":15},
      {"busnumber":70, "value":13},
      {"busnumber":84, "value":12},
      {"busnumber":265, "value":9},
      {"busnumber":291, "value":5}
    ]


### Delay API

The `delay` API is useful to compare bus routes by the total delay incurred by cancellations on that route,
e.g. route 269 had 152 minutes of delay this month, while route 252 had 116 minutes delay.  The delay is the
aggregate of the 'next bus in' minutes metrics of the corresponding cancellations.

Aggregating delay is useful when counting #cancellations is misleading. E.g. suppose route 100 had 2 
cancellations this month (each incurred a delay of 20 minutes).  If route 122 had 6 cancellations, that
sounds worse than route 100 at first glance. But if each cancellation only incurred a 5 minute delay,
route 122 had a total delay of 30 minutes, v/s 40 minutes on route 100.  This suggests that the impact
of cancellations is more severe on route 100, even though it has fewer #cancellations.

#### Most Total Delay

API: `ocapi/v1/delay/most/<num>/(am|pm|all)[?[sd=yyyy-mm-dd&]ed=yyyy-mm-dd]`

To find 8 routes with the most delay over all time:
 
`ocapi/v1/delay/most/8/all`

To find 6 routes with the most morning delays in September 2018:
 
`ocapi/v1/delay/most/6/am?sd=2018-09-01&ed=2018-09-30`

To list all routes by their afternoon delays, from the beginning until 5-Dec-18:
 
`ocapi/v1/delay/most/0/pm?ed=2018-12-05`


#### Least Total Delay

API: `ocapi/v1/delay/least/<num>/(am|pm|all)[?[sd=yyyy-mm-dd&]ed=yyyy-mm-dd]`

To find 5 routes with the least afternoon delays over all days:
 
`ocapi/v1/delay/least/5/pm`

To list all routes by their morning delays, starting 1-Nov-18 to the present:
 
`ocapi/v1/delay/least/0/am?sd=2018-11-01`

Note, setting `<num>` to zero returns delays for _all_ routes, i.e. `delay/most` and `delay/least` APIs
return the same results, although ordered differently.

#### Response JSON

The delay APIs return the total delay in minutes per route, like so:

    [
      {"busnumber":198, "value":1521},
      {"busnumber":70, "value":1303},
      {"busnumber":84, "value":1211},
      {"busnumber":265, "value":993},
      {"busnumber":291, "value":572}
    ]


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

## Troubleshooting

### Eclipse dependencies not found
If Eclipse failing to find dependencies even though `build.gradle` is up to date, it is likely because Eclipse `.classpath` is out of date.  To update Eclipse project:

    $ ./gradlew cleanEclipse
    $ ./gradlew eclipse

### HTTP Requests returning 404
If a class has been annotated `@RestController` but HTTP Requests still fail, it is likely because that class isn't being found by Spring.  Recommended Spring Boot approach is to have the top-level application (`@SpringBootApplication`) class in a non-default package (e.g. `app`), the controller and other classes in sub-packages (e.g. `app.controller`, `app.model` etc.).  If that's not feasible, alternately, the top-level class can be explicitly annotated by pointing it to class(es) to scan:

    @SpringBootApplication
    @ComponentScan(basePackageClasses = ApiV1Controller.class)
    public class Application {
