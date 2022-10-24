Installing the GeoStyler extension
=============================================

 #. Go to the `GeoStyler extension packages <https://github.com/geostyler/geostyler-geoserver-plugin/packages/1469140/versions>`_
    list in the project's `repository <https://github.com/geostyler/geostyler-geoserver-plugin/>`_, select a matching version
    and download the ``gs-geostyler-<VERSION>.jar`` (see the download link on the right).

    .. warning:: Make sure to match the version of the extension to the version of the GeoServer instance!

 #. Copy the file into the ``WEB-INF/lib`` directory of the GeoServer installation.

 #. Restart GeoServer.

The module might actually work in the 2.15 series, but that's highly experimental.
