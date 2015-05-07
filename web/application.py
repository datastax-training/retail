from flask import Flask

from utils.JinjaHelper import makeURL
from routes import rest
from routes import web
from routes.rest import rest_api
from routes.google_charts import gcharts_api
from routes.index import index_api
from routes.web import web_api


app = Flask(__name__)
app.config.from_pyfile('application.cfg')

# Register the routes
app.register_blueprint(index_api)
app.register_blueprint(rest_api, url_prefix='/api')
app.register_blueprint(gcharts_api, url_prefix='/gcharts')
app.register_blueprint(web_api, url_prefix='/web')

# Make urlencode available in the templates
app.jinja_env.globals.update(makeURL=makeURL)

def start():
    rest.init_cassandra(app.config['DSE_CLUSTER'].split(','))
    web.init()

    app.run(host='0.0.0.0',
            port=app.config['APPLICATION_PORT'],
            use_reloader=True,
            threaded=True)


if __name__ == "__main__":
    start()