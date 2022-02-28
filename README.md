# Temporal File API

Facade to the NGSI-LD File-API, providing the responses as a file download.

## Deployment

The service is developed as a facade to the actual NGSI-LD API, thus requires a real implementation downstream. The [docker-compose](./src/test/resources/docker-compose/docker-compose.yml) provides an environment
with [Orion-LD](https://github.com/FIWARE/context.Orion-LD) and [Mintaka](https://github.com/FIWARE/mintaka), but it only uses endpoints defined by the
[NGSI-LD API](https://docbox.etsi.org/isg/cim/open/Latest%20release%20NGSI-LD%20API%20for%20public%20comment.pdf). Currently, the service only supports the local-storage as a backend(thus, not supporting horizontal scaling). Therefore, the
following configurations need to be set:


|  Property | Env-Var | Description | Default |
| ----------------- | ----------------------------------- | ----------------------------------------------- | ------------------------ |
| `micronaut.http.services.mintaka.url`        | `MICRONAUT_HTTP_SERVICES_MINTAKA_URK` | Address to the downstream temporal api.    |  http://localhost:8080  |
| `local.enabled`        | `LOCAL_ENABLED` | Should the local storage be enabled.    |  true  |
| `local.baseFolder`        | `LOCAL_BASE_FOLDER` | Folder to be used for storing the generated files.    |  /responses  |
| `local.baseAddress`        | `LOCAL_BASE_ADDRESS` | Address of the Temporal-File API to generate the location headers for accesing the files.   |  http://localhost:7070/  |

## Demo