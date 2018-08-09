class Request:
    def __init__(self):
        self.method = 'GET'
        self.url = None
        self.path = None
        self.header = {}
        self.body = {}
        self.query = {}


class HttpContext:
    def __init__(self):
        self.request = Request()
