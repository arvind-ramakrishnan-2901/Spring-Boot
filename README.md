# Spring Boot Rest Controller Example Project
REST APIs implemented using Spring Boot Rest Controller

### How to initialise the project

* Download the project from here. import as "Existing Maven Projects" in your IDE
* Once successfully built, run the below file as Java application 

## Pre-requisites and How to Run

* 1. Substitute the aws access key and secret key value in the properties to your values. The IAM user created
     should have write/read access to both S3 and DynamoDB

* 2. In the AWS Console, navigate to S3 and create a folder "dogbreed.pictures" in the root AmazonS3 I
```
com.dogbreed.rest.controller.SpringBootMain

All good. You can start calling the below rest api's.
```

## REST API Endpoints

### Get Dog Breed Details from public url

```
Base URL: http://localhost:8080
```

```
GET /dogbreed
Content-Type: application/json

Sample Response: 

{
    "id": "n02111129_187",
    "name": "leonberg",
    "imageURL": "https://dogbreed.pictures.s3.ap-southeast-2.amazonaws.com/leonberg",
    "lastmodified": "Wed Jan 22 23:58:08 AEDT 2020"
}

```

### Get Dog Breed By ID
```
GET /dogbreed/{id}
Content-Type: application/json

Sample Response:
{
    "id": "n02111129_187",
    "name": "leonberg",
    "imageURL": "https://dogbreed.pictures.s3.ap-southeast-2.amazonaws.com/leonberg",
    "lastmodified": "Wed Jan 22 23:58:08 AEDT 2020"
}

```

### Get Dog Breed By Name
```
Get /dogbreed/search?name=

Content-Type: application/json

Sample Response:

[
    {
        "id": "n02111129_187",
        "name": "leonberg",
        "imageURL": "https://dogbreed.pictures.s3.ap-southeast-2.amazonaws.com/leonberg",
        "lastmodified": "Wed Jan 22 23:58:08 AEDT 2020"
    }
]
```

### Delete a dog Breed Record
```
Delete /dogbreed/{id}

Content-Type: application/json

Sample Response:
{
    "statusCode": 200,
    "statusMessage": "n02111129_187 deleted successfully from S3 and  DynamoDB"
}
```

### Find All Dog Breeds From Database
```
GET /dogbreeds
Content-Type: application/json

Sample Response:
[
    "spaniel-sussex",
    "springer-english",
    "wolfhound-irish",
    "poodle-standard",
    "greyhound-italian",
    "borzoi"
]
```

### Tech Stack used
```
1. Spring Boot/ Rest Controllers
2. Embedded Tomcat Instance (Apache Tomcat/8.5.34)
3. JUnit
4. Postman and Blazemeter for testing the API's and performance
5. Java AWS SDK API's for DynamoDB and S3
```
### Scope for Improvements
```
1. Code Modularity 
	a. Align to PMD comments on variable/method name declarations.

2. AWS Credentials
	a. Rather than storing the aws access key and aws secret key in the properties file, we can create a normal user in IAM (with no access policy to DynamoDB and S3), make the user assume
	   the role of an user who has full access to S3 and DynamoDB. We get a signInToken from AWS this way which it is short lived, thereby providing an abstraction on the actual access and 
	   secret keys. 
```

