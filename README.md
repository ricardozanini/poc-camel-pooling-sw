# Camel Pooling to SonataFlow Workflow via CloudEvents

In this example we show a brief demonstration of Camel pooling from a given HTTP endpoint and producing CloudEvents to a SonataFlow workflow.

## How to run

In one terminal, enter in the [`target-workflow`](target-workflow) and run:

```shell
cd target-workflow
quarkus dev
```

The workflow will start and wait for CloudEvents over HTTP at http://localhost:8080/

In a second terminal, enter in the [`camel-pooling`](camel-pooling) and run:

```shell
cd camel-pooling
quarkus dev
```

Camel will run under http://localhost:9090/ and an interval of 30 seconds will pool the endpoint https://my-json-server.typicode.com, transform the response in a CloudEvent message and send it to the workflow.

To check if the example is running you can follow the logs or access the workflow dev-ui page:

http://localhost:8080/q/dev-ui/org.apache.kie.sonataflow.sonataflow-quarkus-devui/workflows

Click on "Monitoring" tab to see the workflow instances.
