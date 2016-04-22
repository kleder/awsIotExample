# Home Security mobile project 
This project was build for Netvision 2016 conference as exaple of AWS IoT Shadow usage in Adroid application. This application working with the https://github.com/3mdeb/aws-iot-mqtt-pubsub embedded project

## Initial setup
Please modify the GetShadowTask.java and UpdateShadowTask.java files with your Amazon endpoint prefix
and Cognito Pool Id

## Where to get Amazon endpoint prefix?
Endpoint Prefix = random characters at the beginning of the custom AWS

IoT endpoint

describe endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com,

## Where to get Cogito Pool id?
Log into Amazon console, open Cognito service, open "Manage Federated Identities" link and create new Pool id

## How to setup Cognito Pool access rights to AWS IoT?
Go to Amazon IAM and modify the unauthorized cognito role policy by adding these policies:

  "iot:GetThingShadow",
  "iot:UpdateThingShadow"

