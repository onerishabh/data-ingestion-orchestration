# AWS Data Ingestion Orchestration

[![.github/workflows/aws-deploy.yml](https://github.com/onerishabh/data-ingestion-orchestration/actions/workflows/aws-deploy.yml/badge.svg)](https://github.com/onerishabh/data-ingestion-orchestration/actions/workflows/aws-deploy.yml) [![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

This is a simple application that exposes `AWS Lambda Function` as a public URL. The URL is used to ingest data to an `AWS DynamoDB Table` and list items in the same table. Data is recieved via query string parameters from the URL and validated using an `AWS Statemachine`. If data is in correct format then it is ingested otherwise reponse code `400` is shared as a `PUT` response via the `AWS Lambda Function`. Data processing is done using `Python` based `AWS Lambda Function`.

The application traffic is monitored using `AWS Cloudwatch Dashboard` which tracks the `AWS Lambda Function` invocations, durations, error/success rates and `AWS Account` billings as four independent widgets.

The application is put through load-testing by simulating traffic using `AWS ECS Farget` which runs **2** instances of `AWS ECS Tasks` *(this adds costs exponentially, so check with your organization or sandbox provider)* running custom `Docker` image. Each task hits the URL endpoint once every second using a simple shell script. The traffic can be monitored safely through `AWS Cloudwatch Dashboard`. 

![ezgif com-gif-maker](https://raw.githubusercontent.com/onerishabh/data-ingestion-orchestration/main/.github/images/app_demo.gif)