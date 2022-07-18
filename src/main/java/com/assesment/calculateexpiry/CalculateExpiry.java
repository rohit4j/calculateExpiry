package com.assesment.calculateexpiry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;


/**
 * <h3>Calculate Expiry program</h3>
 *
 * @author hitro.jain@gmail.com
 * @description program to calculate the expiry datetime
 * @date 2022-07-18
 **/
public class CalculateExpiry {

    //java class main method
    public static void main(String[] args){

        if (args.length < 2) {
            System.err.println("Invalid or no arguments were provided. Please pass 'schedule json file path' as 1st argument and 'now datetime string'(format : yyyy-MM-dd'T'HH:mm:ssZ) as 2nd argument.");
            System.err.println("Example of java full command :  java -jar .\\CalculateExpiry-jar-with-dependencies.jar \"C:\\Users\\h104433\\Downloads\\schedule.json\" \"2019-10-11T16:00:00+0800\"");

        } else {

            Path fileName
                    = Path.of(args[0]);

            //input - schedule json string
            String scheduleJson = null;
            try {
                scheduleJson = Files.readString(fileName);
            } catch (Exception e) {
                System.err.println("Exception occurred while reading from file path. Invalid path or file does not exist.");
                return;
            }

            //input - now
            String now = args[1];

            CalculateExpiry calculateExpiry = new CalculateExpiry();

            //call to find method which returns expiry datetime
            calculateExpiry.find(now, scheduleJson);
        }

    }

    /**
     * method to calculate the expiry datetime
     *
     * @param inputNow          - current datetime. e.g: '2019-10-11T08:13:07+0800'
     * @param inputScheduleJson - json string which specifies the day open or close and also the start and end of working hours
     * @return datetime - which specifies expiry datetime which is 3 WORKING hours from the "inputNow" input parameter
     */
    public OffsetDateTime find(String inputNow, String inputScheduleJson) {

        //convert json string to json array using java gson library
        JsonArray inputScheduleArray = new Gson().fromJson(inputScheduleJson, JsonArray.class);

        //map where key = 'day of the week' i.e 'Friday' and value = schedule for that specific day, i.e {"open": true, "open_at": "09:00", close_at: "17:00"}
        Map<String, JsonObject> dayOfWeekScheduleMap = enrichScheduleData(inputScheduleArray);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        OffsetDateTime now = OffsetDateTime.parse(inputNow, formatter);

        //output of program - expiry datetime
        OffsetDateTime expiry = null;

        // 3 hours = 10800 secs
        long secondsToIncrement = 10800;

        //check if now day is open or not, if not then increment the day till an open day found
        if (!isOpen(dayOfWeekScheduleMap, now.getDayOfWeek().name())) {
            now = incrementDateAndTime(now, dayOfWeekScheduleMap);
        }

        //if now is open, check if now is before open_at, if it is then set now's time = open_at time.
        if (now.isBefore(OffsetDateTime.of(now.toLocalDate(), LocalTime.parse(getOpenAt(dayOfWeekScheduleMap, now.getDayOfWeek().name())), now.getOffset())))
            now = OffsetDateTime.of(now.toLocalDate(), LocalTime.parse(getOpenAt(dayOfWeekScheduleMap, now.getDayOfWeek().name())), now.getOffset());

        //if now is open, check if now is after close_at, then increment the day till an open day is found as per schedule. set open day's time = open_at time
        if (now.isAfter(OffsetDateTime.of(now.toLocalDate(), LocalTime.parse(getCloseAt(dayOfWeekScheduleMap, now.getDayOfWeek().name())), now.getOffset())))
            now = incrementDateAndTime(now, dayOfWeekScheduleMap);

        //increment the datetime by 3 hours. If it breaches close_at, then increment the datetime and carry forward the extra seconds i.e [(now + 3 hours) - close_at datetime]
        if (now.plusHours(3).isAfter(OffsetDateTime.of(now.toLocalDate(), LocalTime.parse(getCloseAt(dayOfWeekScheduleMap, now.getDayOfWeek().name())), now.getOffset()))) {
            secondsToIncrement = secondsToIncrement - Duration.between(now, OffsetDateTime.of(now.toLocalDate(), LocalTime.parse(getCloseAt(dayOfWeekScheduleMap, now.getDayOfWeek().name())), now.getOffset())).toSeconds();
            now = incrementDateAndTime(now, dayOfWeekScheduleMap);
        }

        //add the extra seconds on the incremented now
        expiry = now.plusSeconds(secondsToIncrement);

        System.out.println("input datetime = " + inputNow);
        System.out.println("input day of week = " + OffsetDateTime.parse(inputNow, formatter).getDayOfWeek().name());
        System.out.println("expiry datetime = " + expiry);
        System.out.println("expiry day of week = " + expiry.getDayOfWeek().name());

        //return expiry datetime
        return expiry;
    }

