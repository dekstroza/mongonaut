# Micronaut & GraalVM example


## Description

Micronaut and Graal example service using mongodb as the datastore.
Compiles to native image. Helm chart provided will deploy the built code together with prometheus, grafana, and jaeger demonstrating telemetry gathering out of the box, together with jaeger tracing and openapi. Grafana is preloaded with demo dashboard as an example.

## Requirements

1. Java with Graal, can be install with sdkman (https://sdkman.io)
2. Maven or Gradle
3. Docker
4. Kubernetes
5. Helm


Easiest way to run this demo is to install sdkman, and using it install java (graal, latest) and micronaut (latest). 

The instructions assume k8s on Docker for Mac/Windows (k8s installed with docker itself). This makes exposing services through LoadBalancer easy, as they are exposed on localhost.
By default ```./mvnw clean install``` will also create docker image with Graal
compiled binary inside, using provided Dockerfile. 

## Building
Code can be built with either maven or gradle, both rely on their respective wrapper.

### Maven Build
Build is performed with spotify's dockerfile maven plugin. To skip Graal build, run `/mvnw -Ddockerfile.skip clean install` which will build only the jar.
To build the code run, this will build the code and create docker image with
Graal binary inside.

```
./mvnw clean install # or just mvn clean install
```

### Gradle Build
To build with gradle run ```./gradlew build``` which will build the code and execute the test cases. To build the Docker image and compile with graal, run ```./gradlew dockerImg``` which will build the Docker image with graal compiled binary.

```
./gradlew build # to build and run the tests
```
or
```
./gradlew dockerImg # to build the code and create graal docker image
```

## Trying out the service

Service can be deployed using provided helm charts in k8s directory with
```
#if using helm v2
helm install k8s/mongonaut

#is using helm v3
helm install --generate-name k8s/mongonaut
```
This will deploy two instances of the service, mongodb, prometheus, grafana and jaeger-all-in-one (for demo purpose). It
will also configure grafana to use prometheus as datasource, and automatically
add application dashboard to the grafana.
Service is annotated for prometheus in helm charts, and will automatically be
scraped by prometheus.

After deploying helm chart, follow instructions printed by helm to obtain
grafana admin password, and access grafana with browser. Follow the instructions in the Notes printed after helm chart is deployed.

Alternatively after deploying service with helm command above, more instances
can be started with "normal" jvm for comparision examples, using the following commands:
```
# Expose mongodb on loadbalancer
SERVICE_NAME=$(kubectl get svc | grep mongodb | awk '{print $1}'); kubectl expose svc $SERVICE_NAME --name $SERVICE_NAME-balanced --type LoadBalancer --port 27017 --target-port 27017
java -jar target/mongonaut-1.0.0-SNAPSHOT.jar
```
This can be usefull to compare startup times, and pre and post jvm warmup performance differences.

### Some common commands:

Testing the service from command line using curl:

```bash
# Save alarm to the database
curl -X POST localhost:7777/mongonaut/alarms -d '{"id": 1,"name": "Second Alarm", "severity": "MEDIUM"}' -H 'Content-Type:application/json'
# Get all alarms
curl -v localhost:7777/mongonaut/alarms
# Health endpoint
curl -v localhost:7777/health
# Prometheus metric endpoint
curl -v localhost:7777/prometheus
```
Timing the service responses using curl and command line:

1. Create file curl-format.txt
2. Test the service with curl command

Content of the curl-format.txt:
```
      time_namelookup:  %{time_namelookup}\n
         time_connect:  %{time_connect}\n
      time_appconnect:  %{time_appconnect}\n
     time_pretransfer:  %{time_pretransfer}\n
        time_redirect:  %{time_redirect}\n
   time_starttransfer:  %{time_starttransfer}\n
                    ----------\n
            time_total:  %{time_total}\n
``` 
Command to test the latency (get all alarms, or similar for save alarm url):
```
# For get all alarms
curl -w "@curl-format.txt" -o /dev/null -s "http://localhost:7777/mongonaut/alarms"
# For save alarm
curl -w "@curl-format.txt" -o /dev/null -s -X POST localhost:7777/mongonaut/alarms -d '{"id": 1,"name": "Second Alarm", "severity": "MEDIUM"}' -H 'Content-Type:application/json'
# Save several alarms
for i in {10..20}; do curl -X POST localhost:7777/mongonaut/alarms -d "{\"id\": $i,\"name\": \"Second Alarm\", \"severity\": \"MEDIUM\"}" -H 'Content-Type:application/json'; done
```
Grafana is available on http://localhost:876/ username is admin and the password can be obtained following instructions printed after deploying helm chart.
```
Demo dashboard is provided with the Grafana, providing some basic telemetry on cpu, mem and api calls.
```
```
Jaeger UI is on: localhost:80 and shows some useless (in this example) spans, provided more as illustration how to do it.
```
```
Openapi definition can be accessed at: http://localhost:7777/swagger/micronaut-service-1.0.0.yml
```

Happy hacking...
