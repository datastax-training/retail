from flask import Blueprint, render_template

gui_api = Blueprint('gui_api', __name__)


@gui_api.route('/')
def index():
    return render_template('index.jinja2')
