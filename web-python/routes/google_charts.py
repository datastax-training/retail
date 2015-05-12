from flask import Blueprint, render_template, request
from helpers.jinjaHelper import makeURL

gcharts_api = Blueprint('gcharts_api', __name__)


supported_charts = {
    'Table': 'table',
    'GeoChart': 'geochart',
    'BarChart': 'corechart',
    'ColumnChart': 'corechart',
    'LineChart': 'corechart',
    'PieChart': 'corechart',
    'AreaChart': 'corechart'
}

#
# Render a basic flexible charts page
#   Parameters:
#      url=the url to return the chart data
#      options= google charting options
#        for more info see "customizing charts":
#          https://developers.google.com/chart/interactive/docs/customizing_charts
#          https://developers.google.com/chart/interactive/docs/datesandtimes#axesgridlinesticks
#

@gcharts_api.route('/<chart_type>/')
def googlechart(chart_type='ColumnChart'):

    # grab url parameter, if available
    url = request.args.get('url')

    # pass all other parameters to ajax url
    url_parms = []
    for k,v in request.args.iteritems():
        if k not in ['url','options']:
            url_parms += [k,v]

    ajax_source = makeURL(url, *(url_parms))

    options = request.args.get('options',{})

    return render_template('google_charts.jinja2',
                           ajax_source=ajax_source,
                           chart_type=chart_type,
                           package=supported_charts[chart_type],
                           options=options)