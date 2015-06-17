import logging
from flask import Flask
import sys

from helpers.jinjaHelper import makeURL
from routes import web
from routes.rest import rest_api
from routes.google_charts import gcharts_api
from routes.index import index_api
from routes.web import web_api
from helpers.cassandra_helper import init_cassandra


app = Flask(__name__)
app.config.from_pyfile('application.cfg')

# Set up logging
app.logger.setLevel(logging.INFO)  # use the native logger of flask
app.logger.disabled = False
handler = logging.StreamHandler(sys.stdout)

formatter = logging.Formatter( \
    "%(asctime)s - %(levelname)s - %(name)s: \t%(message)s")
handler.setFormatter(formatter)
app.logger.addHandler(handler)

# And the werkzeuglog
werkzeuglog = logging.getLogger('werkzeug')
werkzeuglog.setLevel(logging.INFO)
werkzeuglog.addHandler(handler)

# Register the routes
app.register_blueprint(index_api)
app.register_blueprint(rest_api, url_prefix='/api')
app.register_blueprint(gcharts_api, url_prefix='/gcharts')
app.register_blueprint(web_api, url_prefix='/web')

# Make urlencode available in the templates
app.jinja_env.globals.update(makeURL=makeURL)

def start():
    init_cassandra(app.config['DSE_CLUSTER'].split(','), app.config['KEYSPACE'])
    web.init()

    app.run(host='0.0.0.0',
            port=app.config['APPLICATION_PORT'],
            use_reloader=True,
            threaded=True)


if __name__ == "__main__":
    start()
