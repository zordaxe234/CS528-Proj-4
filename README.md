# Location recording Application
## CS528-Proj-4
This application uses an android device's GPS system to gather certain information about the device and displays:

* latitude, longitude, and altitude.
* geolocation information based on the coordinates.
* hPa value recorded by the device's barometer.

The layout is focused on making this information _digestible_ by using mutliple linear layouts that form a numbered list that keeps track of the history of the places you have been, and the average hPa reading in that area.  

**Please keep in mind** that this application is catered to be tested while on foot, therefore the range that one has to move before he is considered in a "new" location is relatively small. To change this, simply change `LOCATION_CHANGE_RANGE` in UserLocation.java (type double in meters)
