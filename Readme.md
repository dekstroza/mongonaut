# Micronaut & GraalVM example


## Description

Micronaut and Graal example service using mongodb as the datastore.
Compiles to native image.

## Requirements

1. Java with Graal, can be install with sdkman (https://sdkman.io)
2. Maven
3. Docker
4. Kubernetes
5. Helm
6. Gradle (the build is broken at the moment)

Easiest way to run this demo is to install sdkman, and using it install java (graal, latest) and micronaut (latest). 

Following instructinos assume using k8s on Docker for Mac/Windows (k8s installed with docker itself). This makes exposing services through LoadBalancer easy, as they are exposed on localhost.
By default ./mvnw clean install will also create docker image with Graal
compiled binary inside, using provided Dockerfile. 

## Building

Build is performed with spotify's dockerfile maven plugin. To skip Graal build, run `/mvnw -Ddockerfile.skip clean install` which will build only the jar.
To build the code run, this will build the code and create docker image with
Graal binary inside.

```
./mvnw clean install # or just mvn clean install
```

## Trying out the service

Service can be deployed using provided helm charts in k8s directory with
```
helm install k8s/mongonaut
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
Grafana is available on http://localhost:876/ username is admin and password can be obtained using:
```
kubectl get secret --namespace default precise-ragdoll-grafana -o jsonpath="{.data.admin-password}" | base64 --decode ; echo
```
Jaeger UI is on: localhost:80

Happy hacking...
