import sys
import logging
from BaseHTTPServer import HTTPServer as HTTPServer
from argparse import ArgumentParser, ArgumentDefaultsHelpFormatter

from RequestHandler import RequestHandler
from HttpMiddleware import HttpMiddleware, ApiMiddleware

logging.basicConfig(format='%(asctime)s %(levelname)s %(message)s',
                    level=logging.DEBUG,
                    stream=sys.stdout)


# noinspection PyTypeChecker
def get_parser():
    """Get a command line parser for docker-hook."""
    parser = ArgumentParser(description=__doc__,
                            formatter_class=ArgumentDefaultsHelpFormatter)

    parser.add_argument("--port",
                        dest="port",
                        type=int,
                        default=8073,
                        metavar="PORT",
                        help="port where it listens")
    return parser


def load_middleware(server):
    """

    :type server: HTTPServer
    """
    middleware = HttpMiddleware.HttpMiddleware(HttpMiddleware.HttpMiddlewareFilter())
    middleware.registry(ApiMiddleware.ApiMiddleware(HttpMiddleware.HttpMiddlewareFilter('^/api/')))
    server.middleware = middleware


def main(port=8073):
    """
    :type port: int
    """
    server = HTTPServer(('', port), RequestHandler)
    # Load Middleware
    load_middleware(server)

    #

    server.serve_forever()
    logging.info('Server is Setup to port:{0}', port)


if __name__ == '__main__':
    parser = get_parser()

    args = parser.parse_args()
    main(args.port)
