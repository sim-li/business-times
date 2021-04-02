# BusinessTimes (aka OpeningHours)

## How to run
- Download SBT or aquire it via package manager https://www.scala-sbt.org/download.html
- Execute `sbt run` in the project directory
- You're good to go, server should be running on `localhost:9000`
- (Optional) Use `sbt test` to run the test suites 

## Endpoint
You can CURL against `localhost:9000/businessTimes/render` using a `GET` request with JSON body. 
The server will respond in `text/plain` format.

You will find some example `CURL` requests at the end of the document which can be used to quickly test the endpoints. 

## Answer to question about the data format
The current format is:
```
    [
        (WEEKDAY as String): { 
            type:  ("open"|"close"), 
            value: (EPOCH as Number)
        }
        ...
    ] 
```
I like the generic approach of representing opening and closing times as a series of 
JS objects that could be seen like events describing how the "opening state" of the restaurant changes
over time.

This is definately a nice idea. I see potential for an universal format that can be 
interpreted and transformed easily by consumers. Ideally it would be expressive enough to 
be used for such a variety of use cases that we wouldn't have to modify it very often.

To fulfill these expectations it would have to leave little room for wrong interpretations.
Also it would have to be easy to parse and transform.
Sadly, this isn't the case. 

Here is where I see some room for improvement:
- A list of opening/closing time objects are grouped week day. Because of this we can't just see it as a series of events we can order by time. The weekday "buckets" add implicit
  rules to the format that are impossible to guess. The best example for this is that the corresponding closing time 
  for an opening on a weekday may be found grouped within a different weekday bucket. Rules like that are easily 
  overlooked and may lead to expensive mistakes, even if thoroughly documented. 
  
  *Suggestion*: Use a list without weekday grouping and move the weekday information into the objects of the list.


- Opening and closing times are not grouped. We have to guess which closing time corresponds to which opening time
and we could guess wrongly (e.g. wrong bucket, wrong ordering). 
  
  *Suggestion*: Group opening and closing time into one object.

I see the ideal format as something similar to the timeslots of a calendar. The difference is that we drop the concept
of an absolute date. Instead we just work with relative time slots and weekdays. I would like to keep the idea of seeing
business times as a series of events and would model it like this:

```
    businessTimes: [
       {    
            //opening & closing are mandatory fields
            opening: {
                //weekDay & epochTime are mandatory fields
                weekDay: ("monday".."friday"),
                epochTime: (EPOCH as Number),
            }
            closing: {
                //weekDay & epochTime are mandatory fields
                weekDay: ("monday".."friday"),
                epochTime: (EPOCH as Number),
            }
        }` 
        ...
    ] 
    utcOffset: (OFFSET as Number)
```

Advantages:
- Opening and closing times are mandatory and grouped as an object. We know which two times correspond 
to each other and thus create something like a `timeslot` where the business is open
- Weekday information is in every opening and closing time (where a description of relative time takes place).
We can easily and explicity express that a restaurant opens on one day and closes on another day.
- We have a list that we can easily sort by a combination of weekday & epochtime.

## Additional notes
- I modified the formatted output from what was specified on the task instructions to display minutes aswell, since 
I think this is a very likely use case (e.g. restaurant opening at 9:30)
- Endpoints for monitoring/metrics plus deployment scripts are missing and could be the next steps for a proper service
running on a production environment.

## Example requests
### Examples from task description
- ```
  curl --location --request GET 'localhost:9000/businessTimes/render' \
  --header 'Content-Type: application/json' \
  --data-raw '{
      "monday": [{
          "type": "open",
          "value": 32400
      },
      {
          "type": "close",
          "value": 72000
      }
    ]
  }'
  ```

- 
  ```
  curl --location --request GET 'localhost:9000/businessTimes/render' \
  --header 'Content-Type: application/json' --data-raw '{
    "friday": [
        {
            "type": "open",
            "value": 64800
        }
    ],
    "saturday": [
        {
            "type": "close",
            "value": 3600
        }, 
        {
            "type": "open",
            "value": 32400
        },
        {
            "type": "close",
            "value": 39600
        },
        {
            "type": "open",
            "value": 57600
        },
        {
            "type": "close",
            "value": 82800
        }
      ]
  }'
  ```

- ```
  curl --location --request GET 'localhost:9000/businessTimes/render' --header 'Content-Type: application/json' --data-raw '{
    "monday": [],
    "tuesday": [
        {
            "type": "open",
            "value": 36000
        },
        {
            "type": "close",
            "value": 64800
        }
    ],
    "wednesday": [],
    "thursday": [
        {
            "type": "open",
            "value": 36000
        },
        {
            "type": "close",
            "value": 64800
        }
    ],
    "friday": [
        {
            "type": "open",
            "value": 36000
        }
    ],
    "saturday": [
        {
            "type": "close",
            "value": 3600
        },
        {
            "type": "open",
            "value": 36000
        }
    ],
    "sunday": [
        {
            "type": "close",
            "value": 3600
        },
        {
            "type": "open",
            "value": 43200
        },
        {
            "type": "close",
            "value": 75600
        }
    ]
    }'
  ```

### Faulty requests to test error handling
- Wrong field names
  ```
  curl --location --request GET 'localhost:9000/businessTimes/render' \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "monday": [
        {
            "wrong": "open",
            "fields": 32400
        },
        {
            "are": "open",
            "everywhere": 72000
        }
    ]
  }'
  ```
  
- No closing time given
  ```
    curl --location --request GET 'localhost:9000/businessTimes/render' \
    --header 'Content-Type: application/json' \
    --data-raw '{ 
        "monday": [
            {
                "type": "open",
                "value": 36000
            },
            {
                "type": "open",
                "value": 64800
            }
        ]
    }'
    ```

- Number of opening and closing times don't make sense
  ```
    curl --location --request GET 'localhost:9000/businessTimes/render' \
    --header 'Content-Type: application/json' \
    --data-raw '{
        "monday": [
            {
                "type": "open",
                "value": 36000
            },
            {
                "type": "close",
                "value": 64800
            },
            {
                "type": "close",
                "value": 70000
            }
        ]
    }'
  ```
