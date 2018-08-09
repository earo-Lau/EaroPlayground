from src.APIController.BaseAPIController import BaseAPIController


class HelloController(BaseAPIController):
    def index(self):
        return self._ok("Hello World")

