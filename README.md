# gdashboard-cli

Generate Terraform files from Grafana JSON dashboard for [gdashboard](https://registry.terraform.io/providers/iRevive/gdashboard/latest/docs) provider.

### Usage

```
Usage: gdashboard-cli generate [--input <string>] [--dashboard-id <string>] [--group-by-sections] <The path to an output directory>

Parse questions and ensure they are up to the requirements

Options and flags:
    --help
        Display this help text.
    --input <string>
        A path to the file with the JSON of a Grafana dashboard
    --dashboard-id <string>
        An ID of the Grafana dashboard (https://grafana.com/grafana/dashboards)
    --group-by-sections
        Whether to group files by dashboard sections or not
```


### Publish locally

```shell
$ sbt generateNativeBinary
$ gdashboard-cli generate --input ./input.json ./output
$ gdashboard-cli generate --dashboard-id 123 ./output
```

