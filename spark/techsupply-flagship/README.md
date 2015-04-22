Modify techsupply.flagship.SparkTest.callSparkJob to point to your instance of DSE.  Then run:

```
$ chmod +x sbt
$ ./sbt
> assembly
> container:start
```

Open [http://localhost:8080/](http://localhost:8080/) in your browser and you should see product json.
