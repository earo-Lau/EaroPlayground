import BaseAPIController


class Router(object):
    name = None  # type: str
    controller_type = None  # type: type

    def __init__(self, name=None, controller_type=None):
        """

        :type name: str
        :type controller_type: type
        """
        self.name = name
        self.controller_type = controller_type

    def rock(self, http_context, http_server):
        controller = self.controller_type(http_context, http_server)  # type: BaseAPIController.BaseAPIController
        controller.execute()


class APIRouteTable(object):
    __route_table = []  # type: set[Router]

    def registry(self, router):
        """

        :type router: Router
        """
        self.__route_table.append(router)

    def __iter__(self):
        return self.__route_table

    def get_router(self, path=None):
        """

        :param path: Path
        :type path: str
        """
        router_name = path.split('/')[2]
        for router in self.__route_table:
            if router.name == router_name:
                return router

        return None
