import logging

from google.protobuf.message import Message

from src.Model.UploadModel_pb2 import UploadModel, StreamingNode
from src.APIController.BaseAPIController import BaseAPIController
from src.UploadService.FileSaveLocker import FileSaveLocker
from src.UploadService.StreamNodeHandler import StreamNodeHandler
from src.UploadService.UploadModelHandler import UploadModelHandler


# noinspection PyBroadException
class UploadController(BaseAPIController):
    def create(self):
        body = self._http_context.request.body
        upload_model = UploadModel.ParseFromString(body)  # type: UploadModel
        upload_model.id = UploadModelHandler.file_signature(upload_model.name)

        try:
            streaming_node = StreamNodeHandler.generator(upload_model)
            upload_model.root = streaming_node
            stream = UploadModelHandler.save_temp_file(upload_model)

            self._ok(stream)
        except (IOError, IndexError, OverflowError, KeyError):
            logging.exception('Upload file %s error', upload_model.name, exc_info=True)
            self._http_server.send_error(500, 'Upload file %s error' % upload_model.name)

        return

    def steam(self):
        body = self._http_context.request.body
        streaming_node = StreamingNode.ParseFromString(body)  # type: StreamingNode

        f_locker = get_locker(streaming_node.upload_model)
        f_locker.acquire()

        source_model = UploadModelHandler.get_temp_file(streaming_node.upload_model)
        if not source_model:
            f_locker.release()
            self._http_server.send_error(500, 'source file not found')

        root_node = source_model.root
        source_node = UploadModelHandler.retrieval_node(root_node, streaming_node.id)
        if source_node is None:
            f_locker.release()
            self._http_server.send_error(500, 'file "{0}" content error'.format(source_model.name))

        source_node.stream = streaming_node.stream
        UploadModelHandler.save_temp_file(source_model)
        f_locker.release()

        self._ok('ok')

    def done(self):
        body = self._http_context.request.body

        try:
            temp_file = UploadModelHandler.get_temp_file(body.id)  # type: UploadModel
            UploadModelHandler.save_file(temp_file)

        except (IOError, IndexError, OverflowError, KeyError):
            logging.error('Save file %s error', temp_file.name, exc_info=True)
            self._http_server.send_error(500, 'save file {0} error'.format(temp_file.name))

        self._ok('ok')
