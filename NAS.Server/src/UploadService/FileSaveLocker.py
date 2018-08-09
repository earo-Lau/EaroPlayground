import threading

from typing import Dict


class FileSaveLocker(object):
    __file_locker = {}  # type: Dict[str, threading.Lock]
    __atom = threading.Lock()

    def get_locker(self, file_id):
        self.__atom.acquire()
        if file_id in self.__file_locker:
            self.__atom.release()
            return self.__file_locker[file_id]

        locker = threading.Lock()
        self.__file_locker = locker
        self.__atom.release()
        return locker

    def destroy_locker(self, file_id):
        return self.__file_locker.pop(file_id, None)
