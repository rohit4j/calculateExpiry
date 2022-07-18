# prerequisite to run this program

Java 11 installed on local machine </br>
Maven installed on local machine

# command to build jar (run this in project root)
mvn clean package

# command to run jar
cd .\target\ </br>
 java -jar CalculateExpiry-jar-with-dependencies.jar < path to schedule json file > < now datetime string > </br>
 eg.  java -jar CalculateExpiry-jar-with-dependencies.jar "C:\Users\h104433\Downloads\schedule.json" "2019-10-14T19:00:00+0800"



