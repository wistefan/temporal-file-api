# Temporal File API

Facade to the NGSI-LD File-API, providing the responses as a file download.

>:warning: The current implementation uses the local filesystem for storage and does not optimize for memory. It does not scale horizontally(unless a shared filesystem is used)
> and can ran out of memory in huge-data use-cases.  

## Deployment

The service is developed as a facade to the actual NGSI-LD API, thus requires a real implementation downstream. The [docker-compose](./src/test/resources/docker-compose/docker-compose.yml) provides an environment
with [Orion-LD](https://github.com/FIWARE/context.Orion-LD) and [Mintaka](https://github.com/FIWARE/mintaka), but it only uses endpoints defined by the
[NGSI-LD API](https://docbox.etsi.org/isg/cim/open/Latest%20release%20NGSI-LD%20API%20for%20public%20comment.pdf). Currently, the service only supports the local-storage as a backend(thus, not supporting horizontal scaling). Therefore, the
following configurations need to be set:

|  Property | Env-Var | Description | Default |
| ----------------- | ----------------------------------- | ----------------------------------------------- | ------------------------ |
| `micronaut.http.services.mintaka.url`        | `MICRONAUT_HTTP_SERVICES_MINTAKA_URL` | Address to the downstream temporal api.    |  http://localhost:8080  |
| `local.enabled`        | `LOCAL_ENABLED` | Should the local storage be enabled.    |  true  |
| `local.baseFolder`        | `LOCAL_BASE_FOLDER` | Folder to be used for storing the generated files.    |  /responses  |
| `local.baseAddress`        | `LOCAL_BASE_ADDRESS` | Address of the Temporal-File API to generate the location headers for accesing the files.   |  http://localhost:7070/  |

Running the api:

- create a folder to be used as local storage: ```mkdir ./responses``` and make it rw: ```chmod a+rw responses```
- run the container with the mounted folder:```docker run -v $(pwd)/responses:/responses --env LOCAL_BASE_FOLDER=/responses quay.io/wi_stefan/temporal-file-api:latest```

## Demo

- create data in the broker:

```shell
curl --location --request POST 'localhost:1026/ngsi-ld/v1/entities'         --header 'Content-Type: application/json'         --data-raw '{
            "id": "urn:ngsi-ld:cattle:my-cattle",
            "type": "Cattle",
            "temp": {
                "type": "Property",
                "value": 37
            }'
```
- query the data, with ```fileResponse=true```
```shell
  curl --location --request GET 'http://localhost:7070/temporal/entities/?id=urn:ngsi-ld:cattle:my-cattle&timerel=after&timeAt=1970-01-01T07:30:00Z&timeproperty=modifiedAt&fileResponse=true'
```
- follow the "Location"-response header to get the file.