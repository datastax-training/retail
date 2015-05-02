from flask import Blueprint, render_template, request

gcharts_api = Blueprint('gcharts_api', __name__)


def compose_ajax_source():
    """
    grab request GET variables to create new url
    :return: ajax ready url
    """
    # grab url parameter, if available
    url = request.args.get('url')

    # pass all other parameters to ajax url
    ajax_source = '%s?' % url
    for k, v in request.args.iteritems():
        if k != 'url':
            ajax_source += '&%s=%s' % (k, v)

    return ajax_source


@gcharts_api.route('/annotationchart/')
def annotationchart():
    ajax_source = compose_ajax_source()

    return render_template('google-charts.jinja2',
                           ajax_source=ajax_source,
                           gcharts_version=1,
                           packages='annotationchart',
                           data_method='DataTable(jsonData.gcharts[1.1])',
                           chart_type='visualization.AnnotationChart',
                           options='options')


@gcharts_api.route('/areachart/')
def areachart():
    ajax_source = compose_ajax_source()

    return render_template('google-charts.jinja2',
                           ajax_source=ajax_source,
                           gcharts_version=1,
                           packages='corechart',
                           data_method='arrayToDataTable('
                                       'jsonData.gcharts[1])',
                           chart_type='visualization.AreaChart',
                           options='options')


@gcharts_api.route('/barchart/')
def barchart():
    ajax_source = compose_ajax_source()

    return render_template('google-charts.jinja2',
                           ajax_source=ajax_source,
                           gcharts_version=1.1,
                           packages='bar',
                           data_method='DataTable(jsonData.gcharts[1.1])',
                           chart_type='charts.Bar',
                           options='google.charts.Bar.convertOptions(options)')


@gcharts_api.route('/linechart/')
def linechart():
    ajax_source = compose_ajax_source()

    return render_template('google-charts.jinja2',
                           ajax_source=ajax_source,
                           gcharts_version=1.1,
                           packages='line',
                           data_method='DataTable(jsonData.gcharts[1.1])',
                           chart_type='charts.Line',
                           options='google.charts.Line.convertOptions(options)')


@gcharts_api.route('/piechart/')
def piechart():
    ajax_source = compose_ajax_source()

    return render_template('google-charts.jinja2',
                           ajax_source=ajax_source,
                           gcharts_version=1,
                           packages='corechart',
                           data_method='arrayToDataTable('
                                       'jsonData.gcharts[1])',
                           chart_type='visualization.PieChart',
                           options='options')


@gcharts_api.route('/table/')
def table():
    ajax_source = compose_ajax_source()

    return render_template('google-charts.jinja2',
                           ajax_source=ajax_source,
                           gcharts_version=1,
                           packages='table',
                           data_method='arrayToDataTable('
                                       'jsonData.gcharts[1])',
                           chart_type='visualization.Table',
                           options='options')

@gcharts_api.route('/stevechart/')
def stevebarchart():
    ajax_source = compose_ajax_source()

    return render_template('steve-charts.jinja2',
                           ajax_source=ajax_source,
                           gcharts_version=1.1,
                           packages='bar',
                           chart_type='charts.Bar',
                           options='google.charts.Bar.convertOptions(options)')