    /**
     * return the close_at for the particular day of the week
     *
     * @param dayOfWeekScheduleMap
     * @param dayOfWeek
     * @return
     */
    private String getCloseAt(Map<String, JsonObject> dayOfWeekScheduleMap, String dayOfWeek) {
        return dayOfWeekScheduleMap.get(dayOfWeek).get("close_at").getAsString();
    }

    /**
     * return the open_at for the particular day of the week
     *
     * @param dayOfWeekScheduleMap
     * @param dayOfWeek
     * @return
     */
    private String getOpenAt(Map<String, JsonObject> dayOfWeekScheduleMap, String dayOfWeek) {
        return dayOfWeekScheduleMap.get(dayOfWeek).get("open_at").getAsString();
    }

    /**
     * return whether the particular day of the week is open or not
     *
     * @param dayOfWeekScheduleMap
     * @param dayOfWeek
     * @return
     */
    private boolean isOpen(Map<String, JsonObject> dayOfWeekScheduleMap, String dayOfWeek) {
        return dayOfWeekScheduleMap.get(dayOfWeek).get("open").getAsBoolean();
    }

    /**
     * this method increments the current datetime till its gets the next open day as per schedule.
     * It makes use of 'day of the week' map that we created when we enriched the input schedule data.
     *
     * @param now
     * @param dayOfWeekScheduleMap
     * @return
     */
    private OffsetDateTime incrementDateAndTime(OffsetDateTime now, Map<String, JsonObject> dayOfWeekScheduleMap) {
        //increment the day by 1
        now = now.plusDays(1);

        if (isOpen(dayOfWeekScheduleMap, now.getDayOfWeek().name()))
            //set the time on incremented now to open_at time for that day
            now = OffsetDateTime.of(now.toLocalDate(), LocalTime.parse(getOpenAt(dayOfWeekScheduleMap, now.getDayOfWeek().name())), now.getOffset());
        else {
            //loop till an open day is found
            while (!isOpen(dayOfWeekScheduleMap, now.getDayOfWeek().name())) {
                now = now.plusDays(1);
                if (isOpen(dayOfWeekScheduleMap, now.getDayOfWeek().name()))
                    now = OffsetDateTime.of(now.toLocalDate(), LocalTime.parse(getOpenAt(dayOfWeekScheduleMap, now.getDayOfWeek().name())), now.getOffset());
            }
        }

        return now;
    }

    /**
     * This method takes schedule array as input and enriches it with 'day of the week' based on array index.
     * A map is created where key = day of the week and value = schedule for that specific day
     *
     * @param inputScheduleArray - schedule json array
     * @return map
     */
    private Map<String, JsonObject> enrichScheduleData(JsonArray inputScheduleArray) {
        Map dayOfWeekScheduleMap = new HashMap();
        for (int i = 1; i < inputScheduleArray.size(); i++) {
            dayOfWeekScheduleMap.put(DayOfWeek.of(i).name(), inputScheduleArray.get(i).getAsJsonObject());
        }
        dayOfWeekScheduleMap.put(DayOfWeek.of(7).name(), inputScheduleArray.get(0).getAsJsonObject());
        return dayOfWeekScheduleMap;
    }
}