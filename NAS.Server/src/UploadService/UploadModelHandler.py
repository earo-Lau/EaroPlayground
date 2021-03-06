import hashlib
import logging
import os

from Model.UploadModel_pb2 import UploadModel
from UploadService.FileSaveLocker import FileSaveLocker


class UploadModelHandler:
    fileSaveLocker = FileSaveLocker()

    __signature_key = 'c4cd977a71d4e935a29694ef4a14f9ee6701ac01c67e08c021f623af5d324f31'
    __temp_path = '/tmp/NAS.Server/upload'
    __save_path = 'upload'

    def __init__(self):
        pass

    def file_signature(self, file_name):
        return hashlib.sha256('{0}-{1}'.format(file_name, self.__signature_key)).hexdigest()

    def get_temp_file(self, upload_model_id):
        """

        :rtype: UploadModel
        """
        f = open('{0}/{1}'.format(self.__temp_path, upload_model_id), 'rb')
        upload_model = UploadModel()
        upload_model.ParseFromString(f.read())
        f.close()

        return upload_model

    def save_temp_file(self, upload_model):
        """

        save upload model to temp file, thread not safe
        :type upload_model: UploadModel
        """
        try:
            if not os.path.exists(self.__temp_path):
                os.makedirs(self.__temp_path)

            f = open('{0}/{1}'.format(self.__temp_path, upload_model.id), 'wb+')
            streaming = upload_model.SerializeToString()
            f.write(streaming)
            f.close()
            logging.info('temp file %s saved', upload_model.id)

            return streaming
        except IOError, e:
            print 'save temp file {0} exception: {1}'.format(upload_model.id, e.message)
            logging.exception('save temp file %s error', upload_model.id, exc_info=True)
            raise e

    def retrieval_node(self, root, val):
        """
        :type root: StreamingNode
        :type val: int
        :rtype: StreamingNode
        """

        if root is None:
            return None

        if root.id == val:
            return root
        elif root.id > val:
            return self.retrieval_node(root.left, val)
        elif root.id < val:
            return self.retrieval_node(root.right, val)

    def save_file(self, temp_file):
        root = temp_file.root
        try:
            abs_path = os.path.abspath(self.__save_path)
            file_path = '{0}/{1}'.format(abs_path, temp_file.name)
            print('save file to {0}'.format(file_path))

            f = open(file_path, 'w+')
            self.__traversal_node(root, f)
            f.close()
        except IOError, e:
            logging.error('save file %s error', temp_file.name, exc_info=True)
            raise e

        os.remove('{0}/{1}'.format(self.__temp_path, temp_file.id))
        self.fileSaveLocker.destroy_locker(temp_file.id)

    def __traversal_node(self, root, f):
        if not root:
            return None

        if root.HasField('left'):
            self.__traversal_node(root.left, f)
        f.write(root.stream)

        if root.HasField('right'):
            self.__traversal_node(root.right, f)
