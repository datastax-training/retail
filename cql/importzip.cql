COPY retail.zipcodes (zipcode, city, state, lat, long, population, wages) FROM '../csv/zipcodes.csv' WITH delimiter = '|';
