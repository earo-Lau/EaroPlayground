import math

from src.Model.UploadModel_pb2 import StreamingNode


class StreamNodeHandler(object):
    def __init__(self):
        pass

    def generator(self, upload_model):
        """

        :type upload_model: UploadModel
        """
        count = int(math.ceil(upload_model.length / 200000.0))
        mid = int(math.floor(count / 2))
        root_node = StreamingNode()
        root_node.id = mid

        left_node = self.__create_node(range(0, mid))
        right_node = self.__create_node(range(mid + 1, count))
        if left_node:
            root_node.left.CopyFrom(left_node)
        if right_node:
            root_node.right.CopyFrom(right_node)

        return root_node

    def __create_node(self, r):
        length = len(r)
        if length == 0:
            return None

        mid = int(math.floor(length / 2))
        node = StreamingNode()
        node.id = r[mid]

        if length > 3:
            left_node = self.__create_node(r[:mid])
            right_node = self.__create_node(r[mid + 1:])
        elif length == 3:
            left_node = StreamingNode()
            left_node.id = r[mid - 1]

            right_node = StreamingNode()
            right_node.id = r[mid + 1]
        elif length == 2:
            left_node = StreamingNode()
            left_node.id = r[mid - 1]

            right_node = None
        else:
            left_node = None
            right_node = None

        if left_node:
            node.left.CopyFrom(left_node)
        if right_node:
            node.right.CopyFrom(right_node)

        return node

    def node_size(self, root_node):
        if root_node.HasField('right'):
            return self.node_size(root_node.right)

        return root_node.id + 1
