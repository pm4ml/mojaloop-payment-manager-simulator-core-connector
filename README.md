# mojaloop-simulator-core-connector

## Run with environment variable

Application **must** receive the BACKEND_ENDPOINT environment variable to connect to mojaloop-simulator.
```shell script
$ java -Dbackend.endpoint=http://simulator:3000 -Doutbound.endpoint=http://simulator:3003 -jar ./client-adapter/target/client-adapter.jar
```
```shell script
$ docker run --rm -e BACKEND_ENDPOINT=http://simulator:3000 -e MLCONN_OUTBOUND_ENDPOINT=http://simulator:3003 -p 3002:3002 mojaloop-simulator-core-connector:latest
```

### Run Mojaloop Simulator
To enable backend connection test, run `mojaloop-simulator-backend` before run connector.
```shell script
$ docker run --rm -p 3000:3000 mojaloop-simulator-backend:latest
```

### Build Docker Image
To build a new Docker image based on Dockerfile.
```shell script
$ docker build -t mojaloop-simulator-core-connector:latest .
```

## Prometheus client local development
To enable testing Prometheus client implementation in local environment, follow the below steps.

### Prometheus Docker

1. Create a folder for Prometheus Docker volume
```shell script
$ mkdir -p /docker/prometheus
```

2. Under created folder, create a `prometheus.yml` configuration file with below content.
```shell script
global:
  scrape_interval:  10s # By default, scrape targets every 15 seconds.
  evaluation_interval: 10s # By default, evaluate rules every 15 seconds.

# A scrape configuration containing exactly one endpoint to scrape:
# Here it's Prometheus itself.
scrape_configs:
  # The job name is added as a label `job=<job_name>` to any timeseries scraped from this config.
  - job_name: 'prometheus'
  
  # metrics_path defaults to '/metrics'
  #    scheme defaults to 'http'.
    static_configs:
    - targets: ['localhost:7001'] # Might be the local IP  
```

3. Start Docker container binding configuration file.
```shell script
docker run -d -p9090:9090 --name prometheus -v /path/to/file/prometheus.yml:/etc/prometheus/prometheus.yml prom/prometheus
```
**NOTE:** Running above command once it is possible do `docker stop prometheus` to disable service and `docker start prometheus` to enable it again.
Rather than that add `--rm` flag to destroy Docker container whenever it is stopped.

4. Once Docker is running, access Prometheus by browser in [localhost:9090](localhost:9090).
Check configurations is like expected in [/config](localhost:9090/config) and [/targets](localhost:9090/targets).

5. In *Graph* page, Expression field, add the metric key expected to monitor according set for application client and press Execute.

**NOTE:** The list of metric keys can be found `metrics_path` set under `scrape_configs` of `prometheus.yml` file (Ex: [localhost:7001/metrics](localhost:7001/metrics)).
   
### Prometheus Java Client

To enable Prometheus metrics it is following steps and samples from [prometheus/client_java](https://github.com/prometheus/client_java).
The types of metrics enabled are _Counter_ and _Histogram_ and the default port set for exporter is 7001,
but it can be changed in `application.yml` property `server.metrics.port`.