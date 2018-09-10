import unittest
import httplib
from src import Server


class MyTestCase(unittest.TestCase):

    def setUp(self):
        super(MyTestCase, self).setUp()
        self._server = Server.main(8079)
        self._connect = httplib.HTTPConnection('localhost', 8079)

    def tearDown(self):
        self._connect.close()

        self._server.shutdown()
        self._server.server_close()

        super(MyTestCase, self).tearDown()

    def test_Main(self):
        self._connect.request('Get', '/api/hello')
        resp = self._connect.getresponse()

        self.assertEqual(resp.status, 200, 'Get api error')
        self.assertEqual(resp.fp.read(), 'Hello World', 'Response message not match')

        resp.close()


if __name__ == '__main__':
    unittest.main()
