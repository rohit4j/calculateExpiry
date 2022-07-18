# Problem statement

write a function to calculate the expiry datetime. The expiry datetime is 3 WORKING hours from the "now" input parameter.</br>
the working hours is defined in the "schedule" input parameter.</br>


@param {Date} now - current datetime. e.g: '2019-10-11T08:13:07+0800'</br>
@param {Array} schedule - an array of objects. which specified the day open or close and also the start and end of working hours</br>
[
	{"open": false, "open_at": "", close_at: ""}, // sunday</br>
	{"open": true, "open_at": "09:00", close_at: "18:00"}, // monday</br>
	{"open": true, "open_at": "09:00", close_at: "18:00"},</br>
	{"open": true, "open_at": "09:00", close_at: "18:00"},</br>
	{"open": true, "open_at": "09:00", close_at: "18:00"},</br>
	{"open": true, "open_at": "09:00", close_at: "17:00"},</br>
	{"open": false, "open_at": "", close_at: ""},</br>
]
@returns {Date} - datetime, 3 working hour from input date ("now"), which is 11 am of next monday</br>
</br>
</br>
Example:</br>
If "now" is friday 4pm. and "schedule" as the above sample, the expiry date should be next monday 11 am. because on friday office close</br>
at 5pm and office is closed on weekend.</br>
*/

# Prerequisite to run this program

Java 11 installed on local machine </br>
Maven installed on local machine

# Command to build jar (run this in project root)
mvn clean package

# Command to run jar
cd .\target\ </br>
 java -jar CalculateExpiry-jar-with-dependencies.jar < path to schedule json file > < now datetime string > </br>
 eg.  java -jar CalculateExpiry-jar-with-dependencies.jar "C:\Users\h104433\Downloads\schedule.json" "2019-10-14T19:00:00+0800"



