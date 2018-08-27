import math

from src.Model.UploadModel_pb2 import StreamingNode


class StreamNodeHandler(object):
    def __init__(self):
        pass

    def generator(self, upload_model):
        """

        :type upload_model: UploadModel
        """
        count = int(math.ceil(upload_model.length / 200000))
        mid = int(math.floor(count / 2))
        root_node = StreamingNode()
        root_node.id = mid

        self.__create_node(root_node, range(0, mid))
        self.__create_node(root_node, range(mid + 1, count))

        return root_node

    def __create_node(self, root_node, r):
        l = len(r)
        mid = int(math.floor(l / 2))
        node = StreamingNode()
        node.id = mid

        if mid < root_node.id:
            root_node.left = node
        elif mid > root_node.id:
            root_node.right = node
        else:
            return node

        self.__create_node(root_node, r[:mid])
        self.__create_node(root_node, r[mid:])
