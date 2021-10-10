**Steps to run the project locally:**

Before starting make sure you have java installed, and any IDE.

1) clone the repo from git hub (https://github.com/gowtham181998/vrbo)
2) open the project in any IDE (as maven project)
3) run the DemoApplication.java
4) End points exposed:
   * http://localhost:8081/vrbo/getClosestListings/{place/location}/{radius}
   * http://localhost:8081/vrbo/threeDatesWithHighestPrice/{place/location}/{radius}
   * http://localhost:8081/vrbo/perNightPricesForNextOneYear/{place/location}/{radius}

**Implemented 3 API's:**

***getClosestListings/** - will return a csv file when you hit the end point from browser, with some info for each
nearby place.
***threeDatesWithHighestPrice/** - will return a csv file containing three dates with the highest price.
***perNightPricesForNextOneYear/** - will return a csv file containing prices for each property for the next 12 months.

=>Implemented an in-memory cache that caches the data for a particular location/radius, and uses LRU as it's eviction
policy, when dealing with large systems we can use global caches like redis, using cache will lead to data inconsistency
problems (for example based on some news the prices may fall, then we can't show the user the price that's residing in
cache).

**Note:**
In the above end points when we substitute the place as street address like "5 Pace Dr, Edison, NJ USA" or "73 W Monroe
St, Chicago, IL USA", the api that i'm calling is giving some errors saying 400 BAD REQUEST, but for the same if we
provide values without street address like "Edison, NJ USA" or "Chicago, IL USA" or "Inorbit Hyderabad, Telanagana",
able to get the data from the API (still trying to figure out what the issue is).

**Further enhancements:**

* We can improve performance by using multithreading while performing web scraping.
* Logging can be improved further.