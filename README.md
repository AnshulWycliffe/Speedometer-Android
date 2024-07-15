# Speedometer App

## Description
The Speedometer App is an Android application that provides real-time speed, location, and direction information. It utilizes GPS and device sensors to offer a comprehensive speedometer experience.

## Features
- Real-time speed measurement in km/h
- Maximum speed tracking
- Current latitude and longitude display
- Compass functionality showing cardinal directions
- Persistent notification with current and max speed
- Landscape and portrait mode support

## Technical Details
- Developed for Android
- Uses GPS for speed and location data
- Utilizes accelerometer and magnetometer for direction
- Implements Android's LocationListener and SensorEventListener

## Permissions Required
- ACCESS_FINE_LOCATION: For GPS functionality
- FOREGROUND_SERVICE: For persistent notification

## How It Works
1. The app requests location permission on startup.
2. Once granted, it begins tracking the device's location and speed.
3. The speed is calculated from GPS data and displayed on the screen.
4. The app tracks and displays the maximum speed achieved.
5. Accelerometer and magnetometer data are used to determine the device's direction.
6. A persistent notification shows current and maximum speed.

## UI Components
- SpeedView: Displays current speed
- TextViews: Show latitude, longitude, maximum speed, and current direction

## Background Operation
The app continues to track speed and update the notification even when not in the foreground.

## Installation
1. Clone the repository
2. Open the project in Android Studio
3. Build and run on an Android device or emulator

## Dependencies
- AndroidX libraries
- Custom SpeedView library (not included in the provided code)

## Note
This app is designed for informational purposes only and should not be used as a definitive speed measurement tool for vehicles.

## Contributing
Contributions, issues, and feature requests are welcome. Feel free to check [issues page] if you want to contribute.
