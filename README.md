# gcp-exporter

## Motivation
In an effort to centralize our metrics I needed a simple exporter for GCP metrics that Prometheus could interpret.
I created a simple exporter that’s plug and play.


## Quick start

Set the environment variable `GCP_PROJECT_ID` in your project id. Start the main application.

Navigate to http://localhost:8080/prometheus

You **should** see metrics as long as your have compute, redis, or cloudsql resources.

## Adding Monitored Resources

To get started all you need to do is to add your metrics to [gcp-metrics.yml](https://github.com/bpoole6/gcp-exporter/blob/main/src/main/resources/gcp-metrics.yml). [For a complete list of GCP](https://cloud.google.com/monitoring/api/metrics_gcp).

As of writing this, the resource types that are being exported are compute/redis/cloudsql.

If you wanted to add a new type as such as [pub/sub](https://cloud.google.com/monitoring/api/metrics_gcp#gcp-pubsub) then you’d need to add under metrics section of [gcp-metrics.yml](https://github.com/bpoole6/gcp-exporter/blob/main/src/main/resources/gcp-metrics.yml)

```yaml
metrics:
  ...
  - resourceDomain: pubsub
    metrics:
      - snapshot/backlog_bytes
      - snapshot/num_messages
      - subscription/byte_cost
   ...
```

resourceDomain: 
- is used in formatting the resource endpoint. <resourceDomain>.googleapis.com/<metric>.
  - I.E [pubsub.googleapis.com/](https://cloud.google.com/monitoring/api/metrics_gcp#gcp-pubsub)

metrics:
- This is a list of metrics you want to monitor.