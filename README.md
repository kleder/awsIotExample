# Home Security mobile project 
This project was build for Netvision 2016 conference as exaple of AWS IoT Shadow usage in Android application. This application is working with the https://github.com/3mdeb/aws-iot-mqtt-pubsub embedded project

How this project work is described here  http://www.slideshare.net/rafalkorszun/how-to-build-iot-solution-using-cloud-infrastructure-61282284 

## Before you start
### Initial setup
Please modify the GetShadowTask.java and UpdateShadowTask.java files with your Amazon endpoint prefix
and Cognito Pool Id

### Where to get Amazon endpoint prefix?
Endpoint Prefix = random characters at the beginning of the custom AWS

IoT endpoint

describe endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com,

### Where to get Cogito Pool id?
Log into Amazon console, open Cognito service, open "Manage Federated Identities" link and create new Pool id

### How to setup Cognito Pool access rights to AWS IoT?
Go to Amazon IAM and modify the unauthorized cognito role policy by adding these policies:

  "iot:GetThingShadow",
  "iot:UpdateThingShadow"
  
## Compilation

This project is prepared in Android Studio 2.0 please open it from Android Studio and compile

## Credits

This example project is made by http://kleder.co company
