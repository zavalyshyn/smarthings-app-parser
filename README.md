# Samsung Smarthings Apps Parser

A parser for SmartThings SmartApps that extracts the information about the apps' access to device capabilities and calls to SmartThings API that can potentially be used for sensitive data leaking. 

This code is based on [IoTSan project](https://github.com/dangtunguyen/IoTSan) and all the kudos should go to their respective authors. I only modified their code slightly for my needs. 

## What this code does

- compiles the app's Groovy code and visits all the nodes in the AST
- extracts sensitive device capabilities from each node in AST and adds those to the .csv file
- detects sensitive API calls and also adds those into a .csv file
- outputs statistics on the number of sensitive API calls and device capabilities found in the analyzed apps

Note: the list of sensitive device capabilities and API calls is static and can be modified as needed. 

