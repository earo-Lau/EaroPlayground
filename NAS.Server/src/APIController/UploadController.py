import logging

from google.protobuf.message import Message

from src.Model.UploadModel_pb2 import UploadModel, StreamingNode
from src.APIController.BaseAPIController import BaseAPIController
from src.UploadService.StreamNodeHandler import StreamNodeHandler
from src.UploadService.UploadModelHandler import UploadModelHandler


# noinspection PyBroadException
class UploadController(BaseAPIController):

    def __init__(self, http_context, http_server):
        super(UploadController, self).__init__(http_context, http_server)
        self.__upload_model_handler = UploadModelHandler()
        self.__stream_node_handler = StreamNodeHandler()

    def create(self):
        body = self._http_context.request.body
        upload_model = UploadModel()  # type: UploadModel
        upload_model.ParseFromString(body)
        upload_model.id = self.__upload_model_handler.file_signature(upload_model.name)

        try:
            streaming_node = self.__stream_node_handler.generator(upload_model)
            upload_model.root.CopyFrom(streaming_node)
            stream = self.__upload_model_handler.save_temp_file(upload_model)

            self._ok(stream)
        except (IOError, IndexError, OverflowError, KeyError):
            logging.exception('Upload file %s error', upload_model.name, exc_info=True)
            self._http_server.send_error(500, 'Upload file %s error' % upload_model.name)

        return

    def stream(self):
        body = self._http_context.request.body
        streaming_node = StreamingNode()
        streaming_node.ParseFromString(body)  # type: StreamingNode

        f_locker = UploadModelHandler.fileSaveLocker.get_locker(streaming_node.upload_modle)
        f_locker.acquire()

        source_model = self.__upload_model_handler.get_temp_file(streaming_node.upload_modle)
        if not source_model:
            f_locker.release()
            self._http_server.send_error(500, 'source file not found')

        root_node = source_model.root
        source_node = self.__upload_model_handler.retrieval_node(root_node, streaming_node.id)
        if source_node is None:
            f_locker.release()
            self._http_server.send_error(500, 'file "{0}" content error'.format(source_model.name))

        source_node.stream = streaming_node.stream
        self.__upload_model_handler.save_temp_file(source_model)
        f_locker.release()

        self._ok('ok')

    def done(self):
        body = self._http_context.request.body
        upload_model = UploadModel()
        upload_model.ParseFromString(body)

        try:
            temp_file = self.__upload_model_handler.get_temp_file(upload_model.id)  # type: UploadModel
            UploadModelHandler.save_file(temp_file)

        except (IOError, IndexError, OverflowError, KeyError):
            logging.error('Save file (%s) error', upload_model.name, exc_info=True)
            self._http_server.send_error(500, 'save file {0} error'.format(temp_file.name))

        self._ok('ok')
