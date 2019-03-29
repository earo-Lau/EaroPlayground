from RequestHandler import RequestHandler


class BaseAPIController(object):
    _http_server = None  # type: RequestHandler

    def __init__(self, http_context, http_server):
        """

        :param http_context: HttpContext
        :param http_server: RequestHandler
        :type http_context: src.HttpContext
        :type http_server: RequestHandler
        """
        self._http_context = http_context
        self._http_server = http_server

    def execute(self):
        path = self._http_context.request.path.rstrip('/').split('/')
        action = 'index'
        if len(path) > 3:
            action = path[3]

        if not hasattr(self, action):
            self._http_server.send_error(501, "Unsupported method (%r)" % action)
            return

        method = getattr(self, action)
        method()
        pass

    def _ok(self, content=None):
        self._http_server.send_response(200)
        self._http_server.end_headers()
        self._http_server.wfile.write(bytes(content))
        return

    def _err(self, code, msg):
        self._http_server.send_error(code, msg)
        return

