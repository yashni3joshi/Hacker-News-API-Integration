**Hacker News API integration with REST APIs**

The project written in java language using spring boot framework which contains following endpoints:

/top-stories: returns the top 10 stories ranked by score in the last 15 minutes.

/past-stories:returns all the past top stories that were served previously

/comments/ — returns the top 10 parent comments on a given story (sorted by the total number of comments (including child comments) per thread).

App Url for local usage => http://localhost:8080/hacker-news/

Note : Kindly wait for some time after running the /top-stories or /comments api as it filters out a lots of data. Depending upon internet bandwidth it may take 30 seconds or more.

For the second time, /top-stories will display cached data for 15 minutes. Caching is implemented using Redis.

P.S >- This project is built using Java 17 version, spring boot version 3.2.2,and mysql 8.0.29 version.
