README
======

This component generates Kafka streams of rasterdata, which can be used to test the integration of rasterdata
processing in [StreamPipes](https://www.streampipes.org).

See [BigGIS](http://biggis-project.eu/biggis-docs/) for the big picture.

This component provides two data streams, which send rasterdata tiles in different formats.

* inline

  contains the rasterdata serialized in the Kafka message.
  As the Kafka message contains JSON content, the binary data is Base64-encoded.

* out-of-band

  contains an HTTP URI for the raster data. This component contains the necessary HTTP server to serve
  the files.


Installation
------------

Add following fragment to your StreamPipes' docker-compose.yml:

    rasterdata-source:
      image: laus.fzi.de:8201/biggisstreampipes/rasterdata-source
      depends_on:
        - "kafka"
      environment:
        JAVA_OPTS: "-Dkafka.server=kafka:9092"
      ports:
        - "8321:9000"
      volumes:
        - "./data/tiles:/tiles"
      networks:
        spnet:

You also need the [Rasterdata Adapter](https://github.com/biggis-project/biggisstreampipes-rasterdataadapter)
to provide Streampipes with the metadata of this component.


Usage
-----

Both streams don't send continuously, but only when triggered externally.
The triggering is done by a HTTP Post request.

    curl -H "application/json" -d $jsonString http://$dockerHost:8321/start-source

The JSON document takes the following fields (the displayed values are the defaults used if no value is given).

    {
     "startIndex": 1,
     "endIndex": 5,
     "interval": 3,
     "tileset": "techpark",
     "serializer": "json-inline"
    }"

`startIndex` and `endIndex` limit the range of tiles sent.
`interval` is the sending interval in seconds.
`tileset` names the tileset used. Custom tilesets can be added, see below.
`serializer` is either `json-inline` or or `json-oob`.

### Adding Tilesets

A tileset is just a sequence of image files, which are served in alphabetical order. If you limit the range served
with `startIndex` and/or `endIndex`, the values reference this order, so for best user experience, name your files
with a common prefix and a zero-padded number.

Put all tile files in one common directory und put it in your tiles docker volume (`data/tiles` in the fragment above).
Use the name of the directory as parameter `tileset` in your JSON request.