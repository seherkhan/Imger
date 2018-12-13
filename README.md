# Project Description
**Imger** is an Android app to manage images using their metadata.

# Functionality
* View the exif metadata of jpeg images on their phone
* Add tags to an image and download the image with the tags embedded in the metadata
* Sort images on file size and date
* Search images based on exif metadata and user-defined tags

# Getting Started
Prerequisites:
* Android Studio
* Android emulator or phone (API level 28) with access to internet


On your emulator or phone (API level 28), create a folder "ImgerDummy" in the DCIM folder, and add a few images to it.
Import the project to Android Studio and run it.
*Note: permission to access phone storage will be required.*

# Structure of the Application
1. Permission to access phone storage requested
1. Gallery view of all images
   1. Filter button allows sorting/searching images
   1. Clicking on image allows viewing metadata and adding tags

Gallery:
![Gallery View](/home/seherkhan/myfiles/git/finalproj/screenshots/gallery.jpg)

Viewing image metadata and adding tags to the image:
![Image Metadata](/home/seherkhan/myfiles/git/finalproj/screenshots/metadata.jpg)

Faceted search options:
![Filter Options](/home/seherkhan/myfiles/git/finalproj/screenshots/facetsearch.jpg)

# Libraries used
* Apache Commons Imaging
* Glide




