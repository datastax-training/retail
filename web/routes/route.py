from flask import Blueprint, render_template

black_friday_api = Blueprint('black_friday_api', __name__)


@black_friday_api.route('/')
def index():
    content = '''
    List of example links to be used to showcase and ingest data on the frontend:
    <ul>
        <li>
            <a href="/api">API: Test Page</a>
        </li>
        <li>
            <a href="/api/paging/system/compaction_history">API: system.compaction_history</a>
        </li>
        <li>
            <a href="/api/paging/system/schema_keyspaces">API: system.schema_keyspaces</a>
        </li>
        <li>
            <a href="/gcharts/table/?gcharts_columns=compacted_at,id,keyspace_name,columnfamily_name,rows_merged,bytes_in,bytes_out&result_size=100">
                Google Charts: Tabular Example
            </a>
        </li>
    </ul>
    <ul>
        <li>
            <a href="/api/paging/retail/products?result_size=5&paging_keys=product_id&paging_values=B000ET7AZK">
                API: Sample Query with Custom Values
            </a>
        </li>
        <li>
            <a href="/gcharts/barchart/?url=/api/paging/retail/products&result_size=100&gcharts_columns=product_id,price&gcharts_datatable_order_by=price">
                Google Charts: Bar Chart Example
            </a>
        </li>
        <li>
            <a href="/gcharts/table/?url=/api/paging/retail/employees&result_size=100">
                Google Charts: Employees Table
            </a>
        </li>
        <li>
            <a href="/gcharts/table/?url=/api/paging/retail/stores&result_size=100">
                Google Charts: Stores Table
            </a>
        </li>
    </ul>
    '''
    return render_template('index.jinja2',
                           content=content)
