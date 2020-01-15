# SensorTemplate

The SensorTemplate provides a simple build of the Android application with centrelized tools to manipulate with sensors of a phone. Whole computational unit is build on a foreground service, which can be turn on by the button in the main activity.
<p align="center">
<img src="https://creativemotion.app/assets/images/screenshot-2019-09-08-16-39-06-pixel-quite-black-portrait-1048x1050.png"  width="500">
</p>

[![API](https://img.shields.io/badge/API-19%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=19)
[![license](https://img.shields.io/badge/license-Apache%202-blue)](https://www.apache.org/licenses/LICENSE-2.0) 

## Features
The model under foreground service connects different aspects of Android system together. 
* whole life cycle of sensors is handled
* temporal storage for samples
* creation of csv files, if needed
* implementation of GPS
* implementation of Activity Recognition Client
* methods to handle changes in network, user activity and battery life
* wakelock handling
* [app is added to whitelist for background processing by AutoStarter](https://github.com/judemanutd/AutoStarter)
* examples of detector classes

Every aspect is completely modifiable.

## Detectors

Meanwhile the app contains example of <b>Tensorflow Lite</b> neural network for 1D signal/features. Model can be also modified to 2D if needed.
More suitable examples of models will be implemented later. 

## Third parties

Thanks goes to:

* [AutoStarter](https://github.com/judemanutd/AutoStarter) - adds your app to whitelist of battery optimizer implemented by manufacturer

  [![Download](https://api.bintray.com/packages/jude-manutd/maven/autostarter/images/download.svg) ](https://bintray.com/jude-manutd/maven/autostarter/_latestVersion)
[![license](https://img.shields.io/github/license/mashape/apistatus.svg?style=flat-square)](https://opensource.org/licenses/MIT) 
[![API](https://img.shields.io/badge/API-14%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=14)
* [Tensorflow](https://github.com/tensorflow/tensorflow) - an open source machine learning framework

  [![Documentation](https://img.shields.io/badge/api-reference-blue.svg)](https://www.tensorflow.org/api_docs/) 
[![license](https://img.shields.io/badge/license-Apache%202-blue)](https://www.apache.org/licenses/LICENSE-2.0) 

