XmlToRdf
=============

Java library to convert any XML file to RDF.


Benchmark results
==============

| Method | File size | Time |
|--------|---|---|
|Fast convert | 100 MB | 2.027 seconds |
|Object convert | 100 MB |  2.980 seconds |
|Jena convert | 100 MB |  9.980 seconds |
|Sesame convert | 100 MB |  9.597 seconds |


Benchmark information
 - *JDK*: JDK 1.8.0_65, VM 25.65-b01
 - *Machine*: Macbook Pro 15" Mid 2015
 - *CPU*:  2.8 GHz (i7-4980HQ) with 6 MB on-chip L3 cache and 128 MB L4 cache (Crystalwell)
 - *RAM*: 16 GB
 - *SSD*: 512 GB