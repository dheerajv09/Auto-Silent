# Auto Silent

## Overview

- This is an Android based application, aimed at offering comfort to those who wish to keep their mobile phones on silent mode in specific geographical locations. It has an appealing user interface with user-friendly features allowing the users to set their desired locations in their mobiles to turn silent.

- It’s simple to use application, useful in today’s scenario when people are extremely busy and want most of things automated to help in their functioning. This app will work as an aid to their day to day functioning! We often need to switch our mobile in silent mode for personal, social or professional reasons. We may forget to keep our mobile in silent mode that is where this app will come to the aid.

- You simply need to provide the geographical locations where you would like to keep your mobile into silent mode. Rest will be taken care of by this app. It will not forget to go in silent mode the moment it  will enter the set radius of geo location.

- Auto Silent app will automatically silent user's mobile device on selected Locations. The user can select a location with the use of Google Places API(https://developers.google.com/places/) and the app will store location's placeID to the database using content providers.

## Introduction

A geofence is a virtual perimeter defined by GPS or RFID around a real world area. Geofences can be created with a radius around a point location.

## GoogleMap and Places API

The app uses Google Maps and Places API for the geofence-feature. If you want to use this feature, you need to get your own API-Key from Google Cloud Platforms(<https://console.developers.google.com/apis/>). Make sure to register your API key for Maps SDK for Android and Places API.

Add the key to the resource-file google_api.xml by replacing YOUR_API_KEY with the value of your API key.

```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="google_api_key" translatable="false" templateMergeStrategy="preserve">
        YOUR_API_KEY
    </string>
</resources>
```

## Features

- Create rules to automatically switch to vibrate mode or silent mode
- Schedule Rules will silence your phone at a specific time in your weekly schedule
- Calendar Rules will silence your phone during calendar events
- Match all events in your calendar or match specific events based on keywords in the event title or description

## Technologies

- Android SDK v28 (min v21)
- Kotlin 1.3.21
- Dagger 2.20
- Lifecycle 2.2.0
- Room 2.0.0
- Coroutines 1.1.1
  
//https://github.com/sorianog/TreasureHuntApp
https://github.com/StephGit/EazyTime/blob/master/README.md
https://github.com/DenisFerreira/GeoFences