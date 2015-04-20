The Black Friday project contains 4 major parts.

The first is in the cql directory that sets up the database schema for the entire project.

The second is the data generation portion in the generate_data dir.  It does both an initial static data load with the seed scripts (1.seed_zipcode and 2.seed_retail).  It also generates sample data on a continuous basis using metagener (the scripts in 3.scan_data).

The third part is the web project in the web directory that is a simple Python / Flask project that runs against a local DSE. This expects that you have a local DSE setup and running.

The fourth part is what you will write.  It will be Spark Jobs that can run against both the static data set and also against the dynamic data before it flows into Cassandra.

## Mac Setup ##

1 - Install [brew](http://brew.sh/) and then brew python  - The default mac python won't work
```
  brew python
```

2 - install wget:
```
  brew install wget
```

3 - verify you can run python:  

```
      python
      exit()
```
4 Install Python dependencies:
```
  pip install flask
  pip install blist
  pip install cassandra-driver
  pip install requests
```

7 - Download and unzip [google visualization](https://google-visualization-python.googlecode.com/files/gviz_api_py-1.8.2.tar.gz)

8 - cd into the google visualization directory and run:
```
Installing the library:
  python ./setup.py install
  (You might need root privileges to do this)

Testing the library:
  python ./setup.py test
```

9 - Run the primary project which starts up the Flask Web Project:
```
  cd black-friday/web
  python application.py
```

10 - Verify that the project is running locally by going to [http://localhost:5000/](http://localhost:5000/)  Only the top 3 API pages are currently running.

11 - Setup DSE / Cassandra.  Under the cql directory is the schema for the database.  Import it using:
```
  cd cql
  ~/dse/bin/cqlsh -f retail.cql 127.0.0.1
```

11 - Import the data:
```
  cd generate_data/1.seed_zipcode_data
  python 1.zipcodes-to-cassandra.py
  cd ../2.seed_retail_data
  ./1.download-data.sh
  python 2.data-to-cassandra.py
```

12 - Start metagener.  Open 4 tabs and run each of the following commands in a NEW terminal window.  They need to be run together in separate terminal windows.
```
  ./run-metagener --yaml black-friday.yaml
  python 4.metagener-to-cassandra-stores-employees.py
  nc -l 5005
  python 5.metagener-to-cassandra-scan-items.py
```

### Project Overview ###

The directory web/routes has the basic files for the web project.  It uses the familiar paradigm of Ruby on Rails or Play Framework of having a routes file the defines what happens when the user navigates to different URL's.

route.py - This defines the basic index for the root URL of the project.  It simply generates the html in that method.
```
  @black_friday_api.route('/')
```

rest.py - This defines the rest endpoints for the project.  As one example navigate to [http://localhost:5000/api/paging/system/compaction_history/](http://localhost:5000/api/paging/system/compaction_history/).  This calls the following route in rest.py:
```
  @rest_api.route('/paging/<keyspace>/<table>/')
```
It takes the parameter from the method out of the URL path, so what every keyspace and table and path are passed in will be used for this method.  It then calls a basic select query based on that keyspace and table:
```
  'SELECT * FROM %s.%s' % (keyspace, table)
```

The templates under web/templates are basic jinja2 templates that are similiar to JSP's.  Everything between {%  %}  is part of the script for that template.  Then the content for the page gets pulled in where the {{ content }} is defined.
