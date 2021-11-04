# GraVT
Grandma Velocity Tracker (aka GraVT) is a fall-detection Android app built using Java. GraVT detects falls and send alerts to emergency contacts. Download this app on your and your love ones' phones.

Development is currently in progress.

## Demo
<kbd>![screenshots](https://github.com/wxo15/GraVT/blob/main/screenshots/demo.gif)</kbd>


## Algorithm
The app detects 3 major stages in sequence:
1. **Fall** - When close-to-free-fall acceleration is detected.
2. **Impact** - Sudden spike in acceleration above normal.
3. **Inactivity** - Small changes in acceleration for a specified period of time.

Each stage can only be triggered if the last stage has been triggered. There is a small cooldown period implemented at the end of each stage.


## Screenshots

[<img src="/screenshots/dashboard.jpg" align="left"
width="200"
    hspace="10" vspace="10">](/screenshots/dashboard.jpg)
[<img src="/screenshots/settings.jpg" align="center"
width="200"
    hspace="10" vspace="10">](/screenshots/settings.jpg)


