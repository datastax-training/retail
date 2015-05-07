from flask import Blueprint, render_template

index_api = Blueprint('black_friday_api', __name__)


@index_api.route('/')
def index():

    return render_template('index.jinja2')
