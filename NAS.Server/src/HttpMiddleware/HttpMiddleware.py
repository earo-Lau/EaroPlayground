from src.HttpContext import HttpContext
import re


class HttpMiddlewareFilter:
    url_pattern = None  # type: str
    method = None  # type: str

    def __init__(self, url_pattern=None, method=None):
        self.url_pattern = url_pattern
        self.method = method

    def filter(self, http_context):
        """

        :type http_context: RequestHandler
        """
        if self.method:
            if http_context.request.method != self.method:
                return False
        if self.url_pattern:
            if not re.match(self.url_pattern, http_context.request.path):
                return False

        return True


class HttpMiddleware(object):
    _next = None  # type: HttpMiddleware

    def __init__(self, f):
        """

        :type f: HttpMiddlewareFilter
        """
        self._filter = f
        self._next = None

    def __iter__(self):
        return self

    def next(self, http_context, http_server):
        """

        :type http_context: HttpContext
        :type http_server: RequestHandler.RequestHandler
        """
        next = self._next
        while next:
            if next._filter.filter(http_context):
                return next._handle(http_context, http_server)
            else:
                next = next._next

    def registry(self, middleware):
        """

        :type middleware: HttpMiddleware
        """
        if not self._next:
            self._next = middleware
        else:
            self._next.registry(middleware)

    def _handle(self, http_context, http_server):
        """

        :type http_context: HttpContext
        :type http_server: RequestHandler.RequestHandler
        """
        if self._next:
            return self._next.next(http_context, http_server)
