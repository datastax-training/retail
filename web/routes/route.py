from flask import Blueprint, render_template

black_friday_api = Blueprint('black_friday_api', __name__)


@black_friday_api.route('/')
def index():

    return render_template('index.jinja2')
