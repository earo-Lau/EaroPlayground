import HttpMiddleware
from APIController.UploadController import UploadController
from APIController.APIRouter import APIRouteTable
from APIController.HelloController import HelloController
from HttpContext import HttpContext
from RequestHandler import RequestHandler
from APIController import APIRouter


class ApiMiddleware(HttpMiddleware.HttpMiddleware):
    route_table = None  # type: APIRouteTable

    def __init__(self, f):
        super(ApiMiddleware, self).__init__(f)

        route_table = self.load_route_table()
        self.route_table = route_table

    @staticmethod
    def load_route_table():
        route_table = APIRouter.APIRouteTable()
        route_table.registry(APIRouter.Router('hello', HelloController))
        route_table.registry(APIRouter.Router('upload', UploadController))
        return route_table

    def _handle(self, http_context, http_server):
        """

        :type http_context: HttpContext
        :type http_server: RequestHandler
        """
        path = http_context.request.path
        router = self.route_table.get_router(path)
        if not router:
            http_server.send_error(501, "Unsupported router (%r)" % path)

        router.rock(http_context, http_server)
