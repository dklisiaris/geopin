# Geopin
![Geopin Logo](https://dl.dropboxusercontent.com/u/4888041/geopin-shots/geopin_logo.png)

_Geopin_ is a small school project for the university of patras. An android app in which the user marks her favourite places on a map and stores them in database. Supports directions and reverse geocoding.

## Description

Geopin is an android app in which the user marks her favourite places on a map and stores them in database. Supports directions and reverse geocoding and different locales.

## Features

* Android Maps V2
* User pins her favorite places on map
* Database persistent storage
* Pins have title, description and category
* Different color for each category
* Clustering of multiple locations.
* Calculates nearest route with car, foot or bus.
* Turn by turn navigation.
* Multilingual support(Current languages: English, Greek).
* Async tasks for all database ops.
* Metric and imperial units.
* Choose origin and destination by writing text, current location or click on map.
* Reverse Geocoding on long map click.
* Google's ActionBarCompat, ensure action bar availability for lower version device
* Share pins on facebook. 
* Hide/Show pins.
* Holo custom theme.
* Some custom widgets.
* Sliding up navigation panel.

## Usage

Open this project with android studio. Target sdk is Android 5.0 (API Level: 21) and minimum: Android 2.3 (API Level: 9)

## Screenshots
![Screenshot1](https://dl.dropboxusercontent.com/u/4888041/geopin-shots/1.png)
![Screenshot2](https://dl.dropboxusercontent.com/u/4888041/geopin-shots/2.png)
![Screenshot3](https://dl.dropboxusercontent.com/u/4888041/geopin-shots/3.png)
![Screenshot4](https://dl.dropboxusercontent.com/u/4888041/geopin-shots/4.png)
![Screenshot5](https://dl.dropboxusercontent.com/u/4888041/geopin-shots/5.png)
![Screenshot6](https://dl.dropboxusercontent.com/u/4888041/geopin-shots/6.png)

## Dependencies
* [Play Services](https://developer.android.com/google/play-services/index.html)
* [Support libs with Actionbar Compat](https://developer.android.com/tools/support-library/features.html)
* [Maps Utils](http://googlemaps.github.io/android-maps-utils/): open-source library contains classes that are useful for a wide range of applications using the [Google Maps Android API](http://developer.android.com/google/play-services/maps.html).
* [Android Sliding Up Panel](https://github.com/umano/AndroidSlidingUpPanel): provides a simple way to add a draggable sliding up panel.
* [Crouton](https://github.com/keyboardsurfer/Crouton): Context sensitive notifications for Android.
* [DataFactory](http://mvnrepository.com/artifact/org.fluttercode.datafactory/datafactory): Library to generate data for testing.
* [PreferenceFragment](https://github.com/Machinarius/PreferenceFragment-Compat): Unofficial PreferenceFragment compatibility layer for Android 1.6 and up.
* [FacebookAndroidSDK](https://github.com/facebook/facebook-android-sdk): Integrate Android apps with Facebook Platform.

## Contributing

1. Fork it ( https://github.com/dklisiaris/geopin/fork )
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create a new Pull Request