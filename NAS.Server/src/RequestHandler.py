from BaseHTTPServer import BaseHTTPRequestHandler
import rfc822
import json

import urlparse
import HttpMiddleware.HttpMiddleware
import socket
import HttpContext


class RequestHandler(BaseHTTPRequestHandler):
    close_connection = None  # type: int

    def parse_request(self):
        base = self.rfile.readline()

        if not base:
            self.close_connection = 1
            return
        # noinspection PyAttributeOutsideInit,SpellCheckingInspection
        self.requestline = base.rstrip('\r\n')

        header = rfc822.Message(self.rfile, 0)
        body = ''
        if 'content-length' in header.dict:
            content_length = int(header.dict['content-length'])
            body = self.rfile.read(content_length)

        return self.__load_context(base, header.dict, body)

    def __load_context(self, base, header, body):
        context = HttpContext.HttpContext()
        method, url, version = base.split()
        self.__set_version(version)
        path, query = self.__load_url(url)

        context.request.method = method
        context.request.url = url
        context.request.path = path
        context.request.query = query
        context.request.header = header
        encoding = 'utf-8'

        if 'encoding' in context.request.header:
            encoding = context.request.header['encoding']
        if method.upper() != 'GET' and body != '':

            if 'application/json' in header['content-type']:
                context.request.body = json.loads(body, encoding)
            elif 'application/x-protobuf' in header['content-type']:
                context.request.body = body

        return context

    @staticmethod
    def __load_url(url):
        """

        :type url: str
        :return: path, query
        """
        if '?' in url:
            path, qs = url.split('?')
            query = urlparse.parse_qs(qs)
        else:
            path = url
            query = None

        return path, query

    def __set_version(self, version):
        """

        :type version: str
        """
        if version[:5] != 'HTTP/':
            self.send_error(400, "Bad request version (%r)" % version)
            return False
        try:
            base_version_number = version.split('/', 1)[1]
            version_number = base_version_number.split(".")
            if len(version_number) != 2:
                raise ValueError
            version_number = int(version_number[0]), int(version_number[1])
        except (ValueError, IndexError):
            self.send_error(400, "Bad request version (%r)" % version)
            return False
        if version_number >= (1, 1) and self.protocol_version >= "HTTP/1.1":
            self.close_connection = 0
        if version_number >= (2, 0):
            self.send_error(505,
                            "Invalid HTTP Version (%s)" % base_version_number)
            return False
        self.request_version = version

    def handle_one_request(self):
        self.close_connection = 1
        self.request.settimeout(30)

        try:
            http_context = self.parse_request()

            middleware = self.server.middleware  # type: HttpMiddleware.HttpMiddleware.HttpMiddleware
            middleware.next(http_context, self)
        except socket.timeout, e:
            # a read or a write timed out.  Discard this connection
            self.log_error("Request timed out: %r", e)
            self.close_connection = 1
            self.send_error(500, e)
        finally:
            self.wfile.flush()
        return
