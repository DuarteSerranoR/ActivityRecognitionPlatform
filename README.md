# Activity Recognition Platform

This project was developed for academic purposes.
Its objective is to allow users to experiment with device interaction through the environment they are in or emit.

## Permissions

  - Microphone usage -> this is requested so the application can do decibel readings, to read how loud its surroundings are and react to it;
  - Sensor Permissions -> not requested on application launch, but these are used too for all the given reactions.

### NOTICE: NO DATA IS STORED OR SENT ANYWHERE, ALL DATA IS COMPUTED AT THE TIME, SHOWN THE RESULTS AND THEN DISCARDED!

## Application's Main Objective

This app reacts to the device's environment, reacting, complaining or requesting changes on how the device should be threatened.
The concept of the app is then a virtual pet that needs certain environmental conditions to be comfortable. 

This pet can: 
  - feel cold, hot, and all extremes and middle grounds with temperature readings;
  - check the weather;
  - get sick when exposed to bad conditions (coldness with bad weather for a certain amount of time);
  - get dizzy when the device is shaken;
  - fall asleep when it is too silent and there is no light.

## Application's Secondary Functionalities

Besides the pet, the application has:
  - a compass for orientation/direction indication;
  - decibel/loudness readings.

## Used Sensors

With this in mind, we can get to the conclusion that the app uses the given sensors:
  - Accelerometer;
  - Magnetometer;
  - Ambient Temperature;
  - Relative Humidity;
  - Light;
  - Microphone.

## Application Architecture

The application is composed of:
  - Home Screen -> where the user can turn on and off the sensor readings;
  - Dashboard -> contains all pet data, how it feels when it feels, and what is happening to it;<br />-> also contains the graphical compass;
  - Configurations -> // TODO - Under development;
  - Sensor Data -> has all processed interesting data recovered from the sensors;
          <br />-> has raw light readings;
          <br />-> has decibel readings.

