# Learn Microservices with Spring Boot -（Spring Cloud Alibaba with RocketMQ）
This repository contains the source code of the practical use case described in the book [Learn Microservices with Spring Boot 3 (3rd Edition)](https://link.springer.com/book/10.1007/978-1-4842-9757-5).
The book follows a pragmatic approach to building a Microservice Architecture. You start with a small monolith and examine the pros and cons that come with a move to microservices.

## Chapter 8 version 4/4 (Book's final version)

The Chapter 8's source code is divided into four parts for a better understanding of how the system evolves when we start introducing _Common Patterns of Microservice Architectures_.

In this last part, we introduce Centralized Logs, Distributed Tracing, and Containerization with Docker and Docker Compose.

The figure below shows a high-level overview of the final version of our system.

![Logical View - Chapter 8 (Final)](resources/microservice_patterns-Config-Server-1.png)

The main concepts included in this last part of the chapter are:

* Why do we need Centralized Logs and Distributed tracing?
* Why would I create Docker images for my applications?
* Building a simple logger application with Spring Boot and RocketMQ.
* Distributed traces with OpenTelemetry.
* Sentinel flow control and circuit breaker control for Spring Cloud Gateway.
* Building Docker images for Spring Boot applications with Cloud Native Buildpacks.
* Container Platforms, Application Platforms, and Cloud Services.

As usual, the book follows a hands-on approach, so you learn everything based on this microservice case study.

## Running the app

This time, you can use Docker to start the complete system. If you're interested in the instructions to run the components manually, [check the previous version of the repository](https://github.com/Book-Microservices-v2/chapter08c).

If you don't want to build the images yourself, these images are already in the Docker Hub. You can run the complete system using the `docker-compose-public.yml` file:

```bash
docker$ docker-compose -f docker-compose-public.yml up
```

In case you want to learn how to build the images, keep reading the instructions below.

### Building the images yourself

First, build the application images with:

```bash
multiplication$ docker build -t  multiplication:0.0.1-SNAPSHOT .
gamification$ docker build -t  gamification:0.0.1-SNAPSHOT .
gateway$ docker build -t  gateway:0.0.1-SNAPSHOT .
logs$ docker build -t  logs:0.0.1-SNAPSHOT .
```

And the UI server (first you have to build it with `npm run build`):

```bash
challenges-frontend$ npm install
challenges-frontend$ npm run build
challenges-frontend$ docker build -t challenges-frontend:1.0 .
```

change brokerIP1 to your docker hosts IP in docker/config/rocketmq/broker.conf

brokerIP1=<192.168.71.47>

Once you have all the images ready, run:

```bash
docker$ docker-compose up
```

Go into the docker container and execute the following commands 

```bash
docker exec -it <nameserver> bash
bin/mqadmin updateTopic -n localhost:9876 -c DefaultCluster -t attempts-topic -a +message.type=FIFO
bin/mqadmin updateTopic -n localhost:9876 -c DefaultCluster -t logs           -a +message.type=FIFO
```

change kibana_system password to changeme
```bash
docker exec -it <elasticsearch> bash
/usr/share/elasticsearch/bin/elasticsearch-reset-password -i -u kibana_system
```

and It now uses opentelemetry collector contrib to collect:
- metrics to prometheus
- traces to tempo/zipkin/jaeger/elasticsearch
- logs to loki/elasticsearch
and grafana can be used to view loki logs, elasticsearch logs/traces, tempo traces, and prometheus metrics

```
Front End: http://localhost:3000
Grafana: http://localhost:3001/grafana
Prometheus: http://localhost:9090
Tempo: http://localhost:3200
Nacos: http://localhost:8082  (nacos/nacos)
Sentinel Dashboard: http://localhost:8858 (sentinel/sentinel)
Kibana: http://localhost:5601
```

**Important: Nacos Initialization**

After starting Nacos for the first time, you must complete the initialization process:
1. Access http://localhost:8082/ in your browser
2. Log in with credentials: `nacos/nacos`
3. Complete the initialization wizard that appears
4. This step is required to promote the nacos user from regular user to admin (globalAdmin: true)

See the figure below for a diagram showing the container view.

![Container View](resources/microservice_patterns-View-Containers.png)

Once the backend and the frontend are started, you can navigate to `http://localhost:3000` in your browser and start resolving multiplication challenges.

## Playing with Docker Compose

After the system is up and running, you can quickly scale up and down instances of both Multiplication and Gamification services. For example, you can run:

```bash
docker$ docker-compose up --scale multiplication=2 --scale gamification=2
```

And you'll get two instances of each of these services with proper Load Balancing and Service Discovery.

## Questions

* Do you have questions about how to make this application work?
* Did you get the book and have questions about any concept explained within this chapter?
* Have you found issues using updated dependencies?

Don't hesitate to create an issue in this repository and post your question/problem there. 

## About the book

Are you interested in building a microservice architecture from scratch? You'll face all the challenges of designing and implementing a distributed system one by one, and will be able to evaluate if it's the best choice for your project.

## Purchase
You can buy the book online from these stores:
* [Apress](https://link.springer.com/book/10.1007/978-1-4842-9757-5)
* [Amazon](https://www.amazon.com/Learn-Microservices-Spring-Boot-Containerization/dp/1484297563)
and other online stores.
