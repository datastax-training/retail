from urllib import urlencode

__author__ = 'stevelowenthal'


def makeURL(url, *args):
    url_params = [(args[i],args[i+1].encode("UTF-8")) for i in range(0,len(args),2)]
    return url + '?' + urlencode(url_params)