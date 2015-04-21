from flask import Blueprint, render_template, request

search_api = Blueprint('search_api', __name__)


def compose_ajax_source():
    """
    grab request GET variables to create new url
    :return: ajax ready url
    """
    # grab url parameter, if available
    url = request.args.get('url', '/api/paging/system/compaction_history')

    # pass all other parameters to ajax url
    ajax_source = '%s?' % url
    for k, v in request.args.iteritems():
        if k != 'url':
            ajax_source += '&%s=%s' % (k, v)

    return ajax_source



@search_api.route('/')
def table():
    ajax_source = compose_ajax_source()

    return render_template('city_search.jinja2',
                           ajax_source=ajax_source)